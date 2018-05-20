import java.io.*;
import java.util.*;

public class MonogramModeler {
	// Define where input files come from. Simple is the boring file, brown is the complex file.
	static final String textFolder = "inputs/ps5/";
	static final String textSubject = "brown";
	static final String sentenceStarter = "#";
	
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
			BufferedReader trainSentences = ModelerFunctions.load(textFolder + textSubject + "-train-sentences.txt");
			BufferedReader trainTags = ModelerFunctions.load(textFolder + textSubject + "-train-tags.txt");
			
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
			// Get the best POS.
			String bestPOS = ModelerFunctions.getBestFromHashMapStringDouble(POSUsage);
			// Log the shit out of POSUsage.
			ModelerFunctions.logify(POSUsage);
			// Change the POSWords map from using rote numbers to percentage hits.
			for (String word : POSWords.keySet()) {
				ModelerFunctions.logify(POSWords.get(word));
			}
			
			/*
			 * INTERPRETING TEST FILES.
			 */
			
			System.out.println("TESTING BEGUN");
			
			// Create bufferedReader for testing sentences and bufferedWriter for putting output.
			BufferedReader testSentences = ModelerFunctions.load(textFolder + textSubject + "-test-sentences.txt");
			BufferedWriter resultTagsIn = ModelerFunctions.write(textFolder + textSubject + "-monogram-tags.txt");
			
			String calcTagsString, bestTag;
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
			
			// Check results.
			ModelerFunctions.checkTags(textFolder + textSubject + "-test-tags.txt", textFolder + textSubject + "-monogram-tags.txt");
			
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
