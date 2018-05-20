import java.io.*;
import java.util.*;

public class BigramModeler {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static final String textFolder = "inputs/ps5/";
	static final String textSubject = "brown";
	static final String sentenceStarter = "#";
	
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
		try {
			// Declare general variables.
			String tagLine, sentenceLine;
			String[] splitTagLine, splitSentenceLine;
			
			/*
			 * TRAINING THE MAP AND ADJACENCY GRAPH.
			 */
			
			System.out.println("TRAINING BEGINS");
			
			// Create bufferedReaders training files
			BufferedReader trainSentences = load(textFolder + textSubject + "-train-sentences.txt");
			BufferedReader trainTags = load(textFolder + textSubject + "-train-tags.txt");
			
			// Generate all required data structures for training
			/**
			 * THE FOLLOWING MIGHT MAKE MORE SENSE WITH WORDS AS KEYS AND POS AS VALUE
			 */
			// Inputs word, get out HashMap of POS with # of times used as POS
			HashMap<String, HashMap<String, Double>> POSWords = new HashMap<String, HashMap<String, Double>>(); 
			// Inputs POS, gets # of times it transitions to other POS
			AdjacencyMapGraph<String, Double> POSTransitions = new AdjacencyMapGraph<String, Double>(); 
			
			// Put all the parts of speech we need into a list.
			ArrayList<String> POSList = new ArrayList<String>(
					Arrays.asList(
							"ADJ", "ADV", "CNJ", "DET", "EX", "FW", "MOD", 
							"N", "NP", "NUM", "PRO", "P", "TO", "UH", 
							"V", "VD", "VG", "VN", "WH", ".", sentenceStarter,
							",", "``", "VBZ", "''", "*", ":", "(", ")", "--", "'", "NIL" // for the brown corpus
					)
			);
			// Add all parts of speech as objects into the graph.
			for (String POS : POSList) {
				POSTransitions.insertVertex(POS);
			}
			
			// READ ALL WORDS INTO PARTS OF SPEECH MAP.
			// While there is another line, read it.
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
					if (POSWord.containsKey(nextTag)) {
						Double numberOfTimesThatThisWordHasBeenUsedAsThisPOS = POSWord.get(nextTag);
						POSWord.put(nextTag, numberOfTimesThatThisWordHasBeenUsedAsThisPOS + 1);
					}
					else {
						POSWord.put(nextTag, 1.0);
					}
					POSWords.put(nextWord, POSWord);
					// Get the number of times POS1 has gone to POS2
					Double n;
					if (POSTransitions.getLabel(currentTag, nextTag) == null) { n = 1.0;}
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
			Iterator<String> vertexIterator = POSTransitions.vertices().iterator();
			Iterator<String> neighborIterator;
			while(vertexIterator.hasNext()) {
				String currentVertex = vertexIterator.next();
				// Iterate through neighbors of current vertex, get the total number of hits
				neighborIterator = POSTransitions.outNeighbors(currentVertex).iterator();
				Double currentValue, sum = 0.0;
				while (neighborIterator.hasNext()) {
					String neighborVertex = neighborIterator.next();
					currentValue = POSTransitions.getLabel(currentVertex, neighborVertex);
					sum += currentValue;
				}
				ArrayList<String> toBeRemoved = new ArrayList<String>();
				// Iterate through neighbors again, changing routes to percentage hits
				
				// MAKE MY FAKE LIST, AND ADD EVERYTHING TO IT
				neighborIterator = POSTransitions.outNeighbors(currentVertex).iterator();
				while (neighborIterator.hasNext()) {
					String neighborVertex = neighborIterator.next();
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
				
				// Get the sum of hits
				Double sum = 0.0;
				for (String POS : POSWords.get(word).keySet()) {
					sum += POSWords.get(word).get(POS);
				}
				// Turn all hits into percentages
				for (String POS : POSWords.get(word).keySet()) {
					POSWords.get(word).put(POS, Math.log(POSWords.get(word).get(POS) / sum));
				}
			}
			
			/*
			 * INTERPRETING TEST FILES.
			 */
			
			System.out.println("TESTING BEGUN");
			System.out.println(POSTransitions);
			
			// Create bufferedReader for testing sentences and bufferedWriter for putting output.
			BufferedReader testSentences = load(textFolder + textSubject + "-test-sentences.txt");
			BufferedWriter resultTagsIn = write(textFolder + textSubject + "-result-tags.txt");
			
			// Allocate memory to stuff that we need.
			HashMap<String, Double> currentScores, nextScores;
			String currentPOS; 
			HashMap<String, String> thisFrame, nextLayer;
			ArrayList<HashMap<String, String>> backtraces;
			Double currentScore, transitionScore, observationScore, nextScore, bestEndValue;
			Iterator<String> currentScoresIterator, neighborPOSs;
			
			// Keep going until we run out of lines to read.
			Boolean ranOnce = false;
			while ((sentenceLine = testSentences.readLine()) != null) {
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
					currentScoresIterator = currentScores.keySet().iterator();
					while (currentScoresIterator.hasNext()) {
						currentPOS = currentScoresIterator.next();
						// Making a score for the current state to the next state with the next word.
						currentScore = currentScores.get(currentPOS);
						neighborPOSs = POSTransitions.outNeighbors(currentPOS).iterator();
						while (neighborPOSs.hasNext()) {
							String nextPOS = neighborPOSs.next();
							transitionScore = POSTransitions.getLabel(currentPOS, nextPOS);
							// If the word has never been used as this type, give it a -10.0 score.
							if (POSWords.get(nextWord) == null) {
								observationScore = -100.0;
							}
							else if (POSWords.get(nextWord).get(nextPOS) == null) { 
								observationScore = -100.0; 
							}
							// Else, the word exists and has been used as this type. Give it its proper score.
							else { observationScore = POSWords.get(nextWord).get(nextPOS); }
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
					ranOnce = true;
				}
				
				// BACKTRACE! Generate array POS, iterate to generate a string, copy string into an output file.
				
				// Get the best end POS.
				bestEndValue = nextScores.get(nextScores.keySet().iterator().next());
				currentPOS = sentenceStarter;
				for (String POS : nextScores.keySet()) {
					if (bestEndValue < nextScores.get(POS)) {
						bestEndValue = nextScores.get(POS);
						currentPOS = POS;
					}
				}
				
				// Generate the list.
				ArrayList<String> backtracedListPOS = new ArrayList<String>();
				int layersBack = 0;
				while (currentPOS != sentenceStarter) {
					backtracedListPOS.add(currentPOS);
					nextLayer = backtraces.get(backtraces.size() - 1 - layersBack);
					currentPOS = nextLayer.get(currentPOS);
					layersBack ++;
				}
								
				// Write generated words to a string.
				String backtracedStringPOS = "";
				for (int i = 0; i < backtracedListPOS.size(); i ++) {
					backtracedStringPOS += backtracedListPOS.get(backtracedListPOS.size() - 1 - i) + " ";
				}
				// Write backtraced string into an output file.
				resultTagsIn.write(backtracedStringPOS + "\n");
			}
			
			// Close testing file and results writer
			testSentences.close();
			resultTagsIn.close();
			
			/*
			 * CHECK TAGS AGAINST GIVEN.
			 */
			
			// Open test and result tags
			BufferedReader testTags = load(textFolder + textSubject + "-test-tags.txt");
			BufferedReader resultTagsOut = load(textFolder + textSubject + "-result-tags.txt");
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
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
