import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicIntegerArray;


/**
 * @author Christian
 * TODO: 1. Ta tiden.
 */

class ParaRadix {
	int n;
	int numberOfCores;
	int[] a;
	int[] b;
	int globalMax = 0;
	int numBit = 2;
	int[][] allCount;
	int[] sumCount;
	int[][] countRange;
	int[] partialSum;
	Thread[] t;
	CyclicBarrier cbMain;
	CyclicBarrier cb2;
	CyclicBarrier cb3;
	CyclicBarrier cb4;
	CyclicBarrier cb6;
	CyclicBarrier cb9;
	int bit1, bit2, mask, mask2;
	long startTime, endTime, elapsedTime;
	double millisecs;
	
	
	ParaRadix(final int n) {
		this.n = n;
		numberOfCores = (Runtime.getRuntime().availableProcessors());
		a = new int[n];
		b = new int[n];
		t = new Thread[numberOfCores];
		partialSum = new int[numberOfCores];
		countRange = new int[numberOfCores][2];
		allCount = new int[numberOfCores][];
		fillArrayWithRand(); // Fills a[] with random numbers up to n.

		
		cbMain = new CyclicBarrier(numberOfCores, new Runnable(){
			  @Override
			  public void run() {
				  
				  // CHANGE: Only one thread is now finding numBit
				  // Finding numBit
				  while (globalMax >= (1<<numBit) )numBit++; // antall siffer i max

				  bit1 = numBit/2;
				  bit2 = numBit-bit1;

				  mask = (1<<bit1) -1;
				  mask2 = (1<<bit2) -1;
				  sumCount = new int[mask+1];
			  }
		  });
		
		cb2 = new CyclicBarrier(numberOfCores, new Runnable(){
			  @Override
			  public void run() {
				  // Calculating start and end -range for each thread in sumCount array.
				  
				  // Implemented your suggested simplified code. Thanks! (Still with exclusive interval.)
				  int countPerThread = (mask+1)/numberOfCores;
				  int countRest = (mask+1)%numberOfCores;
				  int startCountIndex = 0;
				  int endCountIndex = countPerThread;
				  
				  for(int i = 0; i < numberOfCores; i++) {
					    if (countRest > 0) {
					        endCountIndex++;
					        countRest--;
					    }

					    countRange[i][0] = startCountIndex;
					    countRange[i][1] = endCountIndex-1;
					    startCountIndex = endCountIndex;
					    endCountIndex += countPerThread;
					}
				  
			  }
		  });
		
		cb3= new CyclicBarrier(numberOfCores);
		
		cb4= new CyclicBarrier(numberOfCores, new Runnable(){
			  @Override
			  public void run() {				  
				  // Initializing new sumcount for step2.
				  sumCount = new int[mask2+1];				  
			  }
		  });
		
		// This is the final barrier. It checks if array is sorted.
		cb6= new CyclicBarrier(numberOfCores, new Runnable(){
			  @Override
			  public void run() {
				  endTime = System.nanoTime();
				  elapsedTime = endTime - startTime;
				  millisecs = (double)elapsedTime / 1000000.0;
				  System.out.println("\nRadix sort av " + n + " tall tok: " + millisecs + " millisek i parallell løsning.");
				  System.out.println(isSorted(a));
			  }
		  });
		
		cb9 = new CyclicBarrier(numberOfCores, new Runnable(){
			  @Override
			  public void run() {
				  // Calculating start and end -range for each thread in sumCount array.
				  
				  // Implemented your suggested simplified code. Thanks! (Still with exclusive interval.)
				  int countPerThread = (mask2+1)/numberOfCores;
				  int countRest = (mask2+1)%numberOfCores;
				  int startCountIndex = 0;
				  int endCountIndex = countPerThread;
				  
				  for(int i = 0; i < numberOfCores; i++) {
					    if (countRest > 0) {
					        endCountIndex++;
					        countRest--;
					    }

					    countRange[i][0] = startCountIndex;
					    countRange[i][1] = endCountIndex-1;
					    startCountIndex = endCountIndex;
					    endCountIndex += countPerThread;
					}
				  
			  }
		  });
		
		startTime = System.nanoTime();
		findMax();
		
	}
	
	// Fills array with random numbers.
	void fillArrayWithRand() {
		//System.out.println("Generating random numbers");
		Random r = new Random(45123);
    	for(int i = 0; i < a.length; i++) {
    		a[i] = r.nextInt(n-1);
    	}
	}
	
	// Checks if an array is sorted from smallest to biggest.
	boolean isSorted(int[] arr) {
		System.out.println("Is array successfully sorted?");
		for(int i = 0; i < arr.length-1; i++) {
			if(arr[i] > arr[i+1]) {
				System.out.println("Error: Array was not successfully sorted. Error triggered at index " + i + ".");
				return false;
			}
		}
		return true;
	}
	
	void findMax() {
		
		int numPerThread = n/numberOfCores;
		int numRest = n%numberOfCores;
		int startIndex = 0;
		int endIndex = numPerThread;
		
		// Calculates start and end -index for each thread.
		for(int i = 0; i < numberOfCores; i++) {
			if(numRest > 0) {
				endIndex++;	
				numRest--;
			}
			(t[i] = new Thread(new RadixThread(startIndex, (endIndex-1), a, cbMain, i, cb2))).start();	// Starting thread.
			startIndex = endIndex;
			endIndex += numPerThread;
		}
		
		
	}
	
