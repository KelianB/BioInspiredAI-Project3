package pso;

import java.util.Arrays;
import java.util.Random;

import jssp.ProblemInstance;
import main.Config;
import utils.GanttChart;

/**
 * Particle Swarm Optimization algorithm class
 * @author Kelian Baert & Caroline de Pourtales
 */
public class PSOAlgorithm {
	// The problem instance this algorithm operates on
	private ProblemInstance problemInstance;
	
	// A random generator
	private Random random;
	
	// The particle swarm
	private Swarm swarm;
	
	// Keep track of ran iterations
	private int ranIterations = 0;
	
	// Store min and max velocity so they can be enforced during the particle updates
	private float vmin, vmax;

	// Inertia weight
	private float inertia, minInertia;
	// The step by which to linearly decrease inertia at each generation
	private float inertiaStep;
	
	// Local and global acceleration constants
	private float c1, c2;

	// Array of operation indices used for decoding to phenotype (saved so we don't have to recreate it each time)
	private Integer[] tempOperationOrder;
	
	/***
	 * Initialize a Particle Swarm Optimization algorithm.
	 * @param problemInstance - A JSSP problem instance
	 * @param config - A configuration object
	 */
	public PSOAlgorithm(ProblemInstance problemInstance, Config config) {
		this.problemInstance = problemInstance;
		this.random = new Random();
	
		tempOperationOrder = new Integer[getProblemInstance().getTotalOperations()];
		for(int i = 0; i < tempOperationOrder.length; i++)
			tempOperationOrder[i] = i;
		
		// Read fields from the config
		this.vmin = config.getFloat("vmin");
		this.vmax = config.getFloat("vmax");
		this.inertia = config.getFloat("initialInertia");
		this.minInertia = config.getFloat("minInertia");
		this.inertiaStep = config.getFloat("inertiaStep");
		this.c1 = config.getFloat("localAccelerationConstant");
		this.c2 = config.getFloat("globalAccelerationConstant");
		
		int swarmSize = config.getInt("swarmSize");
		float xmin = config.getFloat("xmin"), xmax = config.getFloat("xmax");
		
		// Initialize the swarm
		this.swarm = Swarm.randomSwarm(this, swarmSize, xmin, xmax, vmin, vmax);		
	}
	
	/**
	 * Run a single iteration of the Particle Swarm Optimization
	 */
	public void runIteration() {
		// Update the swarm's global best position and fitness
		getSwarm().updateGlobalBest();
	
		// Update all particles in the swarm
		for(Particle p : swarm.getParticles()) {
			p.update(swarm.getGlobalBestPosition(), inertia, c1, c2, vmin, vmax);
		}
		
		// Update inertia
		if(inertia > minInertia) {
			inertia -= inertiaStep;
			if(inertia < minInertia)
				inertia = minInertia;
		}
			
		ranIterations++;
	}
	
	/**
	 * Get the problem instance this algorithm operates on.
	 * @return the problem instance
	 */
	public ProblemInstance getProblemInstance() {
		return problemInstance;
	}
	
	/**
	 * Get the particle swarm in this Particle Swarm Optimization
	 * @return the swarm of particles
	 */
	public Swarm getSwarm() {
		return swarm;
	}
	
	/**
	 * Get the number of iterations that this algorithm has run.
	 * @return the number of iterations ran
	 */
	public int getRanIterations() {
		return ranIterations;
	}
	
	/**
	 * Print the current state of the algorithm
	 */
	public void printState() {
		Swarm s = getSwarm();
		System.out.println("\n############### Iteration " + ranIterations + " ###############");
		System.out.println("Inertia: " + inertia);
		System.out.println("Best makespan of swarm: " + (-s.getFittest().getFitness()) + " (average = " + (-s.getAverageFitness()) + ")");
		System.out.println("Best makespan achieved globally: " + (-s.getGlobalBestFitness()));
	}
	
