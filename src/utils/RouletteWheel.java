package utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

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
		int length = cumulativeWeights.length - 1;
		float sumOfWeights = cumulativeWeights[length];
		float r = sumOfWeights * rand.nextFloat();
		
		// Look for the index of the entry just above r
		int a = 0, b = length - 1;
	    while(b-a > 1) {
	        int mid = (a + b) / 2;
	        if(cumulativeWeights[mid] > r)
	        	b = mid;
	        else
	        	a = mid;
	    }
	    
	    return a;	
	}
	
	public static int spinOnce(Random rand, float[] probabilities) { 
		//return spin(rand, createCumulativeWeights(probabilities));
		return spin(rand, createMap(probabilities));
	}
	
	/*public static void main(String[] args) {
		float[] probas = new float[] {1.0f, 1.0f, 1.0f};
		for(int i = 0; i < 10; i++)
			System.out.println(spinOnce(new Random(), probas));
	}*/
	
    public static NavigableMap<Float, Integer> createMap(float[] probabilities) {
    	NavigableMap<Float, Integer> map = new TreeMap<Float, Integer>();
        float total = 0;
        for(int i = 0; i < probabilities.length; i++) {
        	if(probabilities[i] != 0) {
        		total += probabilities[i];
        		map.put(total, i);
        	}
        }
        return map;
    }

    public static int spin(Random rand, NavigableMap<Float, Integer> map) {
    	if(map.size() == 1)
    		return map.firstEntry().getValue();
    	float total = map.lastKey();
        float value = rand.nextFloat() * total;
        return map.higherEntry(value).getValue();
    }
}
