import java.util.LinkedList;
import java.util.List;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.exception.InterruptException;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

public class Transporter extends SimProcess{
	
	protected double x;
	protected double y;
	protected double speed;
	
	private int capacity;
	private int raw;
	private int processed = 0;
	protected State state = State.IDLE;
	
	protected WorkStation task = null;
	protected List<WorkStation> taskSequence = new LinkedList<>();
	private TransporterModel myModel;
	private TransportControl tc;
	
	protected TimeInstant startTime;
	protected boolean preempted = false;
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
			preempted = false;
			// ȥ�������
			if (task != null) {
				startTime = presentTime();
				sendTraceNote(getName() + " ��ʼǰ�� " + task.getName()); 
				System.out.println(getName() + " ��ʼǰ�� " + task.getName()+ " " + presentTime() + " " + x + " " + y);
				driveToStation();
				// ����ռ����������
				if (preempted) {
					continue;
				}
				
				sendTraceNote(getName() + " ���� " + task.getName());
				System.out.println(getName() + " ���� " + task.getName()+ " " + presentTime() + " " + x + " " + y);
				// ���Ƿ���Ҫ�ȴ������ӹ����
				waitStation();
				// ��ʼ����
				replenish();
				//�����ɹ�
				task.endWait();
				task.activateAfter(this);
				
				sendTraceNote(getName() + " ������ " + task.getName() + " �����ɹ� ");
				System.out.println(task.getName() + " �����ɹ� " +
					presentTime());
				// �������С������
				removeFromSequence();
				task = null;
				if (raw == capacity || processed == capacity) {
					reload();
				}
			}
			// �ȴ�����
			else {
				myModel.idleTransporters.insert(this);
				tc.activate();
				passivate();
			}
		}
	}
	
	public void waitStation() {
		if (this.task == null || 
				TimeInstant.isBefore(this.task.nextJobTime, presentTime())) {
			return;
		}
		
		state = State.EXECUTING;
		try {
			sendTraceNote(getName() + " �ȴ����� " + task.getName() + " �ӹ� ");
			hold(this.task.nextJobTime);
		} catch (InterruptException | SuspendExecution e) {
			e.printStackTrace();
		}
	}
	
	public void removeFromSequence() {
		if (taskSequence.contains(task)) {
			taskSequence.remove(task);
		}
	}
	
	public void setJob(WorkStation station) {
		task = station;
	}
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double[] getEnRoutePosition() {
		if (task != null) {
			double dist = getEnRouteDis();
			if (dist == 0) {
				return new double[] {x, y};
			}
			double vx = (task.x - x) / getDistance(task);
			double vy = (task.y - y) / getDistance(task);
			return new double[] {x + dist * vx, y + dist * vy};
		}
		else {
			return new double[] {x, y};
		}
	}
	
	public double getDistance(WorkStation station) {
		return Helper.computeDist(x, y, station.x, station.y);
	}
	
	private double getEnRouteDis() {
		double enRouteTime = TimeOperations.diff(startTime, presentTime()).getTimeAsDouble();
		if (startTime.equals(presentTime())) {
			return 0;
		}
		return enRouteTime * speed;
	}
	
	private void reload() throws SuspendExecution {
		state = State.UNAVAILABLE;
		System.out.println(getName() + " ���� " + presentTime());
		hold(new TimeSpan(getReloadTime()));
		setPosition(myModel.storage_position[0], myModel.storage_position[1]);
		raw = capacity;
		processed = 0;
		System.out.println(getName() + " ���ϳɹ� " + presentTime() + " " + x + " " + y);
		double dis = Helper.computeDist(x, y, myModel.storage_position[0], myModel.storage_position[1]);
		travelDistance += dis;
		myModel.totalTravelDistance += dis;
		if (Double.isNaN(myModel.totalTravelDistance)) {
			System.out.println(presentTime() + "error");
		}
		state = State.IDLE;
	}
	
	private double getReloadTime() {
		return 10;
	}
	
	private void driveToStation() throws SuspendExecution {
		state = State.MOVING;
		double distance = getDistance(task);
		// preemptedֻ�������ʱ��ᱻ���ı�
		hold(new TimeSpan(distance / speed));
		if (preempted) {
			myModel.totalTravelDistance += getEnRouteDis();
			return;
		}
		
		System.out.println(getName() + "��ʻʱ�� " + distance / speed);
		setPosition(task.x, task.y);
		travelDistance += distance;
		myModel.totalTravelDistance += distance;
		if (Double.isNaN(myModel.totalTravelDistance)) {
			System.out.println(presentTime() + "error");
		}
	}
	
	private void replenish() throws SuspendExecution{
		state = State.EXECUTING;
		hold(new TimeSpan(myModel.getLoadingTime()));
		raw--;
		processed++;
		state = State.IDLE;
	}
	
	public enum State {
		EXECUTING,
		IDLE,
		MOVING,
		UNAVAILABLE
	}
}