	/**
	 * Get a random float in [0,1[ using this algorithm instance's random generator.
	 * @return a random float between 0 (inclusive) and 1 (exclusive)
	 */
	public float random() {
		return random.nextFloat();
	}
	
	/**
	 * Calculates the makespan for a given particle position.
	 * @param position - A position array from a particle
	 */
	public int computeMakespan(float[] position) {
		long start = System.nanoTime();
		long time = System.nanoTime();
		boolean logTimes = false;
		
		ProblemInstance pb = getProblemInstance();
		
		// Get the operation order (sort an array of operation indices according to the position values)
		Arrays.sort(tempOperationOrder, (i1, i2) -> Float.compare(position[i1], position[i2]));
		
		if(logTimes) {
			System.out.println("Getting operation order took " + (System.nanoTime() - time) / 1000000.0f + " ms");
			time = System.nanoTime();
		}
			
		// For each machine, build a list of jobs to work on (in order)
	
		int machines = pb.getOperationsPerJob();
		
		/*@SuppressWarnings("unchecked")
		List<Integer>[] machineOperations = new ArrayList[machines];
		for(int i = 0; i < machines; i++)
			machineOperations[i] = new ArrayList<Integer>();
		// Store the current operation index for each job
		int[] currentOperationIndices = new int[pb.getNumberOfJobs()]; 
		
		for(int operationIndex: tempOperationOrder) {
			int job = (int) (operationIndex / pb.getOperationsPerJob());
			int machine = pb.getMachine(job, currentOperationIndices[job]);
			machineOperations[machine].add(job);
			currentOperationIndices[job]++;
		}
		
		if(logTimes) {
			System.out.println("Building list of jobs per machine took " + (System.nanoTime() - time) / 1000000.0f + " ms");
			time = System.nanoTime();
		}
		
		// Calculate the end time for each machine
		
		// Store current time for each machine
		int[] machineTimes = new int[machines];
		
		// Store current time of each job
		int[] jobTimes = new int[pb.getNumberOfJobs()];
		
		int makespan = 0;
		
		for(int m = 0; m < machines; m++) {
			for(int job : machineOperations[m]) {
				int duration = pb.getOperationOnMachine(job, m).getDuration();
				int operationStartTime = Math.max(jobTimes[job], machineTimes[m]);
				int endTime = operationStartTime + duration;
				machineTimes[m] = endTime;
				jobTimes[job] = endTime;
				if(endTime > makespan)
					makespan = endTime;
			}
		}
		
		if(logTimes) {
			System.out.println("Computing ending times took " + (System.nanoTime() - time) / 1000000.0f + " ms");
			System.out.println("Total makespan compute time: " + (System.nanoTime() - start) / 1000000.0f + " ms");
		}*/
		
		
		// Store the current operation index for each job
		int[] currentOperationIndices = new int[pb.getNumberOfJobs()]; 
		
		// Store current time for each machine
		int[] machineTimes = new int[machines];
				
		// Store current time of each job
		int[] jobTimes = new int[pb.getNumberOfJobs()];
		
		int makespan = 0;
		
		for(int operationIndex: tempOperationOrder) {
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
	
	
	public GanttChart createGanttChart(float[] position) {
		ProblemInstance pb = getProblemInstance();
				
		// Get the operation order (sort an array of operation indices according to the position values)
		Integer[] operationOrder = new Integer[pb.getTotalOperations()];
		for(int i = 0; i < operationOrder.length; i++)
			operationOrder[i] = i;
		Arrays.sort(operationOrder, (i1, i2) -> Float.compare(position[i1], position[i2]));
		
		int machines = pb.getOperationsPerJob();
		
		// Store the current operation index for each job
		int[] currentOperationIndices = new int[pb.getNumberOfJobs()]; 
		
		// Store current time for each machine
		int[] machineTimes = new int[machines];
				
		// Store current time of each job
		int[] jobTimes = new int[pb.getNumberOfJobs()];
		
		GanttChart gc = new GanttChart(machines);
		
		for(int operationIndex: operationOrder) {
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
