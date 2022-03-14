
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Manan Dineshkumar Paruthi
 */

public class TwoPhaseMultiwayMergeSort {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int nosToGenerate = 0;
		while(true) {
			System.out.println("\nSelect option : ");
			System.out.println("1. Create a random list of integers");
			System.out.println("2. Display the random list (for debugging purposes only)");
			System.out.println("3. Run 2PMMS");
			System.out.println("4. Exit");
			int selectedOption = sc.nextInt();
			switch(selectedOption) {
				case 1:
					//System.out.println("Enter numbers to generate: ");
					//nosToGenerate = sc.nextInt();
					//generateNumber(nosToGenerate, Integer.MAX_VALUE);
					break;
				case 2:
					//displayInputFile();
					break;
				case 3:
					TPMMS(sc);
					break;
				case 4:
					sc.close();
					System.out.println("GoodBye !");
					System.exit(0);
			}			
		}
	}

	public static void generateNumber(int size, int range) { // case 1
		PrintWriter pw = null;
		int count = 0;
		Random r = new Random();
		try {
			pw = new PrintWriter(new FileOutputStream("data.txt"));
			while (count < size) {
				count++;
				if(count == size) {
					pw.print(r.nextInt(range));
				}
				else {
					pw.println(r.nextInt(range));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pw.close();
	}
	
	public static void displayInputFile() { // case 2
		try {
			Scanner sc = new Scanner(new FileInputStream("data.txt"));
			while (sc.hasNextLine()) {
				System.out.println(sc.nextInt());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void TPMMS(Scanner sc) { // case 3
		System.out.println("Enter Ram Size in Bytes: ");
		//
		long startTime = System.currentTimeMillis();
		long stopTime;
		long elapsedTime;
		//System.out.println("Phase 1 started at : "+startTime);
	      
		Path file = Paths.get("data.txt");
		int ramSize = sc.nextInt();

		int blockSize = ramSize / 4; // 4 bytes => 1 integer => no of integers can fit in one file
		Long noToGenerate = null;
		try {
			noToGenerate = Files.lines(file).count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int nosToGenerate = noToGenerate.intValue();
		int noOfBlocks = (int) Math.ceil((double)nosToGenerate / blockSize);
		System.out.println("Phase 1 started :");
		
		if(noOfBlocks <= 1) {
			generateSublists(1, nosToGenerate,nosToGenerate);
			System.out.println("End of Phase 1");
			stopTime = System.currentTimeMillis();
		    elapsedTime = stopTime - startTime;
		    System.out.println("Phase 1 ended after : "+elapsedTime/1000 +" seconds");
			System.out.println("Phase 2 not required as all numbers can be sorted in single block");
			
		}
		else {
			generateSublists(noOfBlocks, blockSize,nosToGenerate);
			System.out.println("End of Phase 1 => " + noOfBlocks + " files are generated.");
			stopTime = System.currentTimeMillis();
		    elapsedTime = stopTime - startTime;
		    System.out.println("Phase 1 ended after : "+elapsedTime/1000 +" seconds");
		    
		    startTime = System.currentTimeMillis();
			System.out.println("Phase 2 started : ");			
			generateSortedResultFile(noOfBlocks);
			System.out.println("End of Phase 2 =>  Sorted numbers are written into Output.txt file.");
			stopTime = System.currentTimeMillis();
		    elapsedTime = stopTime - startTime;
		    System.out.println("Phase 2 ended after : "+elapsedTime/1000 +" seconds");
		}
	}

	public static void generateSublists(int noOfFilesReq, int size,int nosToGenerate) {
		try {
			Scanner sc = new Scanner(new FileInputStream("data.txt"));
			for (int i = 1; i <= noOfFilesReq; i++) {   // 667, 1500
				int count = 0;
				int[] arr;
				if(i == noOfFilesReq && nosToGenerate % size != 0) {
					arr = new int[nosToGenerate % size];
				}
				else {
					arr = new int[size];
				}				
				PrintWriter pw;
				if(noOfFilesReq == 1) {
					pw = new PrintWriter(new FileOutputStream("Output.txt"));
				}
				else {
					pw = new PrintWriter(new FileOutputStream("level0-file" + i + ".txt"));
				}
				while (sc.hasNextInt() && count < size) {
					arr[count] = sc.nextInt();
					count++;
				}
				MergeSort.sort(arr, 0, arr.length - 1);
				for (int j = 0; j < arr.length; j++) {
					pw.println(arr[j]);
				}
				pw.close();
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateSortedResultFile(int noOfFiles) {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("Output.txt"));

			ArrayList<Long> filePointers = new ArrayList<Long>(noOfFiles);
			for (int i = 0; i < noOfFiles; i++) {
				filePointers.add((long) 0);
			}
			
			ArrayList<Integer> buffer = new ArrayList<Integer>(); // buffer stores file pointers (last accessed index)
			
			for (int i = 0; i < noOfFiles; i++) {
				RandomAccessFile file = new RandomAccessFile("level0-file" + (i+1) + ".txt", "r");
				file.seek(filePointers.get(i));
				int numberAtFilePtr = Integer.valueOf(file.readLine());
				buffer.add(numberAtFilePtr);
				filePointers.set(i, file.getFilePointer());
				file.close();
			}
			
			while(true) {
				int minValueInBuffer = Collections.min(buffer);
				int minValueIndexInBuffer = buffer.indexOf(Collections.min(buffer));
				
				if(minValueInBuffer == Integer.MAX_VALUE) {
					break;
				}
				
				pw.println(minValueInBuffer);
				
				RandomAccessFile file = new RandomAccessFile("level0-file" + (minValueIndexInBuffer+1) + ".txt", "r");
				if(filePointers.get(minValueIndexInBuffer) < file.length()) {
					file.seek(filePointers.get(minValueIndexInBuffer));
					buffer.set(minValueIndexInBuffer, Integer.valueOf(file.readLine()));
					filePointers.set(minValueIndexInBuffer, file.getFilePointer());
				}
				else {
					buffer.set(minValueIndexInBuffer, Integer.MAX_VALUE);
				}
				file.close();
			}
			
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
