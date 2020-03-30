package pso;

import java.util.Arrays;
import java.util.Random;

import jssp.ProblemInstance;
import main.Config;

public class PSOAlgorithm {
	private ProblemInstance problemInstance;
	
	private Random random;
	private Swarm swarm;
	
	// Keep track of ran iterations
	private int ranIterations = 0;
	
	// Store min and max velocity so they can be enforced during the particle updates
	private float vmin, vmax;

	// Inertia weight
	private float inertia;
	
	// Local and global acceleration constants
	private float c1, c2;
	
	/***
	 * Initialize a Particle Swarm Optimization algorithm.
	 * @param problemInstance - A JSSP problem instance
	 * @param config - A configuration object
	 */
	public PSOAlgorithm(ProblemInstance problemInstance, Config config) {
		this.problemInstance = problemInstance;
		
		this.random = new Random();
		this.vmin = config.getFloat("vmin");
		this.vmax = config.getFloat("vmax");
		this.inertia = config.getFloat("initialInertia");
		this.c1 = config.getFloat("localAccelerationConstant");
		this.c2 = config.getFloat("globalAccelerationConstant");
		
		this.swarm = Swarm.randomSwarm(this, config.getInt("swarmSize"), 
			config.getFloat("xmin"), config.getFloat("xmax"),
			vmin, vmax
		);		
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
	 * Run a single iteration of the Particle Swarm Optimization
	 */
	public void runIteration() {
		getSwarm().updateGlobalBest();
	
		for(Particle p : swarm.getParticles()) {
			p.update(swarm.getGlobalBestPosition(), inertia, c1, c2, vmin, vmax);
		}
		
		// Update inertia
		// TODO not hardcoded
		if(inertia > 0.4)
			inertia -= (1.2f - 0.4f) / 50000;
		
		ranIterations++;
	}
	
	/**
	 * Print the current state of the algorithm
	 */
	public void printState() {
		System.out.println("###### Ran iterations: " + ranIterations + " ######");
		System.out.println("Inertia: " + inertia);
		System.out.println("Best fitness: " + getSwarm().getFittest().getFitness() + " (average = " + getSwarm().getAverageFitness() + ")");
	}
	
	/**
	 * Get a random float in [0,1[ using this algorithm instance's random generator.
	 * @return a random float between 0 (inclusive) and 1 (exclusive)
	 */
	public float random() {
		return random.nextFloat();
	}
}
