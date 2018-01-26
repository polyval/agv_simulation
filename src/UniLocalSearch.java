import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UniLocalSearch {
	private int processTime = 0;
	
	public UniLocalSearch(int processTime) {
		this.processTime = processTime;
	}
	
	List<WorkStation> search(Transporter vehicle, List<WorkStation> stations) {
		stations = combinedReinsert(vehicle, stations);
		double cost = Helper.computeCost(vehicle, stations, processTime);
		for (int i = 0; i < 3; i++) {
			List<WorkStation> shufflestations = new LinkedList<WorkStation>(stations);
			Collections.shuffle(shufflestations);
			shufflestations = combinedReinsert(vehicle, shufflestations);
			double newCost = Helper.computeCost(vehicle, shufflestations, processTime);
			if (newCost < cost) {
//				System.out.println("ÓÅ»¯£º " + (cost - newCost));
				cost = newCost;
				stations = shufflestations;
			}
		}
		return stations;
	}
	
	private List<WorkStation> combinedReinsert(Transporter vehicle, List<WorkStation> stations) {
		List<WorkStation> newstations;
		newstations = reinsertBackwards(vehicle, stations);
		newstations = reinsert(vehicle, newstations);
		newstations = swapReinsert(vehicle, newstations);
		newstations = reinsert(vehicle, newstations);
		newstations = reinsertBackwards(vehicle, newstations);
		return newstations;
	}
	
	List<WorkStation> reinsert(Transporter vehicle, List<WorkStation> stations) {
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
		return stations;
	}
	
	private List<WorkStation> reinsertBackwards(Transporter vehicle, List<WorkStation> stations) {
		// Indicate whether the station has been inserted.
		boolean inserted = false;
		int i = stations.size() - 1; 
		// Didn't reinsert the last one.
		while (i > 0) {
			List<WorkStation> leftstations = new LinkedList<WorkStation>(stations.subList(0, i));
			List<WorkStation> rightstations = new LinkedList<WorkStation>(stations.subList(i + 1, stations.size()));
			// The WorkStation to try the insertion.
			WorkStation insertionWorkStation = stations.get(i);
	
			inserted = false;
			int leftSize = leftstations.size();
			for (int j = 0; j < leftSize; j++) {
				// Insert the station backwards.
				leftstations.add(j, insertionWorkStation);
				// Get the stations after insertion.
				List<WorkStation> joinedstations = new LinkedList<WorkStation>(leftstations);
				joinedstations.addAll(rightstations);
				// If a cost reduction is found.
				if (Helper.computeCost(vehicle, joinedstations, processTime) < Helper.computeCost(vehicle, stations, processTime)) {
					stations = joinedstations;
					inserted = true;
					break;
				}
				leftstations.remove(j);
			}
			
			if (inserted == false) {
				i--;
			}
		}
		return stations;
	}
	
	private List<WorkStation> swapReinsert(Transporter vehicle, List<WorkStation> stations) {
		if (stations.size() < 3) {
			return stations;
		}
		List<WorkStation> swapedstations = new LinkedList<WorkStation>(stations);
		double cost = Helper.computeCost(vehicle, stations, processTime);
		for (int i = 0; i < stations.size() - 2; i++) {
			Collections.swap(swapedstations, i, i+1);
			List<WorkStation> newstations = reinsertBackwards(vehicle, swapedstations);
			double newCost = Helper.computeCost(vehicle, newstations, processTime);
			if (newCost < cost) {
				cost = newCost;
				stations = newstations;
			}
			Collections.swap(swapedstations, i, i+1);
		}
		return stations;
	}	
}
