import java.io.*;
import java.util.*;

public class HMM {
	
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
			HashMap tagMap = new HashMap<String, Integer>();
			HashMap allStates = new HashMap<HashMap<String, String>, Double>();
			ArrayList backtraces = new ArrayList<String>();
			
			BufferedReader sentenceFile = new BufferedReader(new FileReader("ps5inputs/simple-train-sentences.txt"));

			int k = sentenceFile.read();
			String sentenceFileString = new String();
			while(k != -1) {
				char nextChar = (char) k;
				sentenceFileString += nextChar;
				k = sentenceFile.read();
			}
			String[] wordArray = sentenceFileString.split(" ");
			
			BufferedReader tagFile = new BufferedReader(new FileReader("ps5inputs/simple-train-tags.txt"));

			int l = tagFile.read();
			String tagFileString = new String();
			while(l != -1) {
				char nextChar = (char) l;
				tagFileString += nextChar;
				l = tagFile.read();
			}
			String[] tagArray = tagFileString.split(" ");

			for(String i: tagArray) {
				if(tagMap.containsKey(i)) {
					tagMap.put(i, (Integer) (int)(tagMap.get(i)) + 1);
				}
				else{
					tagMap.put(i, 0);
				}
			}
			
			sentenceFile.close();
			tagFile.close();
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
