import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;

/***
 * Simple additive weight method
 * @author polyval
 *
 */
public class SAWM_Transport_Strategy  extends ModelComponent implements TransportStrategy {
	
	private TransporterModel myModel;
	private double minWaitingTime;
	private double maxWaitingTime;
	private Map<Transporter, double[]> m;
	
	public SAWM_Transport_Strategy(Model owner) {
		super(owner, "SAWMTransportStrategy");
		myModel = (TransporterModel) owner;
	}

	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		if (transporters.isEmpty() || stations.isEmpty()) {
			return;
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
			if (myModel.idleStations.isEmpty() || myModel.idleTransporters.isEmpty()) {
				break;
			}
			Transporter t = tj.transporter;
			WorkStation j = tj.job;
			if (!myModel.idleStations.contains(j) || !myModel.idleTransporters.contains(t)) {
				continue;
			}
			t.setJob(j);
			myModel.idleTransporters.remove(t);
			myModel.idleStations.remove(j);
			t.activate();
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
				double dis = t.getDistance((WorkStation) stations.get(j));
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
		double dis = (t.getDistance(j) - m.get(t)[0]) / (m.get(t)[1] - m.get(t)[0]);
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
