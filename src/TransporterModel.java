import java.util.Map;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.Histogram;
import desmoj.core.util.AccessPoint;
import desmoj.core.util.Parameterizable;

public class TransporterModel extends Model implements Parameterizable{

	
	protected ProcessQueue<Transporter> transporters;
	protected ProcessQueue<Transporter> idleTransporters;
	protected ProcessQueue<WorkStation> stations;
	protected ProcessQueue<WorkStation> idleStations;
	protected TransportStrategy ts;
	protected TransportControl tc;
	
	protected double[] storage_position;
	
	protected Histogram waitTimeHistogram;
 	
	public TransporterModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, AccessPoint> createParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void doInitialSchedules() {
		int[][] station_positions = {{0, 0}, {2, 0}, {4, 0}, {6, 0}, {8, 0},
									{0, 3}, {2, 3}, {4, 3}, {6, 3}, {8, 3}};

		for (int i = 0; i < station_positions.length; i++) {
			WorkStation s = new WorkStation(this, "机床", true, tc, station_positions[i][0], station_positions[i][1]);
			stations.insert(s);
			idleStations.insert(s);
		}
		
		int[][] vehicle_positions = {{1, 1}, {4, 2}};
		for (int i = 0; i < vehicle_positions.length; i++) {
			Transporter t = new Transporter(this, "小车", true, 10, tc, vehicle_positions[i][0], vehicle_positions[i][1], 1);
			transporters.insert(t);
			idleTransporters.insert(t);
		}
		tc.activate();
	}

	@Override
	public void init() {
		transporters = new ProcessQueue<>(this, "Vehicle Queue", true, false);
		idleTransporters = new ProcessQueue<>(this, "idle Vehicle Queue", true, false);
		stations = new ProcessQueue<>(this, "Station Queue", true, false);
		idleStations = new ProcessQueue<>(this, "idle Station Queue", true, false);
		ts = new NVF_Transport_Strategy(this);
		tc = new TransportControl(this, "Dispatcher", true, ts);
		storage_position = new double[] {10, 2};
		
		waitTimeHistogram = new Histogram(this, "等待时间", 0, 100, 25, true, false);
	}
	
	public double getLoadingTime() {
		return 10;
	}
	
	public static void main(String[] args) {
        Experiment.setReferenceUnit(java.util.concurrent.TimeUnit.SECONDS);
        Experiment experiment = new Experiment("Transporter Model");
        TransporterModel m = new TransporterModel(null, "TransporterModel", true, true);
        
        m.connectToExperiment(experiment);
        // set trace
 		experiment.tracePeriod(new TimeInstant(0), new TimeInstant(100));

 		// now set the time this simulation should stop at 
 		// let him work 1500 Minutes
 		experiment.stop(new TimeInstant(1500));
 		experiment.setShowProgressBar(false);

 		// start the Experiment with start time 0.0
 		experiment.start();

 		// --> now the simulation is running until it reaches its ending criteria
 		// ...
 		// ...
 		// <-- after reaching ending criteria, the main thread returns here

 		// print the report about the already existing reporters into the report file
 		experiment.report();

 		// stop all threads still alive and close all output files
 		experiment.finish();
	}

}
