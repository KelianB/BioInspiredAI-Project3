package pso;

import utils.CachedValue;

/**
 * Represents a Particle in the context of Particle Swarm Optimization
 * @author Kelian Baert & Caroline de Pourtales
 */
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
	
	
	public Particle(PSOAlgorithm alg) {
		this.alg = alg;	
		
		position = new float[alg.getProblemInstance().getTotalOperations()];
		velocity = new float[position.length];
		
		localBestPosition = new float[position.length];
		localBestFitness = Integer.MIN_VALUE;
		
		fitness = new CachedValue<Integer>(() -> -alg.computeMakespan(this.position));
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
	 * Get the fitness value of this particle.
	 * @return the last fitness value in cache
	 */
	public int getFitness() {
		return fitness.getValue();
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