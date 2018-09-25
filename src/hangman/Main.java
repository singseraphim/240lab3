
package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import hangman.IEvilHangmanGame.GuessAlreadyMadeException;

public class Main {

	public static void main(String[] args) {
		EvilHangmanGame myGame = new EvilHangmanGame();
		File inFile = null;
		int wordLength = 0;
		int numGuesses = 0;
		try {
			inFile = new File(args[0]);
			wordLength = Integer.parseInt(args[1]);
			numGuesses = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("USAGE: java Main dictionary wordLength guesses");
			return;
		}
		Scanner input = new Scanner(System.in);

		try {
			myGame.startGame(inFile, wordLength);
		} catch (FileNotFoundException e1) {
			System.err.println("Dictionary file not found. Please enter valid file name.");
			input.close();
			return;
		}
		catch (StringIndexOutOfBoundsException e2){
			System.err.println("Something's wrong with your dictionary file. It may have numbers/special characters, or it may not have any words of the given length.");
			input.close();
			return;
		}

		System.out.println("Hail and well met my dudes!");
		ArrayList<Character> previousGuesses = new ArrayList<Character>();
		char[] word = new char[wordLength];
		Arrays.fill(word, '_');

		// A GIVEN TURN
		boolean gameOver = false;
		while (!gameOver) {
			System.out.println("You have " + numGuesses + " guesses left");
			--numGuesses;
			
			
			char[] guessArray = new char[previousGuesses.size()];
			Arrays.sort(guessArray);
			StringBuilder guessList = new StringBuilder();
			for (int i = 0; i < guessArray.length; ++i) {
				guessArray[i] = previousGuesses.get(i);
			}
			
			Arrays.sort(guessArray);
			
			for (int i = 0; i < guessArray.length; ++i) {
				guessList.append(guessArray[i]);
				guessList.append(" ");
			}
			
			System.out.println("Used characters: " + guessList);
			StringBuilder wordStr = new StringBuilder();

			for (int i = 0; i < word.length; ++i) {
				wordStr.append(word[i]);
				wordStr.append(" ");
			}

			System.out.println("Word: " + wordStr);
			System.out.println("Enter guess: ");

			boolean badInput = false;
			
			String guessStr = input.next();
			if (guessStr.length() > 1) {
				badInput = true;
				System.out.println("Invalid input. Enter a single letter.\n");
				++numGuesses;
			}
			char guess = guessStr.charAt(0);
			guess = Character.toLowerCase(guess);

			if (!Character.isLetter(guess)) {
				badInput = true;
				System.out.println("Invalid input. Enter a single letter.\n");
				++numGuesses;
			}

			Set<String> partitionResult = new HashSet<String>();
			if (!badInput) {
				try {
					partitionResult = myGame.makeGuess(guess);
					//System.out.println(partitionResult);
				} catch (GuessAlreadyMadeException e) {
					System.out.println("You already guessed that letter my guy!");
					badInput = true;
					++numGuesses;
				}
			}

			if (!badInput) {
				previousGuesses.add(guess);
				String keyString = getKeyString(partitionResult, guess);
				//System.out.println("keystring is " + keyString);
				
				boolean newLetterGuessed = false;
				int newCharsGuessed = 0;
				for (int i = 0; i < word.length; ++i) {
					if (keyString.charAt(i) != '_') { // OPERATING UNDER ASSUMPTION THAT I WILL NEVER HAVE A NEW
														// CHARACTER WHERE THERE ALREADY IS ONE IN THE WORD
						newLetterGuessed = true;
						word[i] = keyString.charAt(i);
						++newCharsGuessed;
					}
				}

				if (newLetterGuessed) {
					boolean lastLetterGuessed = true;
					for (int i = 0; i < word.length; ++i) {
						if (word[i] == '_') {
							lastLetterGuessed = false;
						}
					}
					if (lastLetterGuessed) {
						System.out.println("You solved my hangman mystery!");
						System.out.println("Final word was: " + partitionResult.iterator().next());
						gameOver = true;

					} else {
						++numGuesses;
						if (newCharsGuessed > 1) {
							System.out.println("Yes, there are " + newCharsGuessed + " " + guess + "'s");
						} else {
							System.out.println("Yes, there is " + newCharsGuessed + " " + guess);
						}
					}
				} else {
					if (numGuesses > 0) {
						System.out.println("There are no " + guess + "'s to be found");
					} else {
						System.out.println("You lost my hangman puzzle!");
						System.out.println("The word was: " + partitionResult.iterator().next());
						gameOver = true;
					}
				}

				System.out.println("\n");
			}
		}
		input.close();
	}
	
	public static String getKeyString(Set<String> wordBank, char guess) {
		StringBuilder keyStr = new StringBuilder();
		String wordBankElement = wordBank.iterator().next();
		for (int i = 0; i < wordBankElement.length(); ++i) {
			if (wordBankElement.charAt(i) == guess) {
				keyStr.append(guess);
			}
			else {
				keyStr.append('_');
			}
		}
		return keyStr.toString();
	}

}
