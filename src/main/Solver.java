package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	
	// Store algorithms currently running
	private List<JSSPAlgorithm> runningAlgorithms;
	
	// Store in a map the number of epochs since the last improvement in makespan for each algorithm
	private Map<JSSPAlgorithm, Integer> epochsSinceImprovement;
	
	// The termination value above which a thread will terminate.
	private float terminationThreshold;
	
	// The makespan of the benchmark, used to print current relative gap (ignored when set to 0)
	private int benchmarkMakespan;
	
	/**
	 * Initialize the solver.
	 * @param algorithmSupplier - A supplier that creates algorithm instances
	 * @param numThreads - The number of algorithms that will run in parallel
	 */
	public Solver(Supplier<JSSPAlgorithm> algorithmSupplier, int numThreads, float terminationThreshold, int benchmarkMakespan) {
		this.algorithms = new ArrayList<JSSPAlgorithm>();
		this.runningAlgorithms = new ArrayList<JSSPAlgorithm>();
		this.terminationThreshold = terminationThreshold;
		this.benchmarkMakespan = benchmarkMakespan;
		
		// Get n algorithms from the supplier
		for(int i = 0; i < numThreads; i++)
			algorithms.add(algorithmSupplier.get());		
	}

	/**
	 * Initialize the solver without parallel runs.
	 * @param algorithmSupplier - A supplier that gives JSSPAlgorithm instances
	 */
	public Solver(Supplier<JSSPAlgorithm> algorithmSupplier, float terminationThreshold) {
		this(algorithmSupplier, 1, terminationThreshold, 0);
	}
	
	/**
	 * Start solving.
	 * @param maxIterations - The maximum number of iterations to run for
	 * @param epochSize - The number of iterations per epoch
	 * @param onFinish - A function called when the solver has finished working
	 */
	public void solve(int maxIterations, int epochSize, Consumer<JSSPAlgorithm> onFinish) {
		if(!runningAlgorithms.isEmpty()) {
			System.err.println("[JSSP Solver] Already solving.");
			return;
		}
		
		epochsSinceImprovement = new HashMap<JSSPAlgorithm, Integer>();
		
		for(JSSPAlgorithm alg : algorithms) {
			runningAlgorithms.add(alg);
			epochsSinceImprovement.put(alg, 0);
		}
		
		while(!runningAlgorithms.isEmpty()) {
			List<Thread> threads = new ArrayList<Thread>();
			final List<JSSPAlgorithm> toStop = new ArrayList<JSSPAlgorithm>();
			final List<Float> avgTimesPerIter = new ArrayList<Float>();
			
			// For each running algorithm, start a new thread to run an epoch 
			for(int i = 0; i < runningAlgorithms.size(); i++) {
				final JSSPAlgorithm alg = runningAlgorithms.get(i);
					
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
			runningAlgorithms.removeAll(toStop);
			
			// Print state
			System.out.println("\n############### " + (runningAlgorithms.isEmpty() ? "FINISHED" : ("Iteration " + runningAlgorithms.get(0).getRanIterations())) + " ###############");
			printState(avgTimesPerIter);			
		}
		
		onFinish.accept(getBestAlgorithm());
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
	 * Calculate the average of the best makespan of all algorithms still running.
	 * @return the average makespan
	 */
	private float calculateAverageBestMakespan() {
		float avg = 0.0f;
		for(JSSPAlgorithm alg : runningAlgorithms)
			avg += alg.getBestOverallMakespan() / (float) runningAlgorithms.size();
		return avg;
	}
	
	/**
	 * Run a single epoch for the given algorithm.
	 * @param alg - An algorithm
	 * @param epochSize - The number of iterations per epoch
	 * @param maxTotalIterations - The maximum number of iterations the algorithm should run
	 * @return true if the algorithm should keep going after this epoch, false if it has finished running or if it should be early-stopped
	 */
	private boolean runEpoch(JSSPAlgorithm alg, int epochSize, int maxTotalIterations) {
		int makespanBefore = alg.getBestOverallMakespan();

		for(int i = 0; i < epochSize; i++) {
			alg.runIteration();
			if(alg.getRanIterations() == maxTotalIterations)
				return false;
		}
		
		float patience = 15.0f;

		/** EARLY TERMINATION */
		if(alg.getRanIterations() > epochSize * 2) {
			int makespan = alg.getBestOverallMakespan();
			
			// Proportion of threads still running
			float m = runningAlgorithms.size() / (float) algorithms.size();
			
			// (best_makespan - average_makespan) / (max(best_makespan - average_makespan) for all threads)
			float delta = 0.0f;
			if(runningAlgorithms.size() > 1) {
				float averageBestMakespan = calculateAverageBestMakespan();
				float biggestMakespanDifference = runningAlgorithms.stream()
						.map((algo) -> Math.abs(algo.getBestOverallMakespan() - averageBestMakespan))
						.max(Float::compare).get();
				delta = (makespan - averageBestMakespan) / biggestMakespanDifference;
			}		
			
			// Update number of iterations since last makespan improvement
			epochsSinceImprovement.put(alg, makespan < makespanBefore ? 0 : (epochsSinceImprovement.get(alg) + 1));
			float g = epochsSinceImprovement.get(alg) / patience;
			
			return m + delta + g < terminationThreshold;
		}
		
		return true;
	}
	
	/**
	 * Prints the current state of the solver (called after the end of each epoch).
	 * @param running - A list of running algorithms
	 * @param avgTimesPerIter - The average time per iteration for each algorithm, during the last epoch
	 */
	private void printState(List<Float> avgTimesPerIter) {
		Locale l = Locale.ENGLISH;
		
		float avgTimePerIter = 0.0f;
		for(float f : avgTimesPerIter)
			avgTimePerIter += f / avgTimesPerIter.size();
		
		System.out.println("Still running: " + runningAlgorithms.size());
		System.out.println("Average time per iteration: " + Math.round(100 * avgTimePerIter) / 100.0 + " ms");
		JSSPAlgorithm bestAlg = getBestAlgorithm();
		
		int bestMakespan = bestAlg.computeMakespan(bestAlg.getBestSolution());
		String bestMakespanString = "Best makespan achieved globally: " + bestMakespan;
		if(benchmarkMakespan != 0)
			bestMakespanString += " (benchmark: " + benchmarkMakespan + "; " + (100 * (bestMakespan - benchmarkMakespan) / (float) benchmarkMakespan) + "% off)";
		
		System.out.println(bestMakespanString);
		for(int i = 0; i < algorithms.size(); i++) {
			JSSPAlgorithm alg = algorithms.get(i);
			String algStr = "[alg " + String.format(l, "%03d", i+1) + (runningAlgorithms.contains(alg) ? "*" : "-") + "]";
			algStr += " best_makespan=" + String.format(l, "%04d", alg.computeMakespan(alg.getBestSolution()));
			
			// Print PSO-specific info
			if(alg instanceof PSOAlgorithm) {
				PSOAlgorithm pso = (PSOAlgorithm) alg;
				algStr += " inertia=" + String.format(l, "%.4f", pso.getInertia());
				algStr += " swarm_best=" + String.format(l, "%04d", -pso.getSwarm().getFittest().getFitness());
				algStr += " swarm_avg=" + String.format(l, "%06.4f", -pso.getSwarm().getAverageFitness());
			}	
			// Print ASO-specific info
			else if(alg instanceof ACOAlgorithm) {
				ACOAlgorithm aco = (ACOAlgorithm) alg;
				algStr += " colony_best=" + String.format(l, "%04d", aco.getColony().getBestAnt().getMakespan());
				algStr += " colony_avg=" + String.format(l, "%06.4f", aco.getColony().getAverageMakespan());
			}
			
			System.out.println(algStr);
		}
	}

}
