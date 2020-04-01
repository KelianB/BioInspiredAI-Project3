package aco;

import jssp.JSSPAlgorithm;
import jssp.ProblemInstance;
import main.Config;

/**
 * Ant Colony Optimization algorithm class
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ACOAlgorithm extends JSSPAlgorithm {	
	// The ant colony
	private Colony colony;
	
	// Keep track of ran iterations
	private int ranIterations = 0;
	
	// Ant Colony Optimization parameters
	private float alpha, beta;
	
	/***
	 * Initialize an Ant Colony Optimization algorithm.
	 * @param problemInstance - A JSSP problem instance
	 * @param config - A configuration object
	 */
	public ACOAlgorithm(ProblemInstance problemInstance, Config config) {
		super(problemInstance);
		
		// Read fields from the config
		this.alpha = config.getFloat("alpha");
		this.beta = config.getFloat("beta");
		
		int colonySize = config.getInt("colonySize");
		float initialPheromones = config.getFloat("initialPheromones");
		float rho = config.getFloat("rho");
		float Q = config.getFloat("Q");
		
		// Initialize the swarm
		this.colony = new Colony(this, colonySize, initialPheromones, Q, rho);		
	}
	
	/**
	 * Get the ant colony in this Ant Colony Optimization
	 * @return the ant colony
	 */
	public Colony getColony() {
		return colony;
	}	

	public float getAlpha() {
		return alpha;
	}
	
	public float getBeta() {
		return beta;
	}
	
	@Override
	public void runIteration() {
		// Re-generate ants
		this.colony.nextGeneration();

		// Update pheromone matrix
		colony.updatePheromones();
		
		ranIterations++;
	}
	
	@Override
	public void printState() {
		Colony c = getColony();
		System.out.println("\n############### Iteration " + ranIterations + " ###############");
		
		boolean printPheromones = false;
		if(printPheromones) {
			System.out.println("Pheromones:");
			for(int i = 0; i < getProblemInstance().getTotalOperations() + 1; i++) {
				String str = "";
				for(int j = 0; j < getProblemInstance().getTotalOperations() + 1; j++) {
					float p = c.getPheromones(i, j);
					if(p < 1e-3)
						p = 0;
					str += Math.round(p*1000) / 1000.0f + " ";
				}
				System.out.println(str);
			}
		}
		
		System.out.println("Best makespan of colony: " + c.getBestAnt().getMakespan() + " (average = " + c.getAverageMakespan() + ")");
		System.out.println("Best makespan so far: " + computeMakespan(c.getBestSoFar()));
	}
	
	@Override
	public Integer[] getBestSolution() {
		return getColony().getBestSoFar();
	}
	
	@Override
	public int getRanIterations() {
		return ranIterations;
	}
}
