import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LocalSearch {

	public LocalSearch() {
	}
	
	public void schedule(Transporter vehicle, List<WorkStation> stations) {
		if (stations == null || stations.size() <= 1) {
			return;
		}
		if (stations.size() > 10) {
			stations = stations.subList(0, 10);
		}
		vehicle.taskSequence = heuristic(vehicle, stations);
	}
	
	List<WorkStation> heuristic(Transporter vehicle, List<WorkStation> stations) {
		stations = combinedReinsert(vehicle, stations);
		double cost = computeCost(vehicle, stations);
		for (int i = 0; i < 3; i++) {
			List<WorkStation> shufflestations = new LinkedList<WorkStation>(stations);
			Collections.shuffle(shufflestations);
			shufflestations = combinedReinsert(vehicle, shufflestations);
			double newCost = computeCost(vehicle, shufflestations);
			if (newCost < cost) {
				System.out.println("优化： " + (cost - newCost));
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
				if (computeCost(vehicle, joinedstations) < computeCost(vehicle, stations)) {
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
				if (computeCost(vehicle, joinedstations) < computeCost(vehicle, stations)) {
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
		double cost = computeCost(vehicle, stations);
		for (int i = 0; i < stations.size() - 2; i++) {
			Collections.swap(swapedstations, i, i+1);
			List<WorkStation> newstations = reinsertBackwards(vehicle, swapedstations);
			double newCost = computeCost(vehicle, newstations);
			if (newCost < cost) {
				cost = newCost;
				stations = newstations;
			}
			Collections.swap(swapedstations, i, i+1);
		}
		return stations;
	}	
	
	private double computeCost(Transporter vehicle, List<WorkStation> stations) {
		double posX = vehicle.x;
		double posY = vehicle.y;
		
		double cost = 0;
		double time = vehicle.presentTime().getTimeAsDouble();
		for (WorkStation s : stations) {
			double dis = Helper.computeDist(posX, posY, s.x, s.y);
			double jobTime = s.nextJobTime.getTimeAsDouble();
			// 到达任务的时间
			time += dis / vehicle.speed;
			// 到达时任务是否已经开始
			if (jobTime > time) {
				time = jobTime;
			}
			else {
				cost += (time - jobTime); 
			}
			// 换件的时间
			time += 5;
			posX = s.x;
			posY = s.y;
		}
		
		return cost;
	}
}