	synchronized void updateGlobalMaxValue(int i) {
		if(i > globalMax) {
			globalMax = i;
		}
	}
	
	class RadixThread implements Runnable {
		int maxValue = 0;
		int startIndex;		// Start range to find max value in array.
		int endIndex;		// End range to find max value in array.
		int countStartIndex;
		int countEndIndex;
		int[] count;
		int threadNumber;
		CyclicBarrier cb;
		CyclicBarrier cb2;
		
		RadixThread(int startIndex, int endIndex, int[] a, CyclicBarrier cb, int threadNumber, CyclicBarrier cb2) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.cb = cb;
			this.threadNumber = threadNumber;
			this.cb2 = cb2;
		}
		
		public void run() {		
			findLocalMax();
			updateGlobalMaxValue(maxValue);
			
			try {
		           cb.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			count = new int[mask+1];
			countFrequency(mask, 0, a);
			
			try {
		           cb2.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			// Assigning indexes from countRange array.
			countStartIndex = countRange[threadNumber][0];
			countEndIndex = countRange[threadNumber][1];
			summize();
			
			try {
		           cb3.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			acumValStepOne((mask+1));
			
			try {
		           cb3.await(); // cb8
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			acumValStepTwo();
			
			try {
		           cb3.await(); // cb8
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			moveNumbers(a, b, mask, 0);
			
			try {
		           cb4.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			// Ferdig med første del.
			count = new int[mask2+1];
			countFrequency(mask2, bit1, b);
			
			try {
		           cb9.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			countStartIndex = countRange[threadNumber][0];
			countEndIndex = countRange[threadNumber][1];
			summize();
			
			try {
		           cb3.await(); // cb5
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
	         
	        acumValStepOne((mask2+1));
	        
	        try {
		           cb3.await(); // cb5
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
	         
	        acumValStepTwo();
	         
 	        try {
		           cb3.await(); // cb 7
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
			moveNumbers(b, a, mask2, bit1);
			
			try {
	           cb6.await();
	         } catch (InterruptedException ex) {
	           return;
	         } catch (BrokenBarrierException ex) {
	           return;
	         }
			
		}	// END Constructor for ParaFindMax
		
		
		void findLocalMax() {
			for(int i = startIndex; i < endIndex+1; i++) {
				if(a[i] > maxValue) maxValue = a[i];
			}
		}
		
		void countFrequency(int mk, int shift, int[] arr) {
			  for (int i = startIndex; i < endIndex+1; i++) {
					 count[(arr[i]>> shift) & mk]++;
			  }
			  allCount[threadNumber] = count;		// Henger count i den dobble arryen allCount.
		}
		
		void summize() {
			for(int i = 0; i < numberOfCores; i++) {
				for(int k = countStartIndex; k < countEndIndex+1; k++) {
					sumCount[k] = sumCount[k] + allCount[i][k];
				}
			}
		}
		
		// Adds up values in threads' partition. Saves the sum in partitialSum[].
		void acumValStepOne(int arrSize) {
			
			count = new int[arrSize];
			int counter = 0;
			int acumVal = 0;
			int j;
						
			if(threadNumber==0) {
				for(int i = countStartIndex; i < countEndIndex+1; i++) {
					j = sumCount[i];
					count[i] = acumVal;
					acumVal += j;
				}
				count[countEndIndex+1] = acumVal;
				partialSum[threadNumber] = acumVal;
			} else {
				acumVal = sumCount[countStartIndex];
				for(int k = countStartIndex; k < countEndIndex; k++) {
					count[counter] = acumVal;
					acumVal += sumCount[k+1];
					counter++;
				}
				count[counter] = acumVal;
				partialSum[threadNumber] = count[counter];
			}
			
		}
		
		// Adds conditional sums from partitialSum[] to threads counts and adds to global sumCount array.
		void acumValStepTwo() {
			int currVal;
			int counter = 0;
			int countStart = countStartIndex;
			int countEnd = countEndIndex;
			
			if(threadNumber!=0) {
				countStart++;
				countEnd++;
			} else {
				countEnd++;
			}
			
			for(int i = countStart; i <= countEnd; i++) {
				currVal = count[counter];
				for(int k = 0; k < threadNumber; k++) {
					currVal += partialSum[k];
				}
				sumCount[i] = currVal;

				counter++;
				if(i+1 == sumCount.length) return;
			}
		}
		
		
		void findNumBit() {
			while (maxValue >= (1<<numBit) )numBit++; // antall siffer i max
		}
		
		// Move numbers in sorted order arr1 to arr2.
		void moveNumbers(int[] arr1, int[] arr2, int mk, int shift) {
			for (int i = 0; i < arr1.length; i++) {
				if(isOneOfMine(((arr1[i]>>shift) & mk))) {
					arr2[sumCount[(arr1[i]>>shift) & mk]++] = arr1[i];
				}
			}

		}
		
		// Checks if given "pos" is current thread's property.
		boolean isOneOfMine(int pos) {
			if(pos >= countStartIndex && pos <= countEndIndex) {
				return true;
			}
			return false;
		}
		
		
	}
	
}// end SekvensiellRadix
