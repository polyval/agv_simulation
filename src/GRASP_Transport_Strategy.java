import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;

// 每一次计算得到的解优化了也不一定代表最终的整体结果会更优
// 由于具有随机性，应该比较多次运行的平均结果
// 重调度越频繁，效果越好,最大等待时间可能增大
// 结果好的时候: <5 reschedule
// 局部搜索可以改善性能，但是计算时间更长
// 运行时间越长，相对于指派规则性能越好
// 降低速度或者增大机床距离可以放大最终结果的比较效果
public class GRASP_Transport_Strategy extends ModelComponent implements
		TransportStrategy {
	
	private TransporterModel myModel;
	private double curTime;
	private Random random = new Random();
	private Map<Transporter, List<WorkStation>> results = new HashMap<>();
	private InsertLocalSearch localSearch;
	private UniLocalSearch search;
	
	public GRASP_Transport_Strategy(Model owner) {

		super(owner, "GRASPTransportStrategy"); // make a ModelComponent
		myModel = (TransporterModel) owner;
		localSearch = new InsertLocalSearch(myModel.getLoadingTime());
		search = new UniLocalSearch(myModel.getLoadingTime());
	}


	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		boolean reschedule = false;
//		for (Transporter t : myModel.transporters) {
//			if (t.state != Transporter.State.UNAVAILABLE && t.taskSequence.size() <= 3) {
//				reschedule = true;
//				break;
//			}
//		}
		if (remainTasks() < 17) {
			reschedule = true;
		}
	
		if (reschedule) {
			long startTime = System.nanoTime();
			scheduleAll(getScheduleableTransporter(), getSchedulableStation());
			System.out.println("计算时间(ms)：" + (System.nanoTime() - startTime) / 1000000);
		}
		
		for (Transporter t : myModel.transporters) {
			System.out.println(t.taskSequence);
			if (t.state == Transporter.State.IDLE && !t.taskSequence.isEmpty()) {
				t.task = t.taskSequence.get(0);
				t.state = Transporter.State.MOVING;
				t.startTime = presentTime();
				t.activate();
			}
		}
		
	}
	
	public int remainTasks() {
		int res = 0;
		for (Transporter t : myModel.transporters) {
			if (t.state != Transporter.State.UNAVAILABLE) {
				res += t.taskSequence.size();
			}
		}
		return res;
	}
	
	public void scheduleAll(List<Transporter> transporters, List<WorkStation> stations) {
		if (transporters.size() == 0 || stations.size() == 0) {
			return;
		}
		curTime = presentTime().getTimeAsDouble();
		
		List<Transporter> vehicles = (List<Transporter>) transporters;
		updateVehicle(vehicles);
		
		int rclLength = 3 * vehicles.size();
 		double totalCost = Double.MAX_VALUE;
 		Map<Transporter, List<WorkStation>> finalResults = null;
 		
 		int i = 0;
 		while (i < 1000) {
 			init(vehicles);
 			List<WorkStation> candidates = new LinkedList<>(stations); 
 			
 			while (!candidates.isEmpty()) {
 				List<Element> candidateElements = getCandidateElements(candidates, vehicles);
 				Element e = getRandomElement(candidateElements, rclLength);
 				results.get(e.t).add(e.index, e.s);
 				candidates.remove(e.s);
 			}
// 			
// 			for (Transporter v : vehicles) {
// 				results.put(v, search.search(v, results.get(v)));
// 			}
// 			
// 			System.out.println("局部搜索" + results);
 			List<List<WorkStation>> s = new ArrayList<>();
 			for (Transporter v : vehicles) {
 				s.add(results.get(v));
 			}
 			localSearch.search(vehicles, s);
// 			System.out.println("局部搜索后" + results);
 			
 			double curCost = getTotalCost(vehicles);
 			if (curCost < totalCost) {
 				totalCost = curCost;
 				finalResults = new HashMap<>(results);
 			}
 			i++;
 		}
		
 		results = finalResults;
 		System.out.println(results);
		assign(vehicles);
	}
	
	
	private double getTotalCost(List<Transporter> vehicles) {
		double cost = 0;
		for (Transporter t : vehicles) {
			cost += evaluate(t, results.get(t));
		}
		return cost;
	}
	
	private void assign(List<Transporter> vehicles) {
		for (Transporter t : vehicles) {
			t.taskSequence = results.get(t);
		}
	}
	
	private void init(List<Transporter> vehicles) {
		results.clear();
		for (Transporter t : vehicles) {
			results.put(t, new LinkedList<WorkStation>());
		}
	}
	
	public List<Transporter> getScheduleableTransporter() {
		List<Transporter> vehicles = new ArrayList<>();
		for (Transporter t : myModel.transporters) {
			if (t.state == Transporter.State.UNAVAILABLE) {
				t.taskSequence.clear();
				continue;
			}
			vehicles.add(t);
		}
		return vehicles;
	}
	
	public List<WorkStation> getSchedulableStation() {
		List<WorkStation> stations = new LinkedList<>();
		for (WorkStation s : myModel.stations) {
			stations.add(s);
		}
		for (Transporter t : myModel.transporters) {
			if (t.state == Transporter.State.EXECUTING ||
					t.state == Transporter.State.MOVING) {
				stations.remove(t.task);
			}
		}
		return stations;
	}
	
	private Element getRandomElement(List<Element> rcl, int rclLength) {
		if (rcl.size() >= rclLength) {
			return rcl.get(random.nextInt(rclLength));
		}
		return rcl.get(random.nextInt(rcl.size()));
		
	}
	
	private List<Element> getCandidateElements(List<WorkStation> stations, List<Transporter> vehicles) {
		List<Element> candidateElements = new ArrayList<>();
		for (WorkStation station : stations) {
			List<Element> elements = new ArrayList<>();
			Element bestElement = null;
			for (Transporter t : vehicles) {
				// Get best insertion position in this route
				Element curBest = getBestInsertion(station, t);
				if (bestElement == null || curBest.cost < bestElement.cost) {
					bestElement = curBest;
				}
				elements.add(curBest);
			}
			// Get insertion priority
			double priority = 0;
			for (Element e : elements) {
				priority += e.cost - bestElement.cost;
			}
			bestElement.priority = priority;
			candidateElements.add(bestElement);
		}
		Collections.sort(candidateElements);
		return candidateElements;
	}
	
	
	public void updateVehicle(List<Transporter> vehicles) {
		for (Transporter t : vehicles) {
			t.taskSequence.clear();
			if (t.state == Transporter.State.MOVING) {
				double[] pos = t.getEnRoutePosition();
				t.setPosition(pos[0], pos[1]);
			}
		}
	}
	
	public double evaluate(Transporter vehicle, List<WorkStation> stations) {
		return Helper.computeCost(vehicle, stations, myModel.getLoadingTime());
	}
	
	public Element getBestInsertion(WorkStation station, Transporter vehicle) {
		List<WorkStation> taskSequence = new LinkedList<>(results.get(vehicle));
		
		int bestInsertionIndex = 0;
		double originCost = evaluate(vehicle, taskSequence);
		double addedCost = Integer.MAX_VALUE;
		for (int i = 0; i <= vehicle.taskSequence.size(); i++) {
			double curCost;
			taskSequence.add(i, station);
			curCost = evaluate(vehicle, taskSequence);
			if ((curCost - originCost) < addedCost) {
				addedCost = curCost - originCost;
				bestInsertionIndex = i;
			}
			taskSequence.remove(i);
		}
		
		return new Element(vehicle, station, bestInsertionIndex, addedCost);
	}
	
	class Element implements Comparable<Element>{
		Transporter t;
		WorkStation s;
		int index;
		double cost;
		double priority;
		
		Element(Transporter t, WorkStation s, int index, double cost) {
			this.t = t;
			this.s = s;
			this.index = index;
			this.cost = cost;
		}
		
		@Override
		public int compareTo(Element o) {
			if (this.priority > o.priority) {
				return -1;
			}
			if (this.priority < o.priority) {
				return 1;
			}
			return 0;
		}
	}
}