import java.io.*;
import java.util.*;

public class BigramModeler {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static final String textURL = "inputs/ps5/brown";
	static final String sentenceStarter = "#";
	static final Double nullScore = -9.5;
	
	public static ArrayList training(String sentenceURL, String tagURL) throws IOException {
		
		String tagLine, sentenceLine;
		String[] splitTagLine, splitSentenceLine;
		/*
		 * TRAINING THE MAP AND ADJACENCY GRAPH.
		 */
		
		System.out.println("TRAINING BEGINS");
		
		// Inputs word, get out HashMap of POS with # of times used as POS
		HashMap<String, HashMap<String, Double>> POSWords = new HashMap<String, HashMap<String, Double>>(); 
		// Inputs POS, gets # of times it transitions to other POS
		AdjacencyMapGraph<String, Double> POSTransitions = new AdjacencyMapGraph<String, Double>(); 
		
		// Create bufferedReaders training files
		BufferedReader trainSentences = ModelerFunctions.load(sentenceURL);
		BufferedReader trainTags = ModelerFunctions.load(tagURL);
		
		// READ ALL WORDS INTO PARTS OF SPEECH MAP.
		// While there is another line, read it.
		POSTransitions.insertVertex(sentenceStarter);
		while ((tagLine = trainTags.readLine()) != null) {
			sentenceLine = trainSentences.readLine();
			// Split your lines.
			splitTagLine = tagLine.split(" ");
			splitSentenceLine = sentenceLine.split(" ");
			// Iterate through the split lines.
			String currentTag = sentenceStarter;
			for(int i = 0; i < splitTagLine.length; i++) {
				// Get your next tag and word.
				String nextTag = splitTagLine[i];
				String nextWord = splitSentenceLine[i];
				// Add next tag to POS map.
				HashMap<String, Double> POSWord = POSWords.get(nextWord);
				// Check that the word exists, and if it doesn't, initialize to a blank
				if (POSWord == null) {
					POSWord = new HashMap<String,Double>();
				}
				// If the word has already been seen as this POS, increment.
				if (POSWord.containsKey(nextTag)) {
					POSWord.put(nextTag, POSWord.get(nextTag) + 1);
				}
				// If the word has not been seen as this POS, initialize.
				else {
					POSWord.put(nextTag, 1.0);
				}
				POSWords.put(nextWord, POSWord);
				
				// Check that POS2 is in the graph
				if (!POSTransitions.hasVertex(nextTag)) { 
					POSTransitions.insertVertex(nextTag); 
				}
				// Get the number of times POS1 has gone to POS2
				Double n;
				if (POSTransitions.getLabel(currentTag, nextTag) == null) { 
					n = 1.0;
				}
				else { 
					n = POSTransitions.getLabel(currentTag, nextTag) + 1.0; 
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
		
		System.out.println("TRAINING COMPLETE");
		
		/*
		 * CONVERTING THE MAP AND ADJACENCY GRAPH TO PERCENTAGE HITS.
		 */
		
		// Change the POSTransitions graph from using rote numbers to percentage hits.
		Iterator<String> neighborIterator;
		for (String currentVertex : POSTransitions.vertices()) {
			// Iterate through neighbors of current vertex, get the total number of hits
			Double currentValue, sum = 0.0;
			for (String neighborVertex : POSTransitions.outNeighbors(currentVertex)) {
				currentValue = POSTransitions.getLabel(currentVertex, neighborVertex);
				sum += currentValue;
			}
			ArrayList<String> toBeRemoved = new ArrayList<String>();
			// Iterate through neighbors again, changing routes to percentage hits
			
			// MAKE MY FAKE LIST, AND ADD EVERYTHING TO IT
			for (String neighborVertex : POSTransitions.outNeighbors(currentVertex)) {
				toBeRemoved.add(neighborVertex);
			}
			
			for(String i: toBeRemoved) {
				Double initialValue = POSTransitions.getLabel(currentVertex, i);
				POSTransitions.removeDirected(currentVertex, i);
				POSTransitions.insertDirected(currentVertex, i, Math.log(initialValue/sum));
			}
		}
		
		// Change the POSWords map from using rote numbers to percentage hits.
		for (String word : POSWords.keySet()) {
			ModelerFunctions.logify(POSWords.get(word));
		}
		
		ArrayList data = new ArrayList();
		data.add(POSWords);
		data.add(POSTransitions);
		return data;
	}
	
	/*
	 * Return the parts of speech for a given string.
	 */
	public static String viterbi(ArrayList data, String sentenceLine) {
		HashMap<String, HashMap<String, Double>> POSWords = (HashMap<String, HashMap<String, Double>>)data.get(0);
		AdjacencyMapGraph<String, Double> POSTransitions = (AdjacencyMapGraph<String, Double>)data.get(1);
		// Allocate memory to stuff that we need.
		HashMap<String, Double> currentScores, nextScores; 
		HashMap<String, String> thisFrame, nextLayer;
		ArrayList<HashMap<String, String>> backtraces;
		Double currentScore, transitionScore, observationScore, nextScore;
		Iterator<String> neighborPOSs;
		String[] splitSentenceLine;
		
		// Create required data structures for testing files, regenerate each line
		currentScores = new HashMap<String, Double>(); 
		currentScores.put(sentenceStarter, 0.0);
		nextScores = new HashMap<String, Double>();
		backtraces = new ArrayList<HashMap<String, String>>();
		
		// Iterate through each line in the sentence.
		splitSentenceLine = sentenceLine.split(" ");
		for (String nextWord : splitSentenceLine) {
			// Make new backpointer frame for this word.
			thisFrame = new HashMap<String, String>(); // KEY:VALUE = NEXT_POS:CURRENT_POS
			nextScores = new HashMap<String, Double>();
			// Iterate through current scores.
			for (String currentPOS : currentScores.keySet()) {
				// Making a score for the current state to the next state with the next word.
				currentScore = currentScores.get(currentPOS);
				neighborPOSs = POSTransitions.outNeighbors(currentPOS).iterator();
				while (neighborPOSs.hasNext()) {
					String nextPOS = neighborPOSs.next();
					transitionScore = POSTransitions.getLabel(currentPOS, nextPOS);
					// If the word has never been used as this type, give it a -10.0 score.
					observationScore = nullScore;
					// If the word has been used before as this POS, give it an appropriate score.
					if (POSWords.keySet().contains(nextWord)) {
						if (POSWords.get(nextWord).keySet().contains(nextPOS)) { 
							observationScore = POSWords.get(nextWord).get(nextPOS);
						}
					}
					nextScore = currentScore + transitionScore + observationScore;
					// If you haven't seen this next state before, put in your score for the next state
					if (nextScores.get(nextPOS) == null || nextScores.get(nextPOS) <= nextScore) {
						nextScores.put(nextPOS, nextScore);
						thisFrame.put(nextPOS, currentPOS);
					}
				}
				
			}
			// Reset states and scores after you've iterated. Clear and put, rather than set=.
			currentScores.clear();
			currentScores.putAll(nextScores); // This doesn't trigger ConcurrentModificationException.
			backtraces.add(thisFrame);
		}
		
		// BACKTRACE! Generate array POS, iterate to generate a string, copy string into an output file.
		
		System.out.println("\nI just went through sentence.");
		for (HashMap<String, String> frame : backtraces) {
			System.out.println(frame);
		}
		
		// Get the best end POS.
		String tracingPOS = ModelerFunctions.getBestFromHashMapStringDouble(nextScores);
		
		// Generate the list.
		ArrayList<String> backtracedListPOS = new ArrayList<String>();
		int layersBack = 0;
		while (tracingPOS != sentenceStarter) {
			backtracedListPOS.add(tracingPOS);
			nextLayer = backtraces.get(backtraces.size() - 1 - layersBack);
			tracingPOS = nextLayer.get(tracingPOS);
			layersBack ++;
		}
						
		// Write generated words to a string.
		String backtracedStringPOS = "";
		for (int i = 0; i < backtracedListPOS.size(); i ++) {
			backtracedStringPOS += backtracedListPOS.get(backtracedListPOS.size() - 1 - i) + " ";
		}
		// Write backtraced string into an output file.
		return backtracedStringPOS;
	}
	
	public static void main(String[] args) throws IOException {
		// Create bufferedReader for testing sentences and bufferedWriter for putting output.
		BufferedReader testSentences = ModelerFunctions.load(textURL + "-test-sentences.txt");
		BufferedWriter resultTagsIn = ModelerFunctions.write(textURL + "-bigram-tags.txt");
		
		ArrayList data = training(textURL + "-train-sentences.txt", textURL + "-train-tags.txt");
		
		// Keep going until we run out of lines to read.
		String sentenceLine, viterbiLine;
		while ((sentenceLine = testSentences.readLine()) != null) {
			resultTagsIn.write(viterbi(data, sentenceLine) + "\n");
		}
		
		// Close testing file and results writer
		testSentences.close();
		resultTagsIn.close();
		
		// Check results.
		System.out.println("\n");
		ModelerFunctions.checkTags(textURL + "-test-tags.txt", textURL + "-bigram-tags.txt");
	}
}
