import java.util.Random;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.ContDist;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

public class WorkStation extends SimProcess{
	
	protected double x;
	protected double y;
	protected boolean priority = false;
	protected TimeInstant nextJobTime = new TimeInstant(0.0);

	private TransporterModel myModel;
	private TransportControl tc;
	
	private Random random = new Random();
	private ContDist processingTime;
	private TimeInstant startWait = new TimeInstant(0.0);
	private TimeInstant endWait;
	
	public WorkStation(Model owner, String name, boolean showInTrace, TransportControl tc,
			double x, double y) {
		super(owner, name, showInTrace);
		myModel = (TransporterModel) owner;
		this.tc = tc;
		this.x = x;
		this.y = y;
		processingTime = new ContDistNormal(myModel, "processingTimeStream", 30, 3, true, false);
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		while (true) {
			myModel.waitTimeHistogram.update(getWaitTime());
			myModel.finishedTask++;
			priority = false;
			// 开始加工
			sendTraceNote(getName() + "开始加工");
			
			TimeSpan processTime = new TimeSpan(getProcessingTime());
			nextJobTime = TimeOperations.add(presentTime(), processTime);
			hold(processTime);
			myModel.idleStations.insert(this);
			startWait = presentTime();
			sendTraceNote(getName() + "需要换件");
			System.out.println(getName() + "需要换件 " + presentTime());
			tc.activate();
			// 等待车辆通知换件成功
			passivate();
		}
	}
	
	public double getProcessingTime() {
//		return 15 + random.nextInt(20);
		return processingTime.sample();
	}
	
	public void endWait() {
		endWait = presentTime();
	}
	
	public double getCurWaitTime() {
		return TimeOperations.diff(startWait, presentTime()).getTimeAsDouble();
	}
	
	public double getWaitTime() {
		if (startWait != null && endWait != null) 
			return TimeOperations.diff(startWait, endWait).getTimeAsDouble();
		else
			return Double.NaN;
	}
}
