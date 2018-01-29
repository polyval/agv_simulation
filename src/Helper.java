import java.util.List;

public class Helper {
	
	public static double computeDist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	public static double computeCost(Transporter vehicle, List<WorkStation> stations, int processTime) {
		double posX = vehicle.x;
		double posY = vehicle.y;
		
		double cost = 0;
		double time = vehicle.presentTime().getTimeAsDouble();
//		if (vehicle.state == Transporter.State.EXECUTING ||
//				vehicle.state == Transporter.State.MOVING) {
//			posX = vehicle.task.x;
//			posY = vehicle.task.y;
//			time += computeDist(vehicle.x, posX, vehicle.y, posY) / vehicle.speed;
//			if (vehicle.task.nextJobTime.getTimeAsDouble() > time) {
//				time = vehicle.task.nextJobTime.getTimeAsDouble();
//			}
//			time += processTime;
//		}
		for (WorkStation s : stations) {
			double dis = computeDist(posX, posY, s.x, s.y);
			double jobTime = s.nextJobTime.getTimeAsDouble();
			// 到达任务的时间
			time += dis / vehicle.speed;
			// 到达时任务是否已经开始
			if (jobTime > time) {
				time = jobTime;
			}
			else {
				cost += (time - jobTime); 
			}
			// 换件的时间
			time += processTime;
			posX = s.x;
			posY = s.y;
		}
		
		return cost;
	}
}
