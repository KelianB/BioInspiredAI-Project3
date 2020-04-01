package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import aco.ACOAlgorithm;
import jssp.JSSPAlgorithm;
import jssp.ProblemInstance;
import jssp.ProblemReader;
import pso.PSOAlgorithm;
import utils.GanttChart;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	/* TODO
	 * early stopping
	 * gantt-chart images
	 * 
	 */
	
	
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
		/** ----------------------------------- */
		
		String mode = config.get("mode");
		
		// Create algorithm
		JSSPAlgorithm alg = null;
		if(mode.equals("ACO"))
			alg = new ACOAlgorithm(instance, config);
		else if(mode.equals("PSO"))
			alg = new PSOAlgorithm(instance, config);
		else {
			System.err.println("[Critical Error] Mode '" + mode + "' does not exist.");
			System.exit(1);
		}
		
		/*Runnable onTermination = () ->  {
			System.out.println("TERMINATION");
		};
		// Define the shutdown hook to execute on termination
		Runtime.getRuntime().addShutdownHook(new Thread(onTermination));*/
		
		int iterations = config.getInt("maxIterations");
		int epochSize = config.getInt("epochSize");
		
		long epochStartTime = System.currentTimeMillis();
		alg.printState();
		for(int i = 0; i < iterations; i++) {
			alg.runIteration();
			if(alg.getRanIterations() % epochSize == 0) {
				alg.printState();
				System.out.println("Average time per iteration: " + Math.round(100 * (System.currentTimeMillis() - epochStartTime) / epochSize) / 100.0 + " ms");
				epochStartTime = System.currentTimeMillis();
			}
		}

		Integer[] bestSolution = alg.getBestSolution();
		int bestMakespan = alg.computeMakespan(bestSolution);
		System.out.println("\n--------------- Run finished ---------------");
		System.out.println("Global best makespan: " + bestMakespan + "  (" + (100 * (bestMakespan - optimalMakespan) / (float) optimalMakespan) + "% off)");
		GanttChart gc = alg.createGanttChart(bestSolution);
		System.out.println("Gantt-Chart validity test: " + gc.test());
		// saveGanttChartImage(instance, gc);
	}

	
	/**
	 * Removes all files in a given directory
	 * @param dir - A directory
	 */
	/*private static void clearDirectory(File dir) {
	    File[] files = dir.listFiles();
	    if(files != null) { // some JVMs return null for empty directories
	        for(File f: files)
	            f.delete();
	    }
	}*/	
	
	/**
	 * Saves the image of a given Gantt chart
	 * @param pi - A problem instance
	 * @param gc - A Gantt chart
	 */
	private static void saveGanttChartImage(ProblemInstance pi, GanttChart gc) {
		BufferedImage img = null/*gc.createImage()*/;
		int makespan = gc.getEndTime();
		
		try {
	    	// Create output directory
	    	new File(config.get("outputDir")).mkdir();
	    	
	    	/*if(!clearedOutputDirs) {
	    		clearDirectory(new File(config.get("outputDir")));
	    		clearedOutputDirs = true;
	    	}*/
	    	
	    	int n = 0;
	    	String fileName;
	    	do {
	    		fileName = "gantt_" + pi.getName() + "_" + makespan + (n==0 ? "" : "_(" + n + ")") + ".png";
	    		n++;
	    	} while(new File(fileName).exists());

	    	// Save the Gantt-Chart image
	    	File outputFile = new File(fileName);
	    	ImageIO.write(img, "png", outputFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*

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
