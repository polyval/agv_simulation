import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeSpan;

/***
 * Simple additive weight method
 * @author polyval
 *
 */
public class SAWMP_Transport_Strategy  extends ModelComponent implements TransportStrategy {
	
	private TransporterModel myModel;
	private double minWaitingTime;
	private double maxWaitingTime;
	private Map<Transporter, double[]> m;
	
	public SAWMP_Transport_Strategy(Model owner) {
		super(owner, "NVFTransportStrategy");
		myModel = (TransporterModel) owner;
	}

	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		//包括前往任务的车辆
		for (Transporter t : myModel.transporters) {
			if (t.state == Transporter.State.MOVING) {
				transporters.insert(t);
				stations.insert(t.task);
			}
		}
		
		init(transporters, stations);
		
		List<TransporterJob> transporterjobs = new LinkedList<>();
		for (int i = 0; i < transporters.size(); i++) {
			Transporter t = (Transporter) transporters.get(i);
			for (int j = 0; j < stations.size(); j++) {
				TransporterJob tj = new TransporterJob(t, (WorkStation) stations.get(j));
				computeScore(tj);
				transporterjobs.add(tj);
			}
		}
		Collections.sort(transporterjobs);
		
		for (TransporterJob tj : transporterjobs) {
			if (transporters.isEmpty() || stations.isEmpty()) {
				break;
			}
			Transporter t = tj.transporter;
			WorkStation j = tj.job;
			if (!stations.contains(j) || !transporters.contains(t)) {
				continue;
			}
			
			if (t.task != null) {
				t.preempted = true;
				t.setPosition(t.getEnRoutePosition()[0], t.getEnRoutePosition()[1]);
				t.setJob(j);
				t.reActivate(new TimeSpan(0.0));
			}
			else {
				t.setJob(j);
				t.activate();
			}
			
			transporters.remove(t);
			stations.remove(j);
			
			if (myModel.idleTransporters.contains(t)) {
				myModel.idleTransporters.remove(t);
			}
			if (myModel.idleStations.contains(j)) {
				myModel.idleStations.remove(j);
			}
			
		}
	}
	
	private void init(ProcessQueue transporters, ProcessQueue stations){
		minWaitingTime = Double.MAX_VALUE;
		maxWaitingTime = 0.0;
		for (int i = 0; i < stations.size(); i++) {
			double waitingTime = ((WorkStation) stations.get(i)).getCurWaitTime();
//			System.out.println(waitingTime);
			if (waitingTime > maxWaitingTime) {
				maxWaitingTime = waitingTime;
			}
			if (waitingTime < minWaitingTime) {
				minWaitingTime = waitingTime;
			}
		}
		
		m = new HashMap<>();
		for (int i = 0; i < transporters.size(); i++) {
			double minDis = Double.MAX_VALUE;
			double maxDis = 0.0;
			Transporter t = (Transporter) transporters.get(i);

			for (int j = 0; j < stations.size(); j++) {
				WorkStation station = (WorkStation) stations.get(j);
				double dis = t.getDistance(station);
				if (t.state == Transporter.State.MOVING) {
					dis = Helper.computeDist(t.getEnRoutePosition()[0], t.getEnRoutePosition()[1], station.x, station.y);
				}
				
				if (dis > maxDis) {
					maxDis = dis;
				}
				if (dis < minDis) {
					minDis = dis;
				}
			}
			m.put(t, new double[] {minDis, maxDis});
		}
	}
	
	private void computeScore(TransporterJob tj) {
		Transporter t = tj.transporter;
		WorkStation j = tj.job;
		double trueDis = t.getDistance(j);
		if (t.state == Transporter.State.MOVING) {
			trueDis = Helper.computeDist(t.getEnRoutePosition()[0], t.getEnRoutePosition()[1], j.x, j.y);
		}
		
		double dis = (trueDis - m.get(t)[0]) / (m.get(t)[1] - m.get(t)[0]);
		double waitTime = (maxWaitingTime - j.getCurWaitTime()) / (maxWaitingTime - minWaitingTime);
		if (Double.isNaN(dis)) {
			dis = 0;
		}
		if (Double.isNaN(waitTime)) {
			waitTime = 0;
		}
		
		tj.score = 0.8 * dis + 0.2 * waitTime;
		System.out.println(dis);
		System.out.println(waitTime);
	}

}
