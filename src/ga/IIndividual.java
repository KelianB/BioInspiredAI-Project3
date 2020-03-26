package ga;

/**
 * An interface describing a basic individual in a Genetic Algorithm.
 * @author Kelian Baert & Caroline de Pourtales
 */
public interface IIndividual {
	/**
	 * Get the fitness of the individual.
	 * @return the individual's fitness
	 */
	public float getFitness();
	
	/**
	 * Perform a mutation on the individual.
	 */
	public void mutate();
	
	/**
	 * Perform a crossover with another given individual.
	 * @param parentB - The second parent with which to crossover
	 * @return a new individual resulting in the crossover with parentB.
	 */
	public IIndividual crossover(IIndividual parentB);
	
	/**
	 * Creates a copy of this individual.
	 * @return a new IIndividual instance with the same genotype
	 */
	public IIndividual copy();
}
