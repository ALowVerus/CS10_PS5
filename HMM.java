import java.io.*;
import java.util.*;

public class HMM {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static String textFolder = "inputs/ps5/";
	static String textSubject = "simple";
	
//	currStates = { start }
//	currScores = map { start=0 }
//	for i from 0 to # observations - 1
//	  nextTagStates = {}
//	  nextTagScores = empty map
//	  for each currState in currStates
//	    for each transition currState -> nextTagState
//	      add nextTagState to nextTagStates
//	      nextTagScore = currScores[currState] +                       // path to here
//	                  transitionScore(currState -> nextTagState) +     // take a step to there
//	                  observationScore(observations[i] in nextTagState) // make the observation there
//	      if nextTagState isn't in nextTagScores or nextTagScore > nextTagScores[nextTagState]
//	        set nextTagScores[nextTagState] to nextTagScore
//	        remember that pred of nextTagState @ i is curr
//	  currStates = nextTagStates
//	  currScores = nextTagScores
	
	public static void main(String[] args) {
		
		try {
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
			// Declare variables.
			String tagLine, trainLine;
			// While there is another line, read it.
			while ((tagLine = trainTags.readLine()) != null) {
				trainLine = trainSentences.readLine();
				// Split your lines.
				String[] splitTagLine = tagLine.split(" ");
				String[] splitTrainLine = trainLine.split(" ");
				// Iterate through the split lines.
				String currentTag = "#";
				for(int i = 0; i < splitTagLine.length; i++) {
					
					// Get your next tag.
					String nextTag = splitTagLine[i];
					
					// Add next tag to POS map.
					HashMap<String, Integer> POSWord = POSWords.get(nextTag);
					if (POSWord.containsKey(splitTrainLine[i])) {
						int numberOfTimesThatThisWordHasBeenUsedAsThisPOS = POSWord.get(splitTrainLine[i]);
						POSWord.put(splitTrainLine[i], numberOfTimesThatThisWordHasBeenUsedAsThisPOS + 1);
					}
					else {
						POSWord.put(splitTrainLine[i], 1);
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
			
			
			// Create bufferedReaders for testing files
			BufferedReader testSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-test-sentences.txt"));
			BufferedReader testTags = new BufferedReader(new FileReader(textFolder + textSubject + "-test-tags.txt"));
			
			// Create required data structures for testing files
			ArrayList backtraces = new ArrayList<String>();
			HashMap currentScores = new HashMap<String, Integer>();
			HashMap nextScores = new HashMap<String, Integer>();
			
			// Close testing files
			testSentences.close();
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
