package ga;

import java.util.List;
import java.util.Random;

/**
 * A simple implementation of the IGeneticAlgorithm interface.
 * @author Kelian Baert & Caroline de Pourtales
 */
public abstract class GeneticAlgorithm implements IGeneticAlgorithm {
	private int generationsRan;
	private IPopulation population;
	private IProblemInstance problemInstance;
	private float mutationRate, crossoverRate;
	private int elites;
	private Random random;

	public GeneticAlgorithm(IProblemInstance problemInstance, float mutationRate, float crossoverRate) {
		this.problemInstance = problemInstance;
		this.generationsRan = 0;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.elites = 0;
		this.random = new Random();
	}
	
	public GeneticAlgorithm(IProblemInstance problemInstance) {
		this(problemInstance, 0, 0);
	}

	@Override
	public void initializePopulation() {
		if(population != null) {
			System.err.println("Error: GA population was already initialized");
			return;
		}
		population = createInitialPopulation();
	}
	
	protected abstract IPopulation createInitialPopulation();
	
	@Override
	public void runGeneration() {
		boolean printTimes = false;
		
		if(population == null) {
			System.err.println("Cannot run GA generation without first initializing the population");
			return;
		}
		
		// Create offsprings
		long globalTime = System.nanoTime();
		long time = System.nanoTime();
		List<IIndividual> offspring = population.createOffspring();
		if(printTimes)
			System.out.println("Creating offspring took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		// Mutate (handles mutation rates higher than 1)
		time = System.nanoTime();
		for(int i = 0; i < offspring.size(); i++) {
			float r = getMutationRate();
			while(r > 0) {
				if(r >= 1 || random() < r)
					offspring.get(i).mutate();
				r -= 1;
			}
		}
		if(printTimes)
			System.out.println("Mutating took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		// Insert offspring
		time = System.nanoTime();
		population.insertOffspring(offspring);
		if(printTimes)
			System.out.println("Inserting offspring took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		if(printTimes)
			System.out.println("Ran generation in " + (System.nanoTime() - globalTime) / 1000000 + "ms");
		
		generationsRan++;
	}
	
	/**
	 * Get a random float in [0,1[ using this GA's random generator
	 * @return a random float between 0 (inclusive) and 1 (exclusive)
	 */
	public float random() {
		return random.nextFloat();
	}
	
	/* SETTERS */
	
	@Override
	public void setMutationRate(float r) {
		this.mutationRate = r;
	}
	
	@Override
	public void setCrossoverRate(float r) {
		this.crossoverRate = r;
	}
	
	@Override
	public void setElites(int elites) {
		this.elites = elites;
	}

	/* GETTERS */
	
	@Override
	public int getGenerationsRan() {
		return generationsRan;
	}

	@Override
	public IPopulation getPopulation() {
		return population;
	}
	
	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
	}
	
	@Override
	public float getMutationRate() {
		return mutationRate;
	}
	
	public float getCrossoverRate() {
		return crossoverRate;
	}
	
	@Override
	public int getElites() {
		return elites;
	}
}
