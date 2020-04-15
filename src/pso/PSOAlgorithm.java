package pso;

import java.util.Arrays;

import jssp.JSSPAlgorithm;
import jssp.ProblemInstance;
import main.Config;

/**
 * Particle Swarm Optimization algorithm class
 * @author Kelian Baert & Caroline de Pourtales
 */
public class PSOAlgorithm extends JSSPAlgorithm {;
	// The particle swarm
	private Swarm swarm;
	
	// Keep track of ran iterations
	private int ranIterations = 0;
	
	// The max number of iterations to run
	private int maxIterations;
	
	// Store min and max velocity so they can be enforced during the particle updates
	private float vmin, vmax;

	// Inertia weight
	private float inertia, initialInertia, minInertia;
	
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
		super(problemInstance);
	
		tempOperationOrder = new Integer[getProblemInstance().getTotalOperations()];
		for(int i = 0; i < tempOperationOrder.length; i++)
			tempOperationOrder[i] = i;
		
		this.maxIterations = config.getInt("maxIterations");
		
		// Read fields from the config
		this.vmin = config.getFloat("vmin");
		this.vmax = config.getFloat("vmax");
		this.initialInertia = config.getFloat("initialInertia");
		this.minInertia = config.getFloat("minInertia");
		
		this.c1 = config.getFloat("localAccelerationConstant");
		this.c2 = config.getFloat("globalAccelerationConstant");
		
		int swarmSize = config.getInt("swarmSize");
		float xmin = config.getFloat("xmin"), xmax = config.getFloat("xmax");

		this.inertia = initialInertia;
		
		// Initialize the swarm
		this.swarm = Swarm.randomSwarm(this, swarmSize, xmin, xmax, vmin, vmax);		
	}
	
	/**
	 * Get the particle swarm in this Particle Swarm Optimization
	 * @return the swarm of particles
	 */
	public Swarm getSwarm() {
		return swarm;
	}
	
	/**
	 * Get the operation order from a given position.
	 * @param position - A position array
	 * @return an array containing operation indices in running order
	 */
	public Integer[] getOperationOrder(float[] position) {
		// Sort an array of operation indices according to the position values
		Arrays.sort(tempOperationOrder, (i1, i2) -> Float.compare(position[i1], position[i2]));
		
		return tempOperationOrder;
	}
	
	/**
	 * Calculates the makespan for a given particle position.
	 * @param position - A position array from a particle
	 */
	public int computeMakespan(float[] position) {
		// Get the operation order
		Integer[] operationOrder = getOperationOrder(position);
		// Compute the makespan from it
		return super.computeMakespan(operationOrder);
	}
	
	/**
	 * Get the current value of the inertia parameter.
	 * @return the inertia value
	 */
	public float getInertia() {
		return inertia;
	}
	
	@Override
	public void runIteration() {
		// Update the swarm's global best position and fitness
		getSwarm().updateGlobalBest();
	
		// Update all particles in the swarm
		for(Particle p : swarm.getParticles()) {
			p.update(swarm.getGlobalBestPosition(), inertia, c1, c2, vmin, vmax);
		}
		
		// Update inertia
		if(inertia > minInertia) {
			// Linear
			//inertia -= config.getFloat("inertiaStep");
			
			// Ajusted to spend more time exploring with lower inertia
			double x = ranIterations / (float) maxIterations;
			inertia = initialInertia - 1.35f * (float) Math.pow(Math.log10(x + 1), 1.0/4.0) * (initialInertia - minInertia);
			
			if(inertia < minInertia)
				inertia = minInertia;
		}
		
		
		ranIterations++;
	}
	
	@Override
	public void printState() {
		Swarm s = getSwarm();
		System.out.println("\n############### Iteration " + ranIterations + " ###############");
		System.out.println("Inertia: " + inertia);
		System.out.println("Best makespan of swarm: " + (-s.getFittest().getFitness()) + " (average = " + (-s.getAverageFitness()) + ")");
		System.out.println("Best makespan achieved globally: " + (-s.getGlobalBestFitness()));
	}
	
	@Override
	public Integer[] getBestSolution() {
		float[] bestPosition = getSwarm().getGlobalBestPosition();
		return getOperationOrder(bestPosition);
	}
	
	@Override
	public int getBestOverallMakespan() {
		return -getSwarm().getGlobalBestFitness();
	}
	
	@Override
	public int getRanIterations() {
		return ranIterations;
	}
}
