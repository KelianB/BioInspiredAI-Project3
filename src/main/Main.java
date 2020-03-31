package main;

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
		
		// Temporary stuff
		int[] solutions = {0, 56, 1059, 1276, 1130, 1451, 1721, 977};
		int testInstance = 1;
		int optimalMakespan = solutions[testInstance];
		
		int iterations = 50000;
		int epochSize = 1000;
		
		// Read problem
		ProblemInstance instance = reader.readProblem("../../Test Data/" + testInstance + ".txt");
		
		// Create algorithm
		PSOAlgorithm pso = new PSOAlgorithm(instance, config);
		
		pso.printState();
		
		for(int i = 0; i < iterations; i++) {
			pso.runIteration();
			if(pso.getRanIterations() % epochSize == 0) {
				pso.printState();
				System.out.println("Global best makespan: " + (-pso.getSwarm().getGlobalBestFitness()));
			}
		}
		
		int bestMakespan = -pso.getSwarm().getGlobalBestFitness();
		System.out.println("Global best makespan: " + bestMakespan + "  (" + (100 * Math.abs(bestMakespan - optimalMakespan) / optimalMakespan) + "% off)");
		GanttChart gc = PSOAlgorithm.createGanttChart(instance, pso.getSwarm().getGlobalBestPosition());
		System.out.println(gc.test());
		System.out.println(gc);
	}
	
	/*public static enum Mode {WEIGHTED_SUM_GA, MOEA};
	public static Mode mode;
	
	
	private static boolean clearedOutputDirs = false;
	
	public static void main(String[] args) {
		// Read configuration file
		config = new Config("config.properties");
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader(ColorMode.valueOf(config.get("colorMode")), config.getFloat("imageScaling"));
		
		// Read a problem instance
		String inputImagePath = config.get("inputImage");
		final ProblemInstance instance = reader.readProblem(inputImagePath);
		
		// Abort if the problem instance couldn't be read
		if(instance == null) {
			System.err.println("[Critical Error] Couldn't read problem instance.");
			System.exit(1);
		}
		
		// Print information about the problem instance
		System.out.println("Problem instance " + inputImagePath);
		System.out.println("Resized image size from " + instance.getOriginalWidth() + "x" + instance.getOriginalHeight() + 
				" to " + instance.getImage().getWidth() + "x" + instance.getImage().getHeight());
		
		// Get the mode from the config (weighted sum or MOEA)
		mode = Mode.valueOf(config.get("mode"));
		
		// Init GA
		SegmentationGA sga =
			mode == Mode.WEIGHTED_SUM_GA ? new SegmentationGA(instance, config.getFloat("mutationRate"), config.getFloat("crossoverRate")) :
			mode == Mode.MOEA ? new MultiObjectiveSegmentationGA(instance, config.getFloat("mutationRate"), config.getFloat("crossoverRate")) :
			null;
			
		if(sga == null) {
			System.err.println("[Critical Error] Couldn't parse GA mode.");
			System.exit(1);
		}
		
		sga.setElites(config.getInt("elites"));
		sga.initializePopulation();
		
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
