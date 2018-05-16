import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Sudi {
	static String textFolder = "inputs/ps5/";
	static String textSubject = "simple";
	public static void main(String args[]) throws FileNotFoundException {
		BufferedReader trainSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-train-sentences.txt"));
		BufferedReader trainTags = new BufferedReader(new FileReader(textFolder + textSubject + "-train-tags.txt"));
		BufferedReader testSentences = new BufferedReader(new FileReader(textFolder + textSubject + "-test-sentences.txt"));
		BufferedReader testTags = new BufferedReader(new FileReader(textFolder + textSubject + "-test-tags.txt"));
	}
}
