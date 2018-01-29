import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	protected double totalTravelDistance = 0.0;
	protected int finishedTask = 0;
 	
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
		int[][] station_positions = {{0, 0}, {0, 3},{2, 0}, {2, 3}, {4, 0}, {4, 3}, {6, 0},{6, 3}, {8, 0},
									  {8, 3},{10,0},{10,3},{12,0},{12,3},{14,0},{14,3},{16,0}, {16,3},
									/**{18,0},{20,0},{22,0},{24,0}, {18,3},{20,3},{22,3},{24,3}**/};

		for (int i = 0; i < station_positions.length; i++) {
			WorkStation s = new WorkStation(this, "机床", true, tc, station_positions[i][0], station_positions[i][1]);
			stations.insert(s);
			idleStations.insert(s);
		}
		
		int[][] vehicle_positions = {{1, 1}, {4, 2}, {6, 2}, {8,2}/****/};
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
		tc = new TransportControl(this, "Dispatcher", true, ts);
		storage_position = new double[] {18, 2};
		
		waitTimeHistogram = new Histogram(this, "等待时间", 0, 200, 25, true, false);
	}
	
	public void setStrategy(TransportStrategy ts) {
		this.ts = ts;
	}
	
	public int getLoadingTime() {
		return 15;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        Map<String, Class> strategy_map = new LinkedHashMap<>();
        strategy_map.put("FIFO", Class.forName("FIFO_Transport_Strategy"));
//        strategy_map.put("NVF", Class.forName("NVF_Transport_Strategy"));
//        strategy_map.put("NVFTP", Class.forName("NVFTP_Transport_Strategy"));
        strategy_map.put("STD", Class.forName("STD_Transport_Strategy"));
        strategy_map.put("STDP", Class.forName("STDP_Transport_Strategy"));
//        strategy_map.put("STDPTP", Class.forName("STDPTP_Transport_Strategy"));
        strategy_map.put("DSTDPTP", Class.forName("DSTDPTP_Transport_Strategy"));
//        strategy_map.put("SAWM", Class.forName("SAWM_Transport_Strategy"));
//        strategy_map.put("SAWM1", Class.forName("SAWM_Transport_Strategy2"));
//        strategy_map.put("SAWMP", Class.forName("SAWMP_Transport_Strategy"));
//        strategy_map.put("SAWMZ", Class.forName("SAWM_Zscore_Transport_Strategy"));
//        strategy_map.put("DSAWM", Class.forName("DSAWM_Transport_Strategy"));
        strategy_map.put("GRASP", Class.forName("GRASP_Transport_Strategy"));
        strategy_map.put("Block_TSP", Class.forName("Block_TSP_Transport_Strategy"));
        
        List<String> strategies = new ArrayList<>();
        List<Double> avgWaitingTime = new ArrayList<>();
        List<Double> maxWaitingTime = new ArrayList<>();
        List<Double> distance = new ArrayList<>();
        List<Integer> tasks = new ArrayList<>();
        		
        String variables = null;
        for (String key : strategy_map.keySet()) {
        	Experiment.setReferenceUnit(java.util.concurrent.TimeUnit.SECONDS);
        	Experiment experiment = new Experiment("Transporter Model");
        	TransporterModel m = new TransporterModel(null, "TransporterModel", true, true);
        	Class<?> cl = strategy_map.get(key);
        	Constructor<?> cons = cl.getConstructor(new Class[] {Model.class});
        	TransportStrategy ts = (TransportStrategy) cons.newInstance(m);
        	
            m.setStrategy(ts);
            
            m.connectToExperiment(experiment);
            // set trace
     		experiment.tracePeriod(new TimeInstant(0), new TimeInstant(1000));

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
//     		System.out.println(m.totalTravelDistance);
//     		System.out.println(m.finishedTask);
     		strategies.add(key);
     		avgWaitingTime.add(m.waitTimeHistogram.getMean());
     		maxWaitingTime.add(m.waitTimeHistogram.getMaximum());
     		distance.add(m.totalTravelDistance);
     		tasks.add(m.finishedTask);
     		
     		// print the report about the already existing reporters into the report file
     		experiment.report();

     		// stop all threads still alive and close all output files
     		experiment.finish();
     		
     		variables = "V" + m.transporters.size() + "S" + m.stations.size() +   "load_" + 
					(int)m.getLoadingTime() + "process_" + (int)m.stations.get(0).getProcessingTime();
        }
        
        System.out.println(avgWaitingTime.stream().map(Object::toString)
                .collect(Collectors.joining(" ")));
        System.out.println(maxWaitingTime.stream().map(Object::toString)
                .collect(Collectors.joining(" ")));
        System.out.println(distance.stream().map(Object::toString)
                .collect(Collectors.joining(" ")));
        System.out.println(tasks.stream().map(Object::toString)
                .collect(Collectors.joining(" ")));
        
        
        // 调用Python画图程序
//        String[] command = new String[] {"py", "plot.py", String.join(" ", strategies),
//        		avgWaitingTime.stream().map(Object::toString).collect(Collectors.joining(" ")),
//        		maxWaitingTime.stream().map(Object::toString).collect(Collectors.joining(" ")),
//        		distance.stream().map(Object::toString).collect(Collectors.joining(" ")),
//        		tasks.stream().map(Object::toString).collect(Collectors.joining(" ")),
//        		variables};
//        
//        try {
//			Process pr = Runtime.getRuntime().exec(command);
//			
//			// 打印输出
//			BufferedReader in = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
//            
//            try {
//				pr.waitFor();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
