import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

public class WorkStation extends SimProcess{
	
	protected double x;
	protected double y;

	private TransporterModel myModel;
	private TransportControl tc;
	
	private TimeInstant startWait;
	private TimeInstant endWait;
	
	public WorkStation(Model owner, String name, boolean showInTrace, TransportControl tc,
			double x, double y) {
		super(owner, name, showInTrace);
		myModel = (TransporterModel) owner;
		this.tc = tc;
		this.x = x;
		this.y = y;
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		while (true) {
			// 开始加工
			hold(new TimeSpan(getProcessingTime()));	
			myModel.idleStations.insert(this);
			startWait = presentTime();
			System.out.println(getName() + "需要换件 " + presentTime());
			tc.activate();
			// 等待车辆通知换件成功
			passivate();
			
			myModel.waitTimeHistogram.update(getWaitTime());
		}
	}
	
	public double getProcessingTime() {
		ContDistUniform processingTime = new ContDistUniform(myModel, "processingTimeStream", 5.0, 15.0, true, false);
		return processingTime.sample();
	}
	
	public void endWait() {
		endWait = presentTime();
	}
	
	public double getWaitTime() {
		if (startWait != null && endWait != null) 
			return TimeOperations.diff(startWait, endWait).getTimeAsDouble();
		else
			return Double.NaN;
	}
}
