package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import aco.ACOAlgorithm;
import jssp.JSSPAlgorithm;
import pso.PSOAlgorithm;

/**
 * A solver class that handles multi-threading for solving JSSP problems using ACO or PSO.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Solver {
	// Store algorithm instances
	private List<JSSPAlgorithm> algorithms;
	
	// Whether or not the solver is currently working or not
	private boolean solving;
	
	/**
	 * Initialize the solver.
	 * @param algorithmSupplier - A supplier that creates algorithm instances
	 * @param numThreads - The number of algorithms that will run in parallel
	 */
	public Solver(Supplier<JSSPAlgorithm> algorithmSupplier, int numThreads) {
		this.algorithms = new ArrayList<JSSPAlgorithm>();
		this.solving = false;

		// Get n algorithms from the supplier
		for(int i = 0; i < numThreads; i++)
			algorithms.add(algorithmSupplier.get());
	}

	/**
	 * Initialize the solver without parallel runs.
	 * @param algorithmSupplier - A supplier that gives JSSPAlgorithm instances
	 */
	public Solver(Supplier<JSSPAlgorithm> algorithmSupplier) {
		this(algorithmSupplier, 1);
	}
	
	/**
	 * Start solving.
	 * @param maxIterations - The maximum number of iterations to run for
	 * @param epochSize - The number of iterations per epoch
	 * @param onFinish - A function called when the solver has finished working
	 */
	public void solve(int maxIterations, int epochSize, Consumer<JSSPAlgorithm> onFinish) {
		if(solving) {
			System.err.println("[JSSP Solver] Already solving.");
			return;
		}
		solving = true;
		
		List<JSSPAlgorithm> running = new ArrayList<JSSPAlgorithm>();
		for(JSSPAlgorithm alg : algorithms)
			running.add(alg);
		
		/*Consumer<JSSPAlgorithm> runAlgorithm = (alg) -> {
			long epochStartTime = System.currentTimeMillis();
			alg.printState();
			for(int i = 0; i < maxIterations; i++) {
				alg.runIteration();
				if(alg.getRanIterations() % epochSize == 0) {
					alg.printState();
					System.out.println("Average time per iteration: " + Math.round(100 * (System.currentTimeMillis() - epochStartTime) / epochSize) / 100.0 + " ms");
					epochStartTime = System.currentTimeMillis();
					
					boolean keepGoing = onEpochEnd.apply(alg);
					if(!keepGoing)
						break;
				}
			}
		};*/
		
		while(!running.isEmpty()) {
			List<Thread> threads = new ArrayList<Thread>();
			final List<JSSPAlgorithm> toStop = new ArrayList<JSSPAlgorithm>();
			final List<Float> avgTimesPerIter = new ArrayList<Float>();
			
			// For each running algorithm, start a new thread to run an epoch 
			for(int i = 0; i < running.size(); i++) {
				final JSSPAlgorithm alg = running.get(i);
					
				Thread thr = new Thread(() -> {
					long epochStartTime = System.currentTimeMillis();
					int ranIterations = alg.getRanIterations();
					
					boolean keepGoing = runEpoch(alg, epochSize, maxIterations);
					if(!keepGoing)
						toStop.add(alg);
					
					avgTimesPerIter.add((System.currentTimeMillis() - epochStartTime) / (float) (alg.getRanIterations() - ranIterations));					
				});
				threads.add(thr);
				thr.start();
			}
			
			// Wait for all threads to be finished
			try {
				for(Thread thr : threads)
					thr.join();
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			// Remove algorithms that should stop running
			running.removeAll(toStop);
			
			// Print state
			System.out.println("\n############### " + (running.isEmpty() ? "FINISHED" : ("Iteration " + running.get(0).getRanIterations())) + " ###############");
			printState(running, avgTimesPerIter);			
		}
		
		onFinish.accept(getBestAlgorithm());

		solving = false;
	}
	
	/**
	 * Get the algorithm that has found the best solution (i.e. lowest makespan) globally so far.
	 * @return a JSSPAlgorithm instance
	 */
	private JSSPAlgorithm getBestAlgorithm() {
		JSSPAlgorithm best = null;
		int bestMakespan = Integer.MAX_VALUE;
		
		for(JSSPAlgorithm alg : algorithms) {
			int makespan = alg.computeMakespan(alg.getBestSolution());
			if(makespan < bestMakespan) {
				best = alg;
				bestMakespan = makespan;
			}
		}
		
		return best;
	}
	
	/**
	 * Run a single epoch for the given algorithm.
	 * @param alg - An algorithm
	 * @param epochSize - The number of iterations per epoch
	 * @param maxTotalIterations - The maximum number of iterations the algorithm should run
	 * @return true if the algorithm should keep going after this epoch, false if it has finished running or if it should be early-stopped
	 */
	private boolean runEpoch(JSSPAlgorithm alg, int epochSize, int maxTotalIterations) {
		int makespanBefore = alg.computeMakespan(alg.getBestSolution());

		for(int i = 0; i < epochSize; i++) {
			alg.runIteration();
			if(alg.getRanIterations() == maxTotalIterations)
				return false;
		}
		
		// Early stopping
		int makespanAfter = alg.computeMakespan(alg.getBestSolution());
				
		if(makespanBefore == makespanAfter)
			return false;
	
		
		// TODO: implement early-stopping
		return true;
	}
	
	/**
	 * Prints the current state of the solver (called after the end of each epoch).
	 * @param running - A list of running algorithms
	 * @param avgTimesPerIter - The average time per iteration for each algorithm, during the last epoch
	 */
	private void printState(List<JSSPAlgorithm> running, List<Float> avgTimesPerIter) {
		Locale l = Locale.ENGLISH;
		
		float avgTimePerIter = 0.0f;
		for(float f : avgTimesPerIter)
			avgTimePerIter += f / avgTimesPerIter.size();
		
		/*System.out.println("Inertia: " + inertia);
		System.out.println("Best makespan of swarm: " + (-s.getFittest().getFitness()) + " (average = " + (-s.getAverageFitness()) + ")");
		*/
		System.out.println("Still running: " + running.size());
		System.out.println("Average time per iteration: " + Math.round(100 * avgTimePerIter) / 100.0 + " ms");
		JSSPAlgorithm bestAlg = getBestAlgorithm();
		System.out.println("Best makespan achieved globally: " + bestAlg.computeMakespan(bestAlg.getBestSolution()));
		for(int i = 0; i < algorithms.size(); i++) {
			JSSPAlgorithm alg = algorithms.get(i);
			String algStr = "[alg " + String.format(l, "%03d", i+1) + (running.contains(alg) ? "*" : "-") + "]";
			algStr += " best_makespan=" + String.format(l, "%04d", alg.computeMakespan(alg.getBestSolution()));
			
			if(alg instanceof PSOAlgorithm) {
				PSOAlgorithm pso = (PSOAlgorithm) alg;
				algStr += " inertia=" + String.format(l, "%.4f", pso.getInertia());
				algStr += " swarm_best=" + String.format(l, "%04d", -pso.getSwarm().getFittest().getFitness());
				algStr += " swarm_avg=" + String.format(l, "%06.4f", -pso.getSwarm().getAverageFitness());
			}	
			else if(alg instanceof ACOAlgorithm) {
				ACOAlgorithm aco = (ACOAlgorithm) alg;
				algStr += " colony_best=" + String.format(l, "%04d", aco.getColony().getBestAnt().getMakespan());
				algStr += " colony_avg=" + String.format(l, "%06.4f", aco.getColony().getAverageMakespan());
			}
			
			System.out.println(algStr);
		}
	}

}
