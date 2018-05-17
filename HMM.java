import java.io.*;
import java.util.*;

public class HMM {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static String textFolder = "inputs/ps5/";
	static String textSubject = "simple";
	
	public static BufferedReader load(String URL) {
		try {return new BufferedReader(new FileReader(URL));}
		catch (FileNotFoundException e) {e.printStackTrace(); return null;} 
	}
	public static BufferedWriter write(String URL) {
		try {return new BufferedWriter(new FileWriter(URL));}
		catch (FileNotFoundException e) {e.printStackTrace(); return null;}
		catch (IOException e) {e.printStackTrace();; return null;}
	}
	
	public static void main(String[] args) throws IOException {

		// Declare general variables.
		String tagLine, sentenceLine;
		String[] splitTagLine, splitSentenceLine;
		
		/*
		 * THE EASY PART: TRAINING THE MAP AND ADJACENCY GRAPH.
		 * Basically done. We still need to test. We might also want to convert from a flat integer adjacency graph to some sort of probability-based solution.
		 */
		// Create bufferedReaders training files
		BufferedReader trainSentences = load(textFolder + textSubject + "-train-sentences.txt");
		BufferedReader trainTags = load(textFolder + textSubject + "-train-tags.txt");
		
		// Generate all required data structures for training
		HashMap<String, HashMap<String, Integer>> POSWords = new HashMap<String, HashMap<String, Integer>>(); // Inputs POS, get out HashMap of Words with # of times used as POS
		AdjacencyMapGraph<String, Integer> POSTransitions = new AdjacencyMapGraph<String, Integer>(); // Inputs POS, gets # of times it transitions to other POS
		
		// Put all the parts of speech we need into a list.
		ArrayList<String> POSList = new ArrayList<String>(
				Arrays.asList(
						"ADJ", "ADV", "CNJ", "DET", "EX", "FW", "MOD", 
						"N", "NP", "NUM", "PRO", "P", "TO", "UH", 
						"V", "VD", "VG", "VN", "WH", "#"
				)
		);
		// Add all parts of speech as objects into the graph.
		for (String POS : POSList) {
			POSTransitions.insertVertex(POS);
			POSWords.put(POS, new HashMap<String, Integer>());
		}
		// READ ALL WORDS INTO PARTS OF SPEECH MAP.
		// While there is another line, read it.
		while ((tagLine = trainTags.readLine()) != null) {
			sentenceLine = trainSentences.readLine();
			// Split your lines.
			splitTagLine = tagLine.split(" ");
			splitSentenceLine = sentenceLine.split(" ");
			// Iterate through the split lines.
			String currentTag = "#";
			for(int i = 0; i < splitTagLine.length; i++) {
				// Get your next tag.
				String nextTag = splitTagLine[i];
				// Add next tag to POS map.
				HashMap<String, Integer> POSWord = POSWords.get(nextTag);
				if (POSWord.containsKey(splitSentenceLine[i])) {
					int numberOfTimesThatThisWordHasBeenUsedAsThisPOS = POSWord.get(splitSentenceLine[i]);
					POSWord.put(splitSentenceLine[i], numberOfTimesThatThisWordHasBeenUsedAsThisPOS + 1);
				}
				else {
					POSWord.put(splitSentenceLine[i], 1);
				}
				// Get the number of times POS1 has gone to POS2
				int n;
				if (POSTransitions.getLabel(currentTag, nextTag) == null) { n = 1;}
				else { 
					n = POSTransitions.getLabel(currentTag, nextTag) + 1; 
					POSTransitions.removeDirected(currentTag, nextTag);
				}
				// Insert a directed edge with 1 more than before
				POSTransitions.insertDirected(currentTag, nextTag, n);
				// Reset currentTag to nextTag
				currentTag = nextTag;
			}
		}
		// Close files.
		trainSentences.close();
		trainTags.close();
		
		/*
		 * THE HARD PART: INPUTTING TEST FILES
		 */
		
		// Create bufferedReader for testing sentences
		BufferedReader testSentences = load(textFolder + textSubject + "-test-sentences.txt");
		BufferedWriter resultTagsIn = write(textFolder + textSubject + "-result-tags.txt");
		// Create required data structures for testing files
		HashMap<String, Integer> currentStates = new HashMap<String, Integer>();
		HashMap<String, Integer> previousStates = new HashMap<String, Integer>();
		HashMap<String, Integer> currentScores = new HashMap<String, Integer>();
		HashMap<String, Integer> previousScores = new HashMap<String, Integer>();
		ArrayList<HashMap<String, String>> backtraces = new ArrayList<HashMap<String, String>>();
		
//		currStates = { start }
//		currScores = map { start=0 }
//		for i from 0 to # observations - 1
		while ((sentenceLine = testSentences.readLine()) != null) {
			splitSentenceLine = sentenceLine.split(" ");
			
			
			
			
			
			
			
			// WHAT THE FUCK DO WE DO HERE, IS THE REAL QUESTION.
			
			for(int i = 0; i < splitSentenceLine.length; i++) {
				// Get your next tag.
				String nextWord = splitSentenceLine[i];
				// TODO: Everything
			}
			
//			  nextStates = {}
//			  nextScores = empty map
//			  for each currState in currStates
//			    for each transition currState -> nextState
//			      add nextState to nextStates
//			      nextScore = currScores[currState] +                       // path to here
//			                  transitionScore(currState -> nextState) +     // take a step to there
//			                  observationScore(observations[i] in nextState) // make the observation there
//			      if nextState isn't in nextScores or nextScore > nextScores[nextState]
//			        set nextScores[nextState] to nextScore
//			        remember that pred of nextState @ i is curr
//			  currStates = nextStates
//			  currScores = nextScores
//			score += value of transition between POS + value of word given POS
			
			
			
			
			
			
			
			
			// BACKTRACE! First, generate an array list of POS. Then, iterate through that list to generate a string. Then, copy that string into an output file.
			// Generate the list.
			String currentPOS = "";
			ArrayList<String> backtracedListPOS = new ArrayList<String>();
			int layersBack = 0;
			while (!currentPOS.equals("#")) {
				backtracedListPOS.add(currentPOS);
				HashMap<String, String> nextLayer = backtraces.get(backtraces.size() - 1 - layersBack);
				currentPOS = nextLayer.get(currentPOS);
				layersBack ++;
			}
			// Write generated words to a string.
			String backtracedStringPOS = "";
			for (int i = 0; i < backtracedListPOS.size(); i ++) {
				backtracedStringPOS += backtracedListPOS.get(backtracedListPOS.size() - 1 - i);
			}
			// Write backtraced string into an output file.
			resultTagsIn.write(backtracedStringPOS);
		}
		
		// Close testing file and results writer
		testSentences.close();
		resultTagsIn.close();
		
		
		// TEST RESULT TAGS AGAINST GIVEN TAGS
		// Open test and result tags
		BufferedReader testTags = load(textFolder + textSubject + "-test-tags.txt");
		BufferedReader resultTagsOut = load(textFolder + textSubject + "-result-tags.txt");
		// Run through each line of the test and result tags
		String testTagsLine, resultTagsOutLine;
		String[] splitTestTagsLine, splitResultTagsOutLine;
		String testTag, resultTagOut;
		int goodCalls = 0, badCalls = 0;
		while ((testTagsLine = testTags.readLine()) != null && (resultTagsOutLine = resultTagsOut.readLine()) != null) {
			splitTestTagsLine = testTagsLine.split(" "); splitResultTagsOutLine = resultTagsOutLine.split(" ");
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
}
