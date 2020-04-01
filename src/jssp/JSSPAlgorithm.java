package jssp;

import java.util.Random;

import utils.GanttChart;

/**
 * Class for stuff that is common for ACO and PSO algorithms
 * @author Kelian Baert & Caroline de Pourtales
 */
public abstract class JSSPAlgorithm {
	// The problem instance this algorithm operates on
	private ProblemInstance problemInstance;
	
	// A random generator
	private Random random;
	
	/**
	 * Init the algorithm.
	 * @param problemInstance - A JSSP problem instance
	 */
	public JSSPAlgorithm(ProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.random = new Random();
	}
	
	/**
	 * Get the number of iterations that this algorithm has run.
	 * @return the number of iterations ran
	 */
	public abstract int getRanIterations();

	/**
	 * Run a single iteration of the algorithm.
	 */
	public abstract void runIteration();
	
	/**
	 * Print the current state of the algorithm.
	 */
	public abstract void printState();
	
	/**
	 * Get the best order of operations found by this algorithm.
	 * @return an array of operation indices to run in order
	 */
	public abstract Integer[] getBestSolution();
	
	/**
	 * Get a random float in [0,1[ using this algorithm instance's random generator.
	 * @return a random float between 0 (inclusive) and 1 (exclusive)
	 */
	public float random() {
		return getRandom().nextFloat();
	}
	
	/**
	 * Get the random generator used by this algorithm.
	 * @return a random generator
	 */
	public Random getRandom() {
		return random;
	}
	
	/**
	 * Get the problem instance this algorithm operates on.
	 * @return the problem instance
	 */
	public ProblemInstance getProblemInstance() {
		return problemInstance;
	}
	
	/**
	 * Calculates the makespan for a given order of operations.
	 * @param operationOrder - An array containing the indices of the operations to run
	 */
	public int computeMakespan(Integer[] operationOrder) {
		long start = System.nanoTime();
		long time = System.nanoTime();
		boolean logTimes = false;
		
		ProblemInstance pb = getProblemInstance();
		
		if(logTimes) {
			System.out.println("Getting operation order took " + (System.nanoTime() - time) / 1000000.0f + " ms");
			time = System.nanoTime();
		}
				
		int machines = pb.getOperationsPerJob();

		// Store the current operation index for each job
		int[] currentOperationIndices = new int[pb.getNumberOfJobs()]; 
		
		// Store current time for each machine
		int[] machineTimes = new int[machines];
				
		// Store current time of each job
		int[] jobTimes = new int[pb.getNumberOfJobs()];
		
		int makespan = 0;
		
		for(int operationIndex : operationOrder) {
			int job = (int) (operationIndex / pb.getOperationsPerJob());
			int machine = pb.getMachine(job, currentOperationIndices[job]);
			
			int duration = pb.getOperationOnMachine(job, machine).getDuration();
			int operationStartTime = Math.max(jobTimes[job], machineTimes[machine]);
			int endTime = operationStartTime + duration;
			machineTimes[machine] = endTime;
			jobTimes[job] = endTime;
			if(endTime > makespan)
				makespan = endTime;
			
			currentOperationIndices[job]++;
		}
		
		if(logTimes) {
			System.out.println("Second step took " + (System.nanoTime() - time) / 1000000.0f + " ms");
			System.out.println("Total makespan compute time: " + (System.nanoTime() - start) / 1000000.0f + " ms");
			System.out.println("-");
		}
	
		return makespan;
	}
	
	/**
	 * Create the Gantt chart for a given order of operations.
	 * @param operationOrder - An array containing the indices of the operations to run
	 */
	public GanttChart createGanttChart(Integer[] operationOrder) {
		ProblemInstance pb = getProblemInstance();
		
		int machines = pb.getOperationsPerJob();
		
		// Store the current operation index for each job
		int[] currentOperationIndices = new int[pb.getNumberOfJobs()]; 
		
		// Store current time for each machine
		int[] machineTimes = new int[machines];
				
		// Store current time of each job
		int[] jobTimes = new int[pb.getNumberOfJobs()];
		
		GanttChart gc = new GanttChart(machines);
		
		for(int operationIndex : operationOrder) {
			int job = (int) (operationIndex / pb.getOperationsPerJob());
			int machine = pb.getMachine(job, currentOperationIndices[job]);
			
			int duration = pb.getOperationOnMachine(job, machine).getDuration();
			int operationStartTime = Math.max(jobTimes[job], machineTimes[machine]);
			int endTime = operationStartTime + duration;
			gc.addTask(machine, job, operationStartTime, duration);
			machineTimes[machine] = endTime;
			jobTimes[job] = endTime;
			
			currentOperationIndices[job]++;
		}
	
		return gc;
	}
}
