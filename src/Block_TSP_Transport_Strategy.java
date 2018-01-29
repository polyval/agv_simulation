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

// ·ÖÇø
public class Block_TSP_Transport_Strategy extends ModelComponent implements
		TransportStrategy {
	
	private TransporterModel myModel;
	private double curTime;
	private Random random = new Random();
	private Map<Transporter, List<WorkStation>> results = new HashMap<>();
	private InsertLocalSearch localSearch;
	private UniLocalSearch search;
	
	public Block_TSP_Transport_Strategy(Model owner) {

		super(owner, "BlockTSPTransportStrategy"); // make a ModelComponent
		myModel = (TransporterModel) owner;
		localSearch = new InsertLocalSearch(myModel.getLoadingTime());
		search = new UniLocalSearch(myModel.getLoadingTime());
	}


	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		boolean reschedule = false;
		for (Transporter t : myModel.transporters) {
			if (t.state == Transporter.State.IDLE) {
				scheduleOne(t, getSchedulableStation(t));
			}
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

	public void scheduleOne(Transporter transporter, List<WorkStation> stations) {
		init(transporter, new ArrayList<>(stations));
		transporter.taskSequence = search.search(transporter, transporter.taskSequence);
	}
	
	public void init(Transporter transporter, List<WorkStation> stations) {
		transporter.taskSequence.clear();
		double time = transporter.presentTime().getTimeAsDouble();
		while (!stations.isEmpty()) {
			WorkStation best = null;
			double bestCost = Double.MAX_VALUE;
			for (int i = 0; i < stations.size(); i++) {
				WorkStation s = stations.get(i);
				if (Math.abs(s.nextJobTime.getTimeAsDouble() - time) < bestCost) {
					best = s;
					bestCost = Math.abs(s.nextJobTime.getTimeAsDouble() - time);
				}
			}
			transporter.taskSequence.add(best);
			stations.remove(best);
		}
	}
	
	public List<WorkStation> getSchedulableStation(Transporter t) {
		List<WorkStation> stations = new LinkedList<>();
		int tNum = lastInt(t.getName());
		for (WorkStation s : myModel.stations) {
			int sNum = lastInt(s.getName());
			if (sNum > (tNum - 1)*5 && sNum <= tNum*5) {
				stations.add(s);
			}
		}
		if (t.state == Transporter.State.EXECUTING ||
				t.state == Transporter.State.MOVING) {
			stations.remove(t.task);
		}
		return stations;
	}

	
	public static int lastInt(String s) {
		int i = s.length();
		while (i > 0 && Character.isDigit(s.charAt(i - 1))) {
	        i--;
	    }
	    return Integer.parseInt(s.substring(i));
	}
}