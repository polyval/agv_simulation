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
		for (WorkStation s : stations) {
			double dis = computeDist(posX, posY, s.x, s.y);
			double jobTime = s.nextJobTime.getTimeAsDouble();
			// ���������ʱ��
			time += dis / vehicle.speed;
			// ����ʱ�����Ƿ��Ѿ���ʼ
			if (jobTime > time) {
				time = jobTime;
			}
			else {
				cost += (time - jobTime); 
			}
			// ������ʱ��
			time += processTime;
			posX = s.x;
			posY = s.y;
		}
		
		return cost;
	}
}
