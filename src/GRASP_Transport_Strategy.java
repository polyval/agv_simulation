import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;


public class GRASP_Transport_Strategy extends ModelComponent implements
		TransportStrategy {
	
	private TransporterModel myModel;
	private double curTime;
	private Random random = new Random();
	
	public GRASP_Transport_Strategy(Model owner) {

		super(owner, "GRASPTransportStrategy"); // make a ModelComponent
		myModel = (TransporterModel) owner;
	}


	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		boolean reschedule = false;
		for (Transporter t : myModel.transporters) {
			if (t.state != Transporter.State.UNAVAILABLE && t.taskSequence.size() <= 0) {
				reschedule = true;
				break;
			}
		}
	
		if (reschedule) {
			scheduleAll(getScheduleableTransporter(), getSchedulableStation());
		}
		
		for (Transporter t : myModel.transporters) {
			System.out.println(t.taskSequence);
			if (t.state == Transporter.State.IDLE) {
				t.task = t.taskSequence.get(0);
				t.state = Transporter.State.MOVING;
				t.activate();
			}
		}
		
	}
	
	public void scheduleAll(List<Transporter> transporters, List<WorkStation> stations) {
		curTime = presentTime().getTimeAsDouble();
		List<WorkStation> candidates = stations; 
		List<Transporter> vehicles = (List<Transporter>) transporters;
		updateVehicle(vehicles);
		int rclLength = 3 * vehicles.size();
		
		while (!candidates.isEmpty()) {
			List<Element> candidateElements = getCandidateElements(candidates, vehicles);
			Element e = getRandomElement(candidateElements, rclLength);
			e.t.taskSequence.add(e.index, e.s);
			candidates.remove(e.s);
		}
		
	}
	
	public List<Transporter> getScheduleableTransporter() {
		List<Transporter> vehicles = new ArrayList<>();
		for (Transporter t : myModel.transporters) {
			if (t.state == Transporter.State.UNAVAILABLE) {
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
		double posX = vehicle.x;
		double posY = vehicle.y;
		
		double cost = 0;
		double time = curTime;
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
	
	public Element getBestInsertion(WorkStation station, Transporter vehicle) {
		List<WorkStation> taskSequence = new LinkedList<>(vehicle.taskSequence);
		
		int bestInsertionIndex = 0;
		double cost = Integer.MAX_VALUE;
		for (int i = 0; i <= vehicle.taskSequence.size(); i++) {
			double curCost;
			taskSequence.add(0, station);
			if ((curCost = evaluate(vehicle, taskSequence)) < cost) {
				cost = curCost;
				bestInsertionIndex = i;
			}
			taskSequence.remove(i);
		}
		
		return new Element(vehicle, station, bestInsertionIndex, cost);
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