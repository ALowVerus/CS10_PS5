import java.io.*;
import java.util.*;

public class MonogramModeler {
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
			HashMap<String, Double> POSUsage = new HashMap<String, Double>(); 
			
			// READ ALL WORDS INTO PARTS OF SPEECH MAP.
			// While there is another line, read it.
			while ((tagLine = trainTags.readLine()) != null) {
				sentenceLine = trainSentences.readLine();
				// Split your lines.
				splitTagLine = tagLine.split(" ");
				splitSentenceLine = sentenceLine.split(" ");
				// Iterate through the split lines.
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
					if (POSWord.containsKey(nextTag)) { POSWord.put(nextTag, POSWord.get(nextTag) + 1); }
					else { POSWord.put(nextTag, 1.0); }
					POSWords.put(nextWord, POSWord);
					// Get the number of times POS1 has gone to POS2
					if (POSUsage.get(nextTag) == null) { POSUsage.put(nextTag, 1.0); }
					else { POSUsage.put(nextTag, POSUsage.get(nextTag) + 1.0); }
				}
			}
			// Close files.
			trainSentences.close();
			trainTags.close();
			
			System.out.println("TRAINING COMPLETE");
			
			/*
			 * CONVERTING THE MAP AND ADJACENCY GRAPH TO PERCENTAGE HITS.
			 */
			
			// Change the POSUsage map from using rote numbers to percentage hits.
			// Get the sum of hits
			Double POSsum = 0.0;
			for (String POS : POSUsage.keySet()) { POSsum += POSUsage.get(POS); }
			// Turn all hits into percentages, and get most likely
			String bestPOS = POSUsage.keySet().iterator().next();
			Double bestScore = POSUsage.get(bestPOS);
			for (String POS : POSUsage.keySet()) { 
				// If this POS has a higher number of hits, it is the best
				if (POSUsage.get(POS) > POSUsage.get(bestPOS)) { bestPOS = POS; }
				// Log the shit out of this
				POSUsage.put(POS, Math.log(POSUsage.get(POS) / POSsum)); 
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
			
			// Get the best POS, for nulls
			
			
			/*
			 * INTERPRETING TEST FILES.
			 */
			
			System.out.println("TESTING BEGUN");
			
			// Create bufferedReader for testing sentences and bufferedWriter for putting output.
			BufferedReader testSentences = load(textFolder + textSubject + "-test-sentences.txt");
			BufferedWriter resultTagsIn = write(textFolder + textSubject + "-monogram-tags.txt");
			
			String calcTagsString, bestTag, firstTag;
			Double nextTagScore, bestTagScore;
			HashMap<String,Double> wordScores;
			
			// Keep going until we run out of lines to read.
			while ((sentenceLine = testSentences.readLine()) != null) {
				
				// Reset tags and sentence
				calcTagsString = "";
				// Iterate through each line in the sentence.
				splitSentenceLine = sentenceLine.split(" ");
				for (String nextWord : splitSentenceLine) {
					if (POSWords.get(nextWord) != null) {
						wordScores = POSWords.get(nextWord);
					}
					else {
						wordScores = new HashMap<String,Double>();
						for (String POS : POSUsage.keySet()) {
							wordScores.put(POS, 0.0);
						}
					}
					bestTag = wordScores.keySet().iterator().next();
					bestTagScore = wordScores.get(bestTag) + POSUsage.get(bestTag);
					for (String nextTag : wordScores.keySet()) {
						nextTagScore = wordScores.get(nextTag) + POSUsage.get(nextTag);
						if (nextTagScore > bestTagScore) {
							bestTagScore = nextTagScore;
						}
					}
					calcTagsString += bestTag + " ";
				}
				// Write backtraced string into an output file.
				resultTagsIn.write(calcTagsString + "\n");
			}
			
			// Close testing file and results writer
			testSentences.close();
			resultTagsIn.close();
			
			/*
			 * CHECK TAGS AGAINST GIVEN.
			 */
			
			// Open test and result tags
			BufferedReader testTags = load(textFolder + textSubject + "-test-tags.txt");
			BufferedReader resultTagsOut = load(textFolder + textSubject + "-monogram-tags.txt");
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
