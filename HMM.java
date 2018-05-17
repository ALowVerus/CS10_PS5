import java.io.*;
import java.util.*;

public class HMM {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static String textFolder = "inputs/ps5/";
	static String textSubject = "simple";
	
	public static void main(String[] args) {
		
		try {
			
			// Declare variables.
			String tagLine, sentenceLine;
			String[] splitTagLine, splitSentenceLine;
			
			/*
			 * THE EASY PART: TRAINING THE MAP AND ADJACENCY GRAPH
			 */
			
			// Create bufferedReaders training files
			BufferedReader trainSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-train-sentences.txt"));
			BufferedReader trainTags = new BufferedReader(new FileReader(textFolder + textSubject + "-train-tags.txt"));
			
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
			
			trainSentences.close();
			trainTags.close();
			
//			// Turn the <String, Integer> Adjacency Map into a <String, Double> with percentages.
//			AdjacencyMapGraph<String, Double> POSPercentages = new AdjacencyMapGraph<String, Double>();
//			for (String vertex : POSTransitions.vertices()) {
//				
//			}
			
			/*
			 * THE HARD PART: INPUTTING TEST FILES
			 */
			
			// Create bufferedReader for testing sentences
			BufferedReader testSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-test-sentences.txt"));
			
			// Create required data structures for testing files
			ArrayList backtraces = new ArrayList<String>();
			HashMap currentStates = new HashMap<String, Integer>();
			HashMap previousStates = new HashMap<String, Integer>();
			HashMap currentScores = new HashMap<String, Integer>();
			HashMap previousScores = new HashMap<String, Integer>();
			
//			currStates = { start }
//			currScores = map { start=0 }
//			for i from 0 to # observations - 1
			while ((sentenceLine = testSentences.readLine()) != null) {
				splitSentenceLine = sentenceLine.split(" ");
				
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
			
			// Close testing file
			testSentences.close();
			
			
			// Test results against actual tags
			BufferedReader testTags = new BufferedReader(new FileReader(textFolder + textSubject + "-test-tags.txt"));
			/* 
			 * DO STUFF
			 */
			testTags.close();
		} 
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
