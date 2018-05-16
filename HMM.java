import java.io.*;
import java.util.*;

public class HMM {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static String textFolder = "inputs/ps5/";
	static String textSubject = "simple";
	
//	currStates = { start }
//	currScores = map { start=0 }
//	for i from 0 to # observations - 1
//	  nextStates = {}
//	  nextScores = empty map
//	  for each currState in currStates
//	    for each transition currState -> nextState
//	      add nextState to nextStates
//	      nextScore = currScores[currState] +                       // path to here
//	                  transitionScore(currState -> nextState) +     // take a step to there
//	                  observationScore(observations[i] in nextState) // make the observation there
//	      if nextState isn't in nextScores or nextScore > nextScores[nextState]
//	        set nextScores[nextState] to nextScore
//	        remember that pred of nextState @ i is curr
//	  currStates = nextStates
//	  currScores = nextScores
	
	public static void main(String[] args) {
		
		try {
			// Create bufferedReaders training files
			BufferedReader trainSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-train-sentences.txt"));
			BufferedReader trainTags = new BufferedReader(new FileReader(textFolder + textSubject + "-train-tags.txt"));
			
			// Generate all required data structures for training
			HashMap POSWords = new HashMap<String, HashMap<String, Integer>>(); // Inputs POS, get out HashMap of Words with # of times used as POS
			AdjacencyMapGraph POSTransitions = new AdjacencyMapGraph<String, Integer>(); // Inputs POS, gets # of times it transitions to other POS
			
			// Read in all words to parts of speech map.
			String line;
			while ((line = trainTags.readLine()) != null) {
				String[] splitLine = line.split(" ");
				for(int i = 0; i < splitLine.length; i++) {
					POSTransitions.setLabel((i, (Integer) ((int)(POSTransitions.get(i)) + (Integer)1));
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
