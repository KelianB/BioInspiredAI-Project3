package aco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jssp.ProblemInstance;
import utils.RouletteWheel;

/**
 * Represents an Ant in the context of Ant Colony Optimization
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Ant {
	// Store a reference to the ACO
	private ACOAlgorithm alg;
	
	// Current makespan of the ant, updated when operations are added to the schedule
	private int makespan;
	
	// The current schedule (rebuilt at every generation)
	private Integer[] scheduledOperations;
	private int scheduleIndex;
	
	// Store the last operation executed for each job (used for knowing possible next operations)
	private int[] lastJobOperation;
	
	// Node connections in the pheromone matrix. Ant uses edge (i,j) if connections[i] = j
	private int[] connections;
	
	public Ant(ACOAlgorithm alg) {
		this.alg = alg;	
		scheduledOperations = new Integer[alg.getProblemInstance().getTotalOperations()];
	}
	
	/**
	 * Generate a new iteration of this ant.
	 */
	public void generate() {
		ProblemInstance pb = alg.getProblemInstance();

		// Reset
		makespan = 0;
		scheduleIndex = 0;
		scheduledOperations = new Integer[pb.getTotalOperations()];
		connections = new int[pb.getTotalOperations()+1];
		lastJobOperation = new int[pb.getNumberOfJobs()];
		Arrays.fill(lastJobOperation, -1);
		
		// Choose first operation
		int operation = chooseNextOperation();
		connections[0] = operation + 1;
		
		long time1 = 0, time2 = 0;
		
		do {
			long time = System.nanoTime();
			
			int temp = operation;
			operation = chooseNextOperation();
			connections[temp+1] = operation+1;
			
			time1 += System.nanoTime() - time;
			time = System.nanoTime();
			
			scheduledOperations[scheduleIndex++] = operation;
			lastJobOperation[operation / pb.getOperationsPerJob()] = operation;
			
			makespan = alg.computeMakespan(scheduledOperations);
			
			time2 += System.nanoTime() - time;
		} while(scheduleIndex < scheduledOperations.length);

		// System.out.println(time1/1000.0f + "us " + time2/1000.0f + "us ");
	}
	
	/**
	 * Get the current makespan of the ant.
	 * @return the makespan value
	 */
	public int getMakespan() {
		return makespan;
	}
	
	/**
	 * Computes the next operation in the schedule, taking into account the current schedule and the colony's pheromone matrix.
	 * @return the index of the next operation
	 */
	private int chooseNextOperation() {
		List<Integer> accessibleOperations = getAccessibleOperations();
		
		if(accessibleOperations.size() == 1)
			return accessibleOperations.get(0);
		
		ProblemInstance pb = alg.getProblemInstance();
		float alpha = alg.getAlpha(), beta = alg.getBeta();
		
		float[] probabilities = new float[pb.getTotalOperations()];
		
		int currentNode = scheduleIndex == 0 ? 0 : (1 + scheduledOperations[scheduleIndex-1]);
		
		double denominator = 0;
		for(int k : accessibleOperations)
			denominator += Math.pow(alg.getColony().getPheromones(currentNode, k+1), alpha) / Math.pow(distance(k), beta);
		
		for(int j : accessibleOperations)
			probabilities[j] = (float) ((Math.pow(alg.getColony().getPheromones(currentNode, j+1), alpha) / Math.pow(distance(j), beta)) / denominator);
			
		/*System.out.println(currentNode);
		System.out.println(Arrays.toString(scheduledOperations));
		System.out.println(Arrays.toString(probabilities));
		System.out.println("accessible length: " + accessibleOperations.size());*/
		
		// Pick a node using roulette wheel with the calculated probabilities
		int operation = RouletteWheel.spinOnce(alg.getRandom(), probabilities);
		
		return operation;
	}
	
	/**
	 * Get the operations that can be executed considering the current state.
	 * @return a list of operation indices
	 */
	public List<Integer> getAccessibleOperations() {
		ProblemInstance pb = alg.getProblemInstance();
		
		List<Integer> accessible = new ArrayList<Integer>(); 
		for(int job = 0; job < pb.getNumberOfJobs(); job++) {
			if(lastJobOperation[job] == -1)
				accessible.add(job * pb.getOperationsPerJob());
			else if((lastJobOperation[job] + 1) % pb.getOperationsPerJob() != 0)
				accessible.add(lastJobOperation[job] + 1);
		}		
		return accessible;
	}
	
	/**
	 * Get the distance (additional makespan) from the current state to the one where the given operation was added to the schedule.
	 * @param addedOperation - An operation index
	 * @return the increase in makespan that would come with adding the given operation to the schedule
	 */
	private int distance(int addedOperation) {
		Integer[] newOrder = Arrays.copyOf(scheduledOperations, scheduleIndex + 1);
		newOrder[scheduleIndex] = addedOperation;
		return alg.computeMakespan(newOrder) - getMakespan();
	}
	
	/**
	 * Get the ant's scheduled order of operations.
	 * @return an array with ordered operation indices
	 */
	public Integer[] getScheduledOperations() {
		return scheduledOperations;
	}

	/**
	 * Get whether or not this ant uses the edge (i,j) in the pheromone matrix.
	 * @param i - A node index in the pheromone matrix
	 * @param j - A node index in the pheromone matrix
	 * @return true if edge (i,j) is found in the ant's route, else false
	 */
	public boolean hasConnection(int i, int j) {
		return connections[i] == j;
	}
}
