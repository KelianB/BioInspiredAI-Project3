package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import aco.ACOAlgorithm;
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
	 * handle inertia better to make sure it goes low enough for some local searching
	 * gantt-chart images
	 */
	
	public static void main(String[] args) {
		// Read configuration file
		Config cfg = new Config("config.properties");
		
		int maxIterations = cfg.getInt("maxIterations");
		int epochSize = cfg.getInt("epochSize");
		int threads = cfg.getInt("threads");
		String mode = cfg.get("mode");
		String benchmark = cfg.get("benchmark");
		String outputDirectory = cfg.get("outputDir");
		
		// Check the properties
		if(!mode.equals("ACO") && !mode.equals("PSO")) {
			System.err.println("[Critical Error] Mode '" + mode + "' does not exist.");
			System.exit(1);			
		}		
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader();
		
		// Read problem
		ProblemInstance instance = reader.readProblem(cfg.get("problemInstance"));
		
		// Abort if the problem instance couldn't be read
		if(instance == null) {
			System.err.println("[Critical Error] Couldn't read problem instance.");
			System.exit(1);
		}
		
		// Print information about the problem instance
		System.out.println("Problem instance: " + instance.getName() + 
				" (" + instance.getNumberOfJobs() + " jobs, " + instance.getOperationsPerJob() + " machines)");		
				
		int benchmarkMakespan = benchmark.equals("enabled") ?
				Arrays.asList(56, 1059, 1276, 1130, 1451, 1721, 977).get(Integer.parseInt(instance.getName().substring(0, 1)) - 1) : 
				benchmark.equals("disabled") ? 0 : cfg.getInt("benchmark");
		
		Solver solver = new Solver(() -> {
			return mode.equals("ACO") ? new ACOAlgorithm(instance, cfg) :
				mode.equals("PSO") ? new PSOAlgorithm(instance, cfg) : null;
		}, threads);
		
		solver.solve(maxIterations, epochSize, (bestAlgorithm) -> {
			Integer[] bestSolution = bestAlgorithm.getBestSolution();
			int bestMakespan = bestAlgorithm.computeMakespan(bestSolution);
			
			System.out.println("\nGlobal best makespan: " + bestMakespan);
			
			if(benchmarkMakespan != 0)
				System.out.println("(benchmark: " + benchmarkMakespan + "; " + (100 * (bestMakespan - benchmarkMakespan) / (float) benchmarkMakespan) + "% off)");
			
			GanttChart gc = bestAlgorithm.createGanttChart(bestSolution);
			System.out.println("Gantt chart validity test: " + gc.test());
			System.out.println("Saving Gantt chart image...");
			saveGanttChartImage(instance, gc, outputDirectory);
			System.out.println("Done!");
		});
	}

	
	/*Runnable onTermination = () ->  {
		System.out.println("TERMINATION");
	};
	// Define the shutdown hook to execute on termination
	Runtime.getRuntime().addShutdownHook(new Thread(onTermination));*/

	
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
	private static void saveGanttChartImage(ProblemInstance pi, GanttChart gc, String outputDirectory) {
		BufferedImage img = gc.generateImage();
		int makespan = gc.getEndTime();
		
		try {
	    	// Create output directory
	    	new File(outputDirectory).mkdir();
	    	
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
	    	File outputFile = new File(outputDirectory + fileName);
	    	ImageIO.write(img, "png", outputFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
