package knox.sudoku;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

/**
 * 
 * This is the MODEL class. This class knows all about the
 * underlying state of the Sudoku game. We can VIEW the data
 * stored in this class in a variety of ways, for example,
 * using a simple toString() method, or using a more complex
 * GUI (Graphical User Interface) such as the SudokuGUI 
 * class that is included.
 * 
 * @author jaimespacco
 * ^^^This guy left it unfinished, so I had to finish it for them ~Tlorenz
 * 
 * Audio playing code from user mini-me on Github, specific thread linked later
 * ^^^No idea if this is the proper way to do this, please enlighten me as to the correct way
 *
 */
public class Sudoku {
	int rows = 9; //don't set a custom board size, especially not one >9. I didn't finish adding that
	int cols = 9;
	int[] all = {1,2,3,4,5,6,7,8,9}; //all single-digit ints, don't remove or program goes kaboom
	int[][] board = new int[rows][cols];
	
	public int get(int row, int col) {
		if(row > rows || col > cols)
			return -1;
		return board[row][col];
	}
	
	public boolean set(int row, int col, int val) {
		// returns false if value is illegal, otherwise returns true
		if(!isLegal(row, col, val))
			return false;
		board[row][col] = val;
		return true;
	}
	
	public boolean isLegal(int row, int col, int val) {
		// TODO: check if it's legal to put val at row, col
		if(getRow(row).contains(val) && getCol(col).contains(val) && getBox(row,col).contains(val))
			return true;
		return false;
	}
	
	public List<Integer> getRow(int row) { //Returns all legal values for the row
		int[] Row = new int[rows];
		Row = board[row];
		List<Integer> legals = new ArrayList<Integer>();
		for(int i : all) { //Adds all possible values to legals
			legals.add(i);
		}
		for(int i : Row) { //Removes illegal values from legals
			legals.remove((Integer)i);
		}
		return legals;
	}
	
	public List<Integer> getCol(int col) { //Returns all legal values for the column
		int[] Col = new int[cols];
		for(int count = 0; count < rows; count++) {
			Col[count] = board[count][col];
		}
		List<Integer> legals = new ArrayList<Integer>();
		for(int i : all) { //Adds all possible values to legals
			legals.add(i);
		}
		for(int i : Col) { //Removes illegal values from legals
			legals.remove((Integer)i);
		}
		return legals;
	}
	
	public List<Integer> getBox(int row, int col) { //returns all legal values from the 3x3
		int[] Box = new int[9];
		int xOffset = row - row%3;
		int yOffset = col - col%3;
		int count = 0;
		for(int r = 0 + xOffset; r < 3 + xOffset; r++) {
			for(int c = 0 + yOffset; c < 3 + yOffset; c++) {
				Box[count] = board[r][c];
				count++;
			}
		}
		List<Integer> legals = new ArrayList<Integer>();
		for(int i : all) {
			legals.add(i);
		}
		for(int i : Box) {
			legals.remove((Integer)i);
		}
		return legals;
	}
	
	public Collection<Integer> getLegalValues(int row, int col) {
		//This method is not necessary given how I structured my code to get legal values, and would slow it down unnecessarily
		//If I'd remembered that this existed when i wrote said code, I would have used it
		//I didn't remove it so that my explanation has a place to live
		return null;
	}
	
/**

_ _ _ 3 _ 4 _ 8 9
1 _ 3 2 _ _ _ _ _
etc


0 0 0 3 0 4 0 8 9

 */
	public void load(String filename) {
		try {
			Scanner barcodeReader = new Scanner(new FileInputStream(filename));
			//I changed this for absolutely no reason other than that I felt like it
			// read the file (but only if you ask nicely and provide a valid filepath)
			for (int r=0; r<9; r++) {
				for (int c=0; c<9; c++) {
					int val = barcodeReader.nextInt();
					board[r][c] = val;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return which 3x3 grid this row is contained in.
	 * 
	 * @param row
	 * @return
	 */
	public int get3x3row(int row) { //Didn't realize this was here until later lol, wish I'd found it sooner
		return row / 3;
	}

	
	/**
	 * Convert this Sudoku board into a String
	 */
	public String toString() {
		String result = "";
		for (int r=0; r<9; r++) {
			for (int c=0; c<9; c++) {
				int val = get(r, c);
				if (val == 0) {
					result += "_ ";
				} else {
					result += val + " ";
				}
			}
			result += "\n";
		}
		return result;
	}
	
	public static void main(String[] args) {
		Sudoku sudoku = new Sudoku();
		sudoku.load("easy1.txt");
		System.out.println(sudoku);
		
		Scanner scan = new Scanner(System.in);
		while (!sudoku.gameOver()) {
			System.out.println("enter value r, c, v :");
			int r = scan.nextInt();
			int c = scan.nextInt();
			int v = scan.nextInt();
			sudoku.set(r, c, v);
			
			System.out.println(sudoku);
			break;
		}
		try {
		victory(sudoku);
		} catch (IOException e) {
			//This should happen exactly never
		}
	}

	public boolean gameOver() { //Inefficient nested for because unless this is running on a Pentium 3 it should be near-instant
		for(int r = 0; r < 9; r ++) {
			for(int c = 0; c < 9; c++) {
				if(board[r][c] == 0)
					return false;
			}
		}
		return true;
	}
	
	public static void victory(Sudoku s) throws IOException {
		/*
		 * Play victory sound effect
		 * Uses code from this github thread: https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java
		 * All code is from user mini-me (user:1525158)
		 */
		try {
			Clip clip = AudioSystem.getClip();
	        clip.open(AudioSystem.getAudioInputStream(new File("customfont.wav")));
	        clip.start();
			
			while(!clip.isRunning())
				Thread.sleep(10);
			while(clip.isRunning())
				Thread.sleep(10);
			clip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public boolean isBlank(int row, int col) {
		return board[row][col] == 0;
	}

}
