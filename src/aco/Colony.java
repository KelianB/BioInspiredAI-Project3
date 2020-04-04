package aco;

import jssp.ProblemInstance;

/**
 * Represents a Colony, in the context of Ant Colony Optimization
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Colony {
	// The ants that make up the colony
	private Ant[] ants;
	
	// Pheromone matrix
	private float[][] pheromones;
	
	// Store the best position achieved by the colony, and its associated makespan
	private Integer[] bestSoFar;
	private int bestMakespanSoFar;
	
	// Pheromone parameters
	private float Q, rho;
	
	public Colony(ACOAlgorithm alg, int size, float initialPheromones, float Q, float rho) {
		this.ants = new Ant[size];
		this.Q = Q;
		this.rho = rho;
		
		ProblemInstance pb = alg.getProblemInstance();
		
		this.bestMakespanSoFar = Integer.MAX_VALUE;
		this.bestSoFar = new Integer[pb.getTotalOperations()];
		
		// Init pheromone matrix
		pheromones = new float[pb.getTotalOperations() + 1][pb.getTotalOperations() + 1];
		for(int i = 0; i < pb.getNumberOfJobs(); i++)
			pheromones[0][1 + i * pb.getOperationsPerJob()] = initialPheromones;
		for(int i = 0; i < pb.getTotalOperations(); i++) {
			int job = i / pb.getOperationsPerJob();
			// Set initial pheromones on each edge from i to an accessible operation
			for(int op = 0; op < pb.getTotalOperations(); op++) {
				int jobOp = op / pb.getOperationsPerJob();		
				if(jobOp != job || op == i+1)
					pheromones[i+1][op+1] = initialPheromones;
			}
		}
		
		// Create ants
		for(int i = 0; i < size; i++)
			this.ants[i] = new Ant(alg);
	}
	
	/**
	 * Get all ants in the colony.
	 * @return the ants that make up the ant colony
	 */
	public Ant[] getAnts() {
		return ants;
	}
	
	/**
	 * Get the pheromone level between two given nodes.
	 * @param i - A node
	 * @param j - Another node
	 */
	public float getPheromones(int i, int j) {
		return pheromones[i][j];
	}
	
	/**
	 * Generate the next generation of ants.
	 */
	public void nextGeneration() {
		for(int i = 0; i < ants.length; i++)
			ants[i].generate();
		
		// Update the best solution so far
		for(Ant a : this.getAnts()) {
			if(a.getMakespan() < bestMakespanSoFar) {
				bestMakespanSoFar = a.getMakespan();
				for(int i = 0; i < a.getScheduledOperations().length; i++)
					bestSoFar[i] = a.getScheduledOperations()[i];
			}
		}
	}
	
	/**
	 * Update the pheromone matrix based on which edges were the most successful.
	 */
	public void updatePheromones() {
		boolean onlyBest = true;
		
		Ant[] ants = onlyBest ? new Ant[] {getBestAnt()} : getAnts();
		
		for(int i = 0; i < pheromones.length; i++) {
			for(int j = 0; j < pheromones[i].length; j++) {
				float delta = 0.0f;
				for(Ant a : ants) {
					float makespan = (float) a.getMakespan();
					delta += a.hasConnection(i, j) ? Q / makespan : 0;
				}	
				pheromones[i][j] = (1.0f - rho) * pheromones[i][j] + delta;
			}
		}
	}
	
	/**
	 * Get the best ant in the colony with regards to makespan.
	 * @return the ant with the lowest makespan
	 */
	public Ant getBestAnt() {
		int bestIndex = 0;
		float bestValue = this.ants[0].getMakespan();
		for(int i = 1; i < this.ants.length; i++) {
			float m = this.ants[i].getMakespan();
			if(m < bestValue) {
				bestValue = m;
				bestIndex = i;
			}
		}
		return this.ants[bestIndex];
	}
	
	/**
	 * Get the average makespan of the colony.
	 * @return the average makespan
	 */
	public float getAverageMakespan() {
		float averageMakespan = 0;
		for(int i = 0; i < this.ants.length; i++)
			averageMakespan += this.ants[i].getMakespan() / (float) this.ants.length;
		return averageMakespan;
	}
	
	/**
	 * Get the best solution so far.
	 * @return the best order of operations found by this ant colony
	 */
	public Integer[] getBestSoFar() {
		return bestSoFar;
	}
}
