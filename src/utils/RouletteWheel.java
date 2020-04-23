package utils;

import java.util.Random;

public class RouletteWheel {
    // Build a vector of cumulative fitnesses (allows for O(log2(n)) roulette wheel selection)
	public static float[] createCumulativeWeights(float[] probabilities) {
		float[] w = new float[probabilities.length+1];
		float total = 0;
		
	    for(int i = 0; i < probabilities.length; i++) {
	        total += probabilities[i];        
	        w[i+1] = total;
	    }
	    return w;
	}
	

	/** Uses binary search to achieve O(log2(n)) time complexity **/
	public static int spin(Random rand, float[] cumulativeWeights) {
		float sumOfWeights = cumulativeWeights[cumulativeWeights.length - 1];
		float r = sumOfWeights * rand.nextFloat();
		
		// Look for the index of the entry just above r
		int a = 0, b = cumulativeWeights.length - 1;
	    while(b-a > 1) {
	        int mid = (a + b) / 2;
	        if(cumulativeWeights[mid] > r)
	        	b = mid;
	        else
	        	a = mid;
	    }
	    
	    return a;	
	}
	
	/**
	 * Spin a single roulette wheel (weighted choice) with the given probabilities.
	 * @param rand - A random generator
	 * @param probabilities - An array of probabilities (or weights)
	 * @return an index between 0 (inclusive) and the length of the probabilities array (exclusive)
	 */
	public static int spinOnce(Random rand, float[] probabilities) { 
		if(probabilities.length == 1)
			return 0;
		
		return spin(rand, createCumulativeWeights(probabilities));
	}
	
	/* Test of Roulette Wheel implementation */
	/*public static void main(String[] args) {
		Random r = new Random();
		
		float[] probas = new float[] {0f, 1f, 2f};
		float[] weights = createCumulativeWeights(probas);
		
		HashMap<Integer, Integer> outputs = new HashMap<Integer, Integer>();
		for(int i = 0; i < probas.length; i++)
			outputs.put(i, 0);
		
		int iters = 1000;
		
		for(int i = 0; i < iters; i++) {
			int output = spin(r, weights);
			outputs.put(output, outputs.get(output) + 1);
		}
		
		for(int i = 0; i < probas.length; i++)
			System.out.println("Output " + i + ": " + 100.0f * outputs.get(i) / iters + "%");
	}*/
}
