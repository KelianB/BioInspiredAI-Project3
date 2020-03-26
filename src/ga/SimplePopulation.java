package ga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A simple implementation of the IPopulation interface.
 * @author Kelian Baert & Caroline de Pourtales
 */
public abstract class SimplePopulation implements IPopulation {
	private List<IIndividual> individuals;
	protected GeneticAlgorithm ga;
	
	public SimplePopulation(GeneticAlgorithm ga) {
		individuals = new ArrayList<IIndividual>();
		this.ga = ga;
	}
	
	@Override
	public void addIndividual(IIndividual ind) {
		this.individuals.add(ind);
	}
	
	@Override
	public void setIndividuals(List<IIndividual> inds) {
		this.individuals = inds;
	}

	@Override
	public List<IIndividual> getIndividuals() {
		return individuals;
	}
	
	@Override
	public int getSize() {
		return individuals.size();
	}
	
	@Override
	public IIndividual getFittestIndividual() {
		int popSize = getSize();
		if(popSize == 0)
			return null;
		
		int maxIndex = 0;
		float maxFitness = getIndividuals().get(0).getFitness();
		for(int i = 1; i < popSize; i++) {
			float fitness = getIndividuals().get(i).getFitness();
			if(fitness > maxFitness) {
				maxFitness = fitness;
				maxIndex = i;
			}
		}
		
		return getIndividuals().get(maxIndex);
	}
	
	@Override
	public void insertOffspring(List<IIndividual> offspring) {
		// No elitism - replace whole population
		if(ga.getElites() == 0) {
			getIndividuals().clear();
			getIndividuals().addAll(offspring);
		}
		// Elitism - replace population except n best
		else {
			putElitesFirst(ga.getElites());
			for(int i = 0; i < offspring.size(); i++)
				getIndividuals().set(ga.getElites() + i, offspring.get(i));
		}
	}
	
	@Override
	public Comparator<IIndividual> getSelectionComparator() {
		// By default, just compare individuals by fitness
		return (a,b) -> (int) (Math.signum(b.getFitness() - a.getFitness()));
	}
	
	/**
	 * Puts the given number of best individuals at the beginning of the individuals list, in no particular order
	 * @param numberOfElites - The number of elites
	 */
	public void putElitesFirst(int numberOfElites) {
		if(numberOfElites == 0)
			return;
		
		PriorityQueue<IIndividual> heap = new PriorityQueue<>(getIndividuals().size(), getSelectionComparator());
		heap.addAll(getIndividuals());
		for(int i = 0; i < numberOfElites; i++) {
			IIndividual next = heap.poll();
			int idx = getIndividuals().indexOf(next);
			getIndividuals().remove(idx);
			getIndividuals().add(0, next);
		}
	}
	
	/**
	 * Selects an individual from the population using tournament selection
	 * @param tournamentSize - The tournament size
	 * @param p - The probability of selecting the best individual in the tournament
	 */
	public IIndividual tournamentSelection(int tournamentSize, float p) {
		List<IIndividual> pool = new ArrayList<IIndividual>();
		
		// Create the tournament pool
		for(int i = 0; i < tournamentSize; i++)
			pool.add(getIndividuals().get((int) (ga.random() * getIndividuals().size())));
		
		// Sort using the selection comparator
		pool.sort(getSelectionComparator());
		
		// Extract an individual
		if(p == 1)
			return pool.get(0);
		for(int i = 0; i < pool.size(); i++) {
			if(ga.random() < p * Math.pow(1-p, i)) 
				return pool.get(i);
		}
		
		// Fall back to random
		return pool.get((int) (ga.random() * pool.size()));
	}

	/**
	 * Selects an individual from the population using deterministic tournament selection (will always select the best individual in the tournament)
	 * @param tournamentSize - The tournament size
	 */
	public IIndividual tournamentSelection(int tournamentSize) {
		return tournamentSelection(tournamentSize, 1);
	}
}
