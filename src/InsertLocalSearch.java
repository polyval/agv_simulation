import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InsertLocalSearch {
	
	private int processTime = 0;

	public InsertLocalSearch(int processTime) {
		this.processTime = processTime;
	}
	
	void search(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		for (int i = 0; i < vehicles.size(); i++) {
			reinsert(vehicles.get(i), stations.get(i));
		}
		exchange(vehicles, stations);
		reLocate(vehicles, stations);
		for (int i = 0; i < vehicles.size(); i++) {
			reinsert(vehicles.get(i), stations.get(i));
		}
	}
	
	void reinsert(Transporter vehicle, List<WorkStation> stations) {
		// Indicate whether the station has been inserted.
		boolean inserted = false;
		int i = 0; 
		// Didn't reinsert the last one.
		while (i < stations.size() - 1) {
			List<WorkStation> leftstations = new LinkedList<WorkStation>(stations.subList(0, i));
			List<WorkStation> rightstations = new LinkedList<WorkStation>(stations.subList(i + 1, stations.size()));
			// The WorkStation to try the insertion.
			WorkStation insertionWorkStation = stations.get(i);
	
			inserted = false;
			int rightSize = rightstations.size();
			for (int j = 1; j < rightSize + 1; j++) {
				// Insert the station afterwards.
				rightstations.add(j, insertionWorkStation);
				// Get the stations after insertion.
				List<WorkStation> joinedstations = new LinkedList<WorkStation>(leftstations);
				joinedstations.addAll(rightstations);
				// If a cost reduction is found.
				if (Helper.computeCost(vehicle, joinedstations, processTime) < Helper.computeCost(vehicle, stations, processTime)) {
					stations = joinedstations;
					inserted = true;
					break;
				}
				rightstations.remove(j);
			}
			
			if (inserted == false) {
				i++;
			}
		}
	}
	
	void reLocate(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		for (int i = 0; i < vehicles.size() - 1; i++) {
			for (int j = i + 1; j < vehicles.size(); j++) {
				List<Transporter> v = new ArrayList<>();
				List<List<WorkStation>> s = new ArrayList<>();
				v.add(vehicles.get(i));
				v.add(vehicles.get(j));
				s.add(stations.get(i));
				s.add(stations.get(j));
				reLocateTwo(v, s);
				Collections.reverse(v);
				Collections.reverse(s);
				reLocateTwo(v, s);
			}
		}
	}
	
	void exchange(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		for (int i = 0; i < vehicles.size() - 1; i++) {
			for (int j = i + 1; j < vehicles.size(); j++) {
				List<Transporter> v = new ArrayList<>();
				List<List<WorkStation>> s = new ArrayList<>();
				v.add(vehicles.get(i));
				v.add(vehicles.get(j));
				s.add(stations.get(i));
				s.add(stations.get(j));
				exchangeTwo(v, s);
				Collections.reverse(v);
				Collections.reverse(s);
				exchangeTwo(v, s);
			}
		}
	}
	
	void exchangeTwo(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		double preCost = getCost(vehicles, stations);
	
		for (int i = 0; i < stations.get(0).size(); i++) {
			double curCost = Double.MAX_VALUE;
			int index = -1;
			WorkStation temp = stations.get(0).get(i);
			for (int j = 0; j < stations.get(1).size(); j++) {
				stations.get(0).set(i, stations.get(1).get(j));
				stations.get(1).set(j, temp);
				double newCost = getCost(vehicles, stations);
				if (newCost < curCost) {
					curCost = newCost;
					index = j;
				}
				stations.get(1).set(j, stations.get(0).get(i));
				stations.get(0).set(i, temp);
			}
			if (curCost < preCost) {
				preCost = curCost;
				stations.get(0).set(i, stations.get(1).get(index));
				stations.get(1).set(index, temp);
			}
		}
	}
	
	void reLocateTwo(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		if (vehicles.size() != 2 && stations.size() != 2) {
			return;
		}
		double preCost = getCost(vehicles, stations);
		
		int i = 0;
		boolean inserted = false;
	
		while (i < stations.get(0).size()) {
			WorkStation insertionWorkStation = stations.get(0).get(i);
			stations.get(0).remove(i);
			inserted = false;
			
			int j;
			int bestIndex = -1;
			double bestCost = Double.MAX_VALUE;
			int size = stations.get(1).size();
			for (j = 0; j <= size; j++) {
				stations.get(1).add(j, insertionWorkStation);
				
				double newCost = getCost(vehicles, stations);
				if (newCost < bestCost) {
					bestCost = newCost;
					bestIndex = j;
				}
				stations.get(1).remove(j);
			}
			
			if (bestCost < preCost) {
				preCost = bestCost;
				inserted = true;
				stations.get(1).add(bestIndex, insertionWorkStation);
			}
			
			if (inserted == false) {
				stations.get(0).add(i, insertionWorkStation);
				i++;
			}
		} 
	}
	
	double getCost(List<Transporter> vehicles, List<List<WorkStation>> stations) {
		double cost = 0;
		for (int i = 0; i < vehicles.size(); i++) {
			cost += Helper.computeCost(vehicles.get(i), stations.get(i), processTime);
		}
		return cost;
	}
}
