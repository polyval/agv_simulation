
public class TransporterJob implements Comparable<TransporterJob> {
	protected Transporter transporter;
	protected WorkStation job;
	protected double score;
	
	public TransporterJob(Transporter t, WorkStation j) {
		this.transporter = t;
		this.job = j;
	}

	@Override
	public int compareTo(TransporterJob o) {
		if (this.score > o.score) {
			return 1;
		}
		if (this.score < o.score) {
			return -1;
		}
		return 0;
	}

}
