import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class TransportControl extends SimProcess{
	
	private TransporterModel myModel;
	private TransportStrategy ts;
	
	public TransportControl(Model owner, String name, boolean showInTrace,
			TransportStrategy ts) {
		super(owner, name, showInTrace);
		
		myModel = (TransporterModel) owner;
		this.ts = ts;
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		while (true) {
			if (myModel.transporters.length() > 0 && myModel.stations.length() > 0) {
				ts.schedule(myModel.idleTransporters, myModel.idleStations);
			}
			passivate();
		}
	}
	
}
