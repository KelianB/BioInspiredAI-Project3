package jssp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Handles reading JSSP problem instances
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemReader {
	/**
	 * Create a Problem Reader
	 */
	public ProblemReader() {
		
	}
	
	/**
	 * Reads a problem instance from the given problem name.
	 * @param path - The path to the problem instance file
	 * @return a problem instance
	 * @throws ProblemReadingException
	 */
	public ProblemInstance readProblem(String path) {
		File file = new File(path);
		
		// Use the file name as the problem instance name
		String name = file.getName();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			// Read meta information
			int[] meta = parseLineOfIntegers(br.readLine());
			int numJobs = meta[0], numMachines = meta[1];

			Operation[][] jobs = new Operation[numJobs][numMachines];

			// Parse jobs
			for(int i = 0; i < numJobs; i++) {
				int[] jobData = parseLineOfIntegers(br.readLine());
				for(int j = 0; j < numMachines; j++) {
					int machine = jobData[j*2];
					int time = jobData[j*2+1];
					jobs[i][j] = new Operation(i, machine, time);
				}
			}
			
			br.close();
			
			return new ProblemInstance(name, jobs);
		}
		catch(IOException e) {
			e.printStackTrace();
			System.err.println("Unable to read problem instance file " + path);
		}
			
		return null;
	}
	
	/**
	 * Parses a string of integers separated by spaces.
	 * @param str - A string of integers separated by spaces, e.g. "  2   1  0   3  1   6  3   7"
	 * @return an array of integers found in the given string
	 */
	private static int[] parseLineOfIntegers(String str) {
		String[] segments = str.trim().split(" ");
		return Arrays.stream(segments)
				.filter((s) -> s.trim().length() > 0) // filter out empty sequences
				.mapToInt(Integer::parseInt) // map from string to int
				.toArray(); // return array
	}
}
