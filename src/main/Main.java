package main;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	/*public static enum Mode {WEIGHTED_SUM_GA, MOEA};
	public static Mode mode;
	
	public static Config config;
	
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
