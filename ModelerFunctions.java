import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ModelerFunctions {
	/*
	 * LOAD A NEW FILE.
	 */
	public static BufferedReader load(String URL) {
		try {return new BufferedReader(new FileReader(URL));}
		catch (FileNotFoundException e) {e.printStackTrace(); return null;} 
	}
	/*
	 * WRITE INTO A NEW FILE.
	 */
	public static BufferedWriter write(String URL) {
		try {return new BufferedWriter(new FileWriter(URL));}
		catch (FileNotFoundException e) {e.printStackTrace(); return null;}
		catch (IOException e) {e.printStackTrace();; return null;}
	}
	/*
	 * CONVERT FROM ROTE TO LOG.
	 */
	public static void logify(HashMap<String,Double> myMap) {
		// Get the sum of hits
		Double sum = 0.0;
		for (String key : myMap.keySet()) {
			sum += myMap.get(key);
		}
		// Turn all hits into percentages
		for (String key : myMap.keySet()) {
			myMap.put(key, Math.log(myMap.get(key) / sum));
		}
	}
	public static String getBestFromHashMapStringDouble(HashMap<String,Double> myMap) {
		String bestPOS = myMap.keySet().iterator().next();
		Double bestEndValue = myMap.get(myMap.keySet().iterator().next());
		for (String POS : myMap.keySet()) {
			if (bestEndValue < myMap.get(POS)) {
				bestEndValue = myMap.get(POS);
				bestPOS = POS;
			}
		}
		return bestPOS;
	}
	public static <V, E> void insertDirectedWithVertices(V u, V v, E n, AdjacencyMapGraph<V,E> map) {
		if (!map.hasVertex(u)) { map.insertVertex(u); }
		if (!map.hasVertex(v)) { map.insertVertex(v); }
		map.insertDirected(u, v, n);
	}
	/*
	 * CHECK TAGS AGAINST GIVEN.
	 */
	public static void checkTags(String testFolderURL, String resultsFolderURL) {
		try {
			// Open test and result tags
			BufferedReader testTags = load(testFolderURL);
			BufferedReader resultTagsOut = load(resultsFolderURL);
			// Run through each line of the test and result tags
			String testTagsLine, resultTagsOutLine;
			String[] splitTestTagsLine, splitResultTagsOutLine;
			String testTag, resultTagOut;
			int goodCalls = 0, badCalls = 0;
			while ((testTagsLine = testTags.readLine()) != null 
					&& (resultTagsOutLine = resultTagsOut.readLine()) != null) {
				splitTestTagsLine = testTagsLine.split(" "); 
				splitResultTagsOutLine = resultTagsOutLine.split(" ");
				for (int i = 0; i < splitTestTagsLine.length; i ++) {
					testTag = splitTestTagsLine[i]; resultTagOut = splitResultTagsOutLine[i];
					if (testTag.equals(resultTagOut)) { goodCalls ++; }
					else { badCalls ++; }
				}
			}
			// Close testing tags and result tags
			testTags.close();
			resultTagsOut.close();
			// Print results
			System.out.println("Good calls: " + String.valueOf(goodCalls));
			System.out.println("Bad calls: " + String.valueOf(badCalls));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
