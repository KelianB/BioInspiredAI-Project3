package main;

import java.util.Arrays;

import jssp.ProblemInstance;
import jssp.ProblemReader;
import pso.PSOAlgorithm;
import utils.GanttChart;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	public static Config config;
	
	public static void main(String[] args) {
		// Read configuration file
		config = new Config("config.properties");
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader();
		
		// Read problem
		ProblemInstance instance = reader.readProblem(config.get("problemInstance"));
		
		// Abort if the problem instance couldn't be read
		if(instance == null) {
			System.err.println("[Critical Error] Couldn't read problem instance.");
			System.exit(1);
		}
		
		// Print information about the problem instance
		System.out.println("Problem instance: " + instance.getName() + 
				" (" + instance.getNumberOfJobs() + " jobs, " + instance.getOperationsPerJob() + " machines)");		
		
		/** -------------TEMPORARY ------------- */
		int optimalMakespan = Arrays.asList(56, 1059, 1276, 1130, 1451, 1721, 977).get(Integer.parseInt(instance.getName().substring(0, 1)) - 1);

		int iterations = 50000;
		int epochSize = 1000;
		/** ----------------------------------- */
		
		// Create algorithm
		PSOAlgorithm pso = new PSOAlgorithm(instance, config);
		
		/*Runnable onTermination = () ->  {
			System.out.println("TERMINATION");
		};
		// Define the shutdown hook to execute on termination
		Runtime.getRuntime().addShutdownHook(new Thread(onTermination));*/
		
		long epochStartTime = System.currentTimeMillis();
		pso.printState();
		for(int i = 0; i < iterations; i++) {
			pso.runIteration();
			if(pso.getRanIterations() % epochSize == 0) {
				pso.printState();
				System.out.println("Average time per iteration: " + Math.round(100 * (System.currentTimeMillis() - epochStartTime) / epochSize) / 100.0 + " ms");
				epochStartTime = System.currentTimeMillis();
			}
		}
		
		int bestMakespan = -pso.getSwarm().getGlobalBestFitness();
		System.out.println("\n--------------- Run finished ---------------");
		System.out.println("Global best makespan: " + bestMakespan + "  (" + (100 * (bestMakespan - optimalMakespan) / optimalMakespan) + "% off)");
		GanttChart gc = pso.createGanttChart(pso.getSwarm().getGlobalBestPosition());
		System.out.println("Gantt-Chart validity test: " + gc.test());
		System.out.println(gc);
	}
	
	
	
	
	
	/*
	private static boolean clearedOutputDirs = false;
	
	public static void main(String[] args) {
		
		
		// on weighted-sum GA termination: save fittest
		Runnable onTerminationGA = () ->  {
			System.out.println("Saving fittest");
	        saveImages(instance, ((Individual) sga.getPopulation().getFittestIndividual()));			
		};
		
		// on MOEA termination: save first front
		Runnable onTerminationMOEA = () ->  {
			System.out.println("Saving first front");
			for(Individual i : ((MultiObjectivePopulation) sga.getPopulation()).getFirstFront())
				saveImages(instance, i);
		};
		
		Runnable onFinish = sga instanceof MultiObjectiveSegmentationGA ? onTerminationMOEA : onTerminationGA;
		
		// Define the shutdown hook to execute on termination
		Runtime.getRuntime().addShutdownHook(new Thread(onFinish));
		
		for(int i = 0; i < config.getInt("generations"); i++) {
			long time = System.nanoTime();
			System.out.println("---------- Running generation #" + i + " ----------");
			sga.runGeneration();
			sga.printState();
			System.out.println("(" + (System.nanoTime() - time) / 1000000 + " ms)");
		}
	}
	*/
}
