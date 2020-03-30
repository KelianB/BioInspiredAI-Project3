package jssp;

public class Operation {
	private int job, machine, duration;
	
	public Operation(int job, int machine, int duration) {
		this.job = job;
		this.machine = machine;
		this.duration = duration;
	}
	
	public int getJob() {
		return job;
	}
	
	public int getMachine() {
		return machine;
	}
	
	public int getDuration() {
		return duration;
	}
	
	@Override
	public String toString() {
		return machine + " (" + duration + ")";
	}
}