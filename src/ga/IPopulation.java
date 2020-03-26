package ga;

import java.util.Comparator;
import java.util.List;

/**
 * An interface describing a basic population in a Genetic Algorithm.
 * @author Kelian Baert & Caroline de Pourtales
 */
public interface IPopulation {
	/**
	 * Get the individuals in the population
	 * @return a list of individuals
	 */
	public List<IIndividual> getIndividuals();
	
	/**
	 * Add an individual to the population
	 * @param ind - An individual
	 */
	public void addIndividual(IIndividual ind);
	
	/**
	 * Sets the population's individuals
	 * @param inds - A list of individuals
	 */
	public void setIndividuals(List<IIndividual> inds);

	/**
	 * Create offspring from the current population 
	 * @return a list of individuals (/!\ remember to make copies of individuals when including duplicates)
	 */
	public List<IIndividual> createOffspring();
	
	/**
	 * Insert offspring into the population
	 * @param offspring - A list of offspring
	 */
	public void insertOffspring(List<IIndividual> offspring);
	
	/**
	 * Get the size of the population
	 * @return the number of individuals in the population
	 */
	public int getSize();
	
	/**
	 * Get the individual that has the highest fitness in the population
	 * @return the individual that has the highest fitness in the population
	 */
	public IIndividual getFittestIndividual();
	
	/**
	 * Get the sorting comparator used for selection
	 */
	public Comparator<IIndividual> getSelectionComparator();
}
