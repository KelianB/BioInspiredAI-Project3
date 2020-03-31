package pso;

import java.util.Arrays;

import jssp.ProblemInstance;
import utils.CachedValue;
import utils.GanttChart;

public class Particle {
	// Store a reference to the PSO
	private PSOAlgorithm alg;
	
	// Fitness value, only refreshed when necessary
	private CachedValue<Integer> fitness;
	
	// Position and velocity in the context of Particle Swarm Optimization
	private float[] position;
	private float[] velocity;
	
	// Store local best position
	private float[] localBestPosition;
	private int localBestFitness;
	
	// temporary array saved so we don't have to recreate it each time
	private Integer[] tempOperationOrder;
	
	public Particle(PSOAlgorithm alg) {
		this.alg = alg;	
		
		position = new float[alg.getProblemInstance().getNumberOfOperations()];
		velocity = new float[position.length];
		
		localBestPosition = new float[position.length];
		localBestFitness = Integer.MIN_VALUE;
		
		fitness = new CachedValue<Integer>(() -> -computeMakespan());
		
		tempOperationOrder = new Integer[position.length];
		for(int i = 0; i < tempOperationOrder.length; i++)
			tempOperationOrder[i] = i;
		
	}

	/**
	 * Get the fitness value of this particle.
	 * @return the last fitness value in cache
	 */
	public int getFitness() {
		return fitness.getValue();
	}
	
	/**
	 * Calculates the makespan
	 */
	public int computeMakespan() {
		long start = System.nanoTime();
		long time = System.nanoTime();
		boolean logTimes = false;
		
		
		ProblemInstance pb = alg.getProblemInstance();
		
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
	
		return makespan;
	}
	
	

	/**
	 * Updates this particle.
	 * @param globalBestPosition - The best position achieved by the swarm (highest fitness)
	 * @param inertia - The inertia weight parameter
	 * @param c1 - The acceleration constant that pulls this particle toward its local best
	 * @param c2 - The acceleration constant that pulls this particle toward the global best
	 * @param vmin - The minimum velocity
	 * @param vmax - The maximum velocity
	 */
	public void update(float[] globalBestPosition, float inertia, float c1, float c2, float vmin, float vmax) {
		for(int j = 0; j < position.length; j++) {
			// Calculate new velocity
			float v = inertia * velocity[j] + 
				c1 * alg.random() * (localBestPosition[j]  - position[j]) +
				c2 * alg.random() * (globalBestPosition[j] - position[j]);
			// Keep velocity in bounds
			velocity[j] = Math.min(vmax, Math.max(vmin, v));
			// Update position
			position[j] += velocity[j];
		}
		fitness.needsUpdating();
		updateLocalBest();
	}
	
	/**
	 * Get the best fitness achieved by this particle
	 * @return the best local fitness
	 */
	protected int getLocalBestFitness() {
		return localBestFitness;
	}

	/**
	 * Get the position for the best fitness achieved by this particle
	 * @return the best local position
	 */
	protected float[] getLocalBestPosition() {
		return localBestPosition;
	}
	
	/**
	 * Updates the local best for this particle, if the current fitness is better than it.
	 */
	private void updateLocalBest() {
		if(getFitness() > localBestFitness) {
			localBestFitness = getFitness();
			for(int j = 0; j < position.length; j++)
				localBestPosition[j] = position[j];
		}
	}
	
	/**
	 * Create a random particle with the given ranges of position and velocities.
	 * @param alg - A reference to the PSO algorithm this particle belongs to
	 * @param xmin - The lower bound of the position range
	 * @param xmax - The upper bound of the position range
	 * @param vmin - The lower bound of the velocity range
	 * @param vmax - The upper bound of the velocity range
	 * @return a Particle
	 */
	public static Particle randomParticle(PSOAlgorithm alg, float xmin, float xmax, float vmin, float vmax) {
		Particle p = new Particle(alg);
		
		// Random positions in given range
		for(int i = 0; i < p.position.length; i++)
			p.position[i] = xmin + alg.random() * (xmax - xmin);
		
		// Random velocities in given range
		for(int i = 0; i < p.velocity.length; i++)
			p.velocity[i] = vmin + alg.random() * (vmax - vmin);
		
		p.fitness.needsUpdating();
		p.updateLocalBest();
		
		return p;
	}
}