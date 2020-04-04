package pso;

/**
 * Represents a Swarm, in the context of Particle Swarm Optimization
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Swarm {
	// The particles that make up the swarm
	private Particle[] particles;
	
	// Store the best position achieved by the swarm, and its associated fitness
	private float[] globalBestPosition;
	private int globalBestFitness;
	
	/**
	 * Initialize a Swarm.
	 * @param alg - A reference to the Particle Swarm Optimizer
	 * @param size - The size of the swarm
	 */
	private Swarm(PSOAlgorithm alg, int size) {
		this.particles = new Particle[size];
		this.globalBestFitness = Integer.MIN_VALUE;
		this.globalBestPosition = new float[alg.getProblemInstance().getTotalOperations()];
	}
	
	/**
	 * Get all particles in the swarm.
	 * @return the particles that make up the particle swarm
	 */
	public Particle[] getParticles() {
		return particles;
	}
	
	/**
	 * Get the fittest particle in the swarm.
	 * @return the particle with the highest fitness
	 */
	public Particle getFittest() {
		int bestIndex = 0;
		float bestValue = this.particles[0].getFitness();
		for(int i = 1; i < this.particles.length; i++) {
			float f = this.particles[i].getFitness();
			if(f > bestValue) {
				bestValue = f;
				bestIndex = i;
			}
		}
		return this.particles[bestIndex];
	}
	
	/**
	 * Get the average fitness of the swarm.
	 * @return the average fitness
	 */
	public float getAverageFitness() {
		float averageFitness = 0;
		for(int i = 0; i < this.particles.length; i++)
			averageFitness += this.particles[i].getFitness() / (float) this.particles.length;
		return averageFitness;
	}
	
	/**
	 * Find the best particle in the swarm, and update the swarm's global best as needed.
	 */
	protected void updateGlobalBest() {
		for(Particle p : this.getParticles()) {
			if(p.getLocalBestFitness() > globalBestFitness) {
				globalBestFitness = p.getLocalBestFitness();
				float[] pos = p.getLocalBestPosition();
				for(int j = 0; j < pos.length; j++)
					globalBestPosition[j] = pos[j];
			}
		}
	}
	
	/**
	 * Get the best position ever found by this swarm (i.e. the positionw with the highest fitness).
	 * @return an array of coordinates
	 */
	public float[] getGlobalBestPosition() {
		return globalBestPosition;
	}
	
	/**
	 * Get the best fitness ever attained by this swarm.
	 * @return the finess associated with the best global position
	 */
	public int getGlobalBestFitness() {
		return globalBestFitness;
	}
	
	/**
	 * Create a swarm of random particles with the given ranges of position and velocities.
	 * @param alg - A reference to the PSO algorithm this swarm belongs to
	 * @param size - The number of particles in the swarm
	 * @param xmin - The lower bound of the position range
	 * @param xmax - The upper bound of the position range
	 * @param vmin - The lower bound of the velocity range
	 * @param vmax - The upper bound of the velocity range
	 * @return a Particle
	 */
	public static Swarm randomSwarm(PSOAlgorithm alg, int size, float xmin, float xmax, float vmin, float vmax) {
		Swarm s = new Swarm(alg, size);
	
		for(int i = 0; i < size; i++)
			s.particles[i] = Particle.randomParticle(alg, xmin, xmax, vmin, vmax);
		
		return s;
	}
}
