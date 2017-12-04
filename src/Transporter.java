import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class Transporter extends SimProcess{
	
	private double x;
	private double y;
	private double speed;
	
	private int capacity;
	private int raw;
	private int processed = 0;
	
	private WorkStation task = null;
	private TransporterModel myModel;
	private TransportControl tc;
	
	private double travelDistance = 0.0;
	
	
	public Transporter(Model owner, String name, boolean showInTrace, int cap,
			TransportControl tc, double x, double y, double speed) {
		super(owner, name, showInTrace);
		
		this.capacity = cap;
		raw = capacity;
		
		myModel = (TransporterModel) owner;
		this.tc = tc;
		this.x = x;
		this.y = y;
		this.speed = speed;
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		while (true) {
			// ȥ�������
			if (task != null) {
				System.out.println(getName() + " ��ʼǰ�� " + task.getName()+ " " + presentTime() + " " + x + " " + y);
				driveToStation();
				System.out.println(getName() + " ���� " + task.getName()+ " " + presentTime() + " " + x + " " + y);
				replenish();
				//�����ɹ�
				task.endWait();
				task.activate();
				System.out.println(task.getName() + " �����ɹ� " +
					presentTime());
				// �������С������
				task = null;
				if (raw == capacity || processed == capacity) {
					reload();
				}
			}
			// �ȴ�����
			else {
				myModel.idleTransporters.insert(this);
				tc.activateAfter(this);
				passivate();
			}
		}
	}
	
	public void setJob(WorkStation station) {
		task = station;
	}
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getDistance(WorkStation station) {
		return Helper.computeDist(x, y, station.x, station.y);
	}
	
	private void reload() throws SuspendExecution {
		System.out.println(getName() + " ���� " + presentTime());
		hold(new TimeSpan(getReloadTime()));
		setPosition(myModel.storage_position[0], myModel.storage_position[1]);
		raw = capacity;
		processed = 0;
		System.out.println(getName() + " ���ϳɹ� " + presentTime() + " " + x + " " + y);
		travelDistance += Helper.computeDist(x, y, myModel.storage_position[0], myModel.storage_position[1]);
	}
	
	private double getReloadTime() {
		return 10;
	}
	
	private void driveToStation() throws SuspendExecution {
		double distance = getDistance(task);
		hold(new TimeSpan(distance / speed));
		System.out.println(getName() + "��ʻʱ�� " + distance / speed);
		// ͣ������
		setPosition(task.x, task.y);
		travelDistance += distance;
	}
	
	private void replenish() throws SuspendExecution{
		hold(new TimeSpan(myModel.getLoadingTime()));
		raw--;
		processed++;
	}
}
