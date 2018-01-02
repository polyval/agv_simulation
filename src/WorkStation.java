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
	protected boolean priority = false;

	private TransporterModel myModel;
	private TransportControl tc;
	
	private ContDistUniform processingTime;
	private TimeInstant startWait = new TimeInstant(0.0);
	private TimeInstant endWait;
	
	public WorkStation(Model owner, String name, boolean showInTrace, TransportControl tc,
			double x, double y) {
		super(owner, name, showInTrace);
		myModel = (TransporterModel) owner;
		this.tc = tc;
		this.x = x;
		this.y = y;
		processingTime = new ContDistUniform(myModel, "processingTimeStream", 15.0, 15.0, true, false);
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		while (true) {
			myModel.waitTimeHistogram.update(getWaitTime());
			myModel.finishedTask++;
			priority = false;
			// ��ʼ�ӹ�
			sendTraceNote(getName() + "��ʼ�ӹ�");
			
			hold(new TimeSpan(getProcessingTime()));
			myModel.idleStations.insert(this);
			startWait = presentTime();
			sendTraceNote(getName() + "��Ҫ����");
			System.out.println(getName() + "��Ҫ���� " + presentTime());
			tc.activate();
			// �ȴ�����֪ͨ�����ɹ�
			passivate();
		}
	}
	
	public double getProcessingTime() {
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
