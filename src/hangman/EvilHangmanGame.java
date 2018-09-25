package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class EvilHangmanGame implements IEvilHangmanGame {
	public EvilHangmanGame() {
		
	}
	public int wordSize = 0; 
	public HashSet<String> wordBank = new HashSet<String>();
	public HashSet<Character> usedLetters = new HashSet<Character>(); 

	
	
	@Override
	public void startGame(File dictionary, int wordLength) throws FileNotFoundException {
		Scanner sc = new Scanner(dictionary);
		wordSize = wordLength;
		Pattern pattern = Pattern.compile("[a-zA-Z]*");

		while (sc.hasNext()) {
			String s = sc.next();
			Matcher matcher = pattern.matcher(s);
			if(!matcher.matches()) {
				throw new StringIndexOutOfBoundsException();
			}
			if (s.length() == wordLength) {
				wordBank.add(s);
			}
		}
		if (wordBank.size() == 0) {
			sc.close();
			throw new StringIndexOutOfBoundsException();
		}
		sc.close();
	}

	@Override
	public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
		if (usedLetters.contains(guess)) {
			throw new GuessAlreadyMadeException();
		}
		else {
			usedLetters.add(guess);
		}
		return partition(guess);
	}
	
	public Set<String> partition (char guess) {
		
		//ArrayList<TreeMap<String, Set<String>>> families = new ArrayList<TreeMap<String, Set<String>>>();
		//ArrayList<String> keyList = new ArrayList<String>();
		HashMap<String, HashSet<String>> families = new HashMap<String, HashSet<String>>();
		Iterator<String> wordBankIt = wordBank.iterator();
		
		//PARTITION THEM INTO BUCKETS
		while (wordBankIt.hasNext()) {
			//System.out.println(wordBankIt.next());
			String s = wordBankIt.next();
			StringBuilder indicesStr = new StringBuilder(s);
			for (int i = 0; i < s.length(); ++i) {
				
				if (s.charAt(i) != guess) {
					indicesStr.setCharAt(i, '_');
				}
			}
			String indexStr = indicesStr.toString();
			if (families.containsKey(indexStr)) { //if the thing exists
				HashSet<String> currentSet = families.get(indexStr);
				currentSet.add(s);
				families.put(indexStr, currentSet);
			}
			else {
				HashSet<String> newSet = new HashSet<String>();
				newSet.add(s);
				families.put(indexStr, newSet);
			} //at this point I should have added the thing to a set if there was one, or made a new map pairing if there wasn't one. 
		}
		//print out what you got
		HashMap <String, HashSet<String>> bestFamily = getBestFamily(families);
		
		HashSet<String> stringSet = new HashSet<String>();
		for (Map.Entry<String, HashSet<String>> entry: bestFamily.entrySet()) {
			stringSet.addAll(entry.getValue());
		}
		wordBank = stringSet;
		return stringSet;
		 
		//System.out.println(bestFamily.keySet().toString());
		
		
	}
	
	public HashMap <String, HashSet<String>> getBestFamily(HashMap<String, HashSet<String>> families) {
		HashMap <String, HashSet<String>> bestFamily = new HashMap <String, HashSet<String>>();
		HashMap <String, HashSet<String>> entriesWithLargestQuantity = getGreatestQuantities(families); //try to get greatest quantity
		if (entriesWithLargestQuantity.size() > 1) { //if there's a tie
			HashMap <String, HashSet<String>> entriesWithFewestGuessedLetters = getFewestGuessedLetters(entriesWithLargestQuantity); //try to get fewest guessed letters
			if (entriesWithFewestGuessedLetters.size() > 1) { //if there's a tie
				HashMap <String, HashSet<String>> rightWeightedEntry = getRightWeightedEntry(entriesWithFewestGuessedLetters); //get right weighted entry
				bestFamily = rightWeightedEntry;
			}
			else {
				bestFamily = entriesWithFewestGuessedLetters;
			}
		}
		else {
			bestFamily = entriesWithLargestQuantity;
		}
		
		return bestFamily;
	}

	public HashMap<String, HashSet<String>> getGreatestQuantities(HashMap<String, HashSet<String>> families) { //WORKS FINE
		//PICK LARGEST SIZE

		HashMap<String, HashSet<String>> entriesWithLargestQuantity = new HashMap<String, HashSet<String>>();
		HashSet<String> entrySet = new HashSet<String>(); //iterate through and pick the greatest quantity
		
		
		Map.Entry<String,HashSet<String>> hasLargestQuantity = new AbstractMap.SimpleEntry<String, HashSet<String>>("", entrySet); //this might break if we have all zero quantities
		
		for (Map.Entry<String, HashSet<String>> entry : families.entrySet()) {
		    if(entry.getValue().size() > hasLargestQuantity.getValue().size()) {
		    	hasLargestQuantity = entry;

		    }
		}
		
		entriesWithLargestQuantity.put(hasLargestQuantity.getKey(), hasLargestQuantity.getValue());
		for(Entry<String, HashSet<String>> pair : families.entrySet()) { //iterate through and look for ties for greatest quantity
			if (pair.getValue().size() == hasLargestQuantity.getValue().size()) {
				entriesWithLargestQuantity.put(pair.getKey(), pair.getValue());
			}
		}
		return entriesWithLargestQuantity;
	}
	
	public HashMap<String, HashSet<String>> getFewestGuessedLetters(HashMap<String, HashSet<String>> entries) {  //I THINK I'LL GO FOR A WALK
		
		HashMap<String, HashSet<String>> entriesWithFewestGuessed = new HashMap<String, HashSet<String>>();
		
		int f = 0; //GETTING A STRING THAT IS A KEY OF ONE OF THE ENTRIES. PROBABLY A BETTER WAY TO DO THIS. 
		String s = new String();
		for(String key: entries.keySet()) {
			if (f == 0) {
				s = key;
				++f;
			}
		}		
		String fewestCharsGuessed = new String(s); //COUNTING NUMBER OF GUESSED CHARS IN COMPARESTRING
		int fewestCharsGuessedNum = 0;
		for (int i = 0; i < s.length(); ++i) {
			if (s.charAt(i) != '_') {
				++ fewestCharsGuessedNum;
			}
		}
		
		
		for(String key : entries.keySet()) { //COUNTS NUM GUESSED IN EACH KEY AND IF IT'S LESS THAN THE CURRENT BEST PICK IT MAKES THAT THE BEST PICK
			int numGuessed = 0;
			for (int i = 0; i < key.length(); ++i) {
				if (key.charAt(i) != '_') {
					++numGuessed;
				}
			}
			if (numGuessed < fewestCharsGuessedNum) {
				fewestCharsGuessed = key;
				fewestCharsGuessedNum = numGuessed;
			}
		}
		
		
		entriesWithFewestGuessed.put(fewestCharsGuessed, entries.get(fewestCharsGuessed));
		
		for(String key: entries.keySet()) { //LOOKS FOR TIES
			int numGuessed = 0;
			for (int i = 0; i < key.length(); ++i) {
				if (key.charAt(i) != '_') {
					++numGuessed;
				}
			}
			if (numGuessed == fewestCharsGuessedNum) {
				entriesWithFewestGuessed.put(key, entries.get(key));
			}
		}
		return entriesWithFewestGuessed;
	}
	
	public HashMap<String, HashSet<String>> getRightWeightedEntry(HashMap<String, HashSet<String>> entries) { //I FEEL AT EASE! 
		HashMap<String, HashSet<String>> rightmostEntry = new HashMap<String, HashSet<String>>();
		
		ArrayList<String> keyStrings = new ArrayList<String>();
		for (String key: entries.keySet() ) {
			keyStrings.add(key);
		}
		
		while (keyStrings.size() > 1) {
			for (int i = keyStrings.get(0).length() - 1; i >= 0; --i) {
				for (int j = 0; j < keyStrings.size(); ++j) {
					if (keyStrings.get(j).charAt(i) == '_') {
						boolean willRemove = false;
						for (int k = 0; k < keyStrings.size(); ++k) {
							if (keyStrings.get(k).charAt(i) != '_') {
								willRemove = true;
							}
						}
						if (willRemove) {
							keyStrings.remove(j);
						}
					}
				}
			}
		}
		//System.out.println("final: " + keyStrings);
		rightmostEntry.put(keyStrings.get(0), entries.get(keyStrings.get(0)));
		return rightmostEntry;
	}
	
}
