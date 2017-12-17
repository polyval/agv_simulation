import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeSpan;

public class STDPTP_Transport_Strategy  extends ModelComponent implements TransportStrategy {
	
	private TransporterModel myModel;
	
	public STDPTP_Transport_Strategy(Model owner) {
		super(owner, "NVFTransportStrategy"); // make a ModelComponent
		myModel = (TransporterModel) owner;
	}

	@Override
	public void schedule(ProcessQueue transporters, ProcessQueue stations) {
		// 时间优先级
		for (int i = 0; i < stations.size() && transporters.size() > 0; i++) {
			WorkStation k;
			if ((k = (WorkStation) stations.get(i)).getCurWaitTime() > 80) {
				k.priority = true;
				Transporter m = (Transporter) transporters.first();
				Transporter n;
				for (int j = 1; j < transporters.size(); j++) {
					if ((n = (Transporter) transporters.get(j)).getDistance(k) < m.getDistance(k)) {
						m = n;
					}
				}
				myModel.idleTransporters.remove(m);
				myModel.idleStations.remove(k);
				m.setJob(k);
				m.activate();
			}
		}
		
		
		transporters = myModel.transporters;
		stations = myModel.idleStations;
		// get the first job of the queue
		WorkStation j = (WorkStation) stations.first();

		// get the first transporter of the queue
		Transporter t = (Transporter) transporters.first();

		// while there's a job and a transporter
		while ((t != null) && (j != null)) {
			if (t.state == Transporter.State.UNAVAILABLE || t.state == Transporter.State.EXECUTING
					|| (t.task != null && t.task.priority)) {
				t = (Transporter) transporters.succ(t);
				continue;
			}
			
			double curX;
			double curY;
			if (t.task != null) {
				double[] curPos = t.getEnRoutePosition();
				curX = curPos[0];
				curY = curPos[1];
			}
			else {
				curX = t.x;
				curY = t.y;
			}
			
			
			// 小车驱动, 找出所有未分配任务中离小车最近的任务
			for (int i = 1; i < stations.size(); i++) {
				if (Helper.computeDist(curX, curY,
						((WorkStation)stations.get(i)).x, ((WorkStation)stations.get(i)).y) 
						< Helper.computeDist(curX, curY, j.x, j.y)) {
					j = (WorkStation) stations.get(i);
				}
			}
			
			// 小车是否已分配任务
			if (t.task != null) {
				// 未分配的最近任务的距离是否比小车离分配给自己的任务距离更近
				if (Helper.computeDist(curX, curY, j.x, j.y) < Helper.computeDist(curX, curY, t.task.x, t.task.y)) {
					t.preempted = true;
					myModel.idleStations.insert(t.task);
					System.out.println(t.getName() + "取消当前任务" + t.task.getName() + "前往" + j.getName());
					System.out.println(t.getName() + "当前位置" + curX + " " + curY);
					t.setPosition(curX, curY);
					t.setJob(j);
					myModel.idleStations.remove(j);
					t.reActivate(new TimeSpan(0.0));
				}
			}
			
			else {
				t.setJob(j);
				myModel.idleTransporters.remove(t);
				myModel.idleStations.remove(j);
				t.activate();
			}
			
			// get next Job of the Job's queue
			j = (WorkStation) stations.first();

			// get next internal transporter of the transporter queue
			t = (Transporter) transporters.succ(t);
		}
	}

}
