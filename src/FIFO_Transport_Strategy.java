import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.ProcessQueue;


public class FIFO_Transport_Strategy extends ModelComponent implements
		TransportStrategy {
	
	private TransporterModel myModel;
	
	public FIFO_Transport_Strategy(Model owner) {

		super(owner, "FIFOTransportStrategy"); // make a ModelComponent
		myModel = (TransporterModel) owner;
	}


	@Override
	public void schedule(ProcessQueue<?> transporters, ProcessQueue<?> stations) {

		// get the first job of the queue
		WorkStation j = (WorkStation) stations.first();

		// get the first transporter of the queue
		Transporter t = (Transporter) transporters.first();

		// while there's a job and a transporter
		while ((t != null) && (j != null)) {
			// make a new TransporterJob
			t.setJob(j);
			myModel.idleTransporters.remove(t);
			myModel.idleStations.remove(j);
			t.activate();

			// get next Job of the Job's queue
			j = (WorkStation) stations.first();

			// get next internal transporter of the transporter queue
			t = (Transporter) transporters.first();

		}
	}

}