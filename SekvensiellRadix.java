import java.util.Random;

class SekvensiellRadix {
	int[] a;
	int n;
	long startTime, endTime, elapsedTime;
	double millisecs;
	
	SekvensiellRadix(int n) {
		this.n = n;
		a = new int[n];
		fillRandom();
		startTime = System.nanoTime();
		radix2(a);	// Starting algorithm.
		endTime = System.nanoTime();
	    elapsedTime = endTime - startTime;
	    millisecs = (double)elapsedTime / 1000000.0;
	    System.out.println("\nRadix sort av " + n + " tall tok: " + millisecs + " millisek i sekvensiell løsning.");
	    
		System.out.println(isSorted(a));
	}
	
	void fillRandom() {
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
	
	void printArray() {
		System.out.println("Printing array...");
		for(int i = 0; i < a.length; i++) {
			System.out.println(a[i]);
		}
	}
	
	static void radix2(int [] a) {
		  // 2 digit radixSort: a[]
		  int max = a[0], numBit = 2, n =a.length;
		 // a) finn max verdi i a[]
		  for (int i = 1 ; i < n ; i++)
			   if (a[i] > max) max = a[i];
		  while (max >= (1<<numBit) )numBit++; // antall siffer i max

		  // bestem antall bit i siffer1 og siffer2
			int bit1 = numBit/2,
				  bit2 = numBit-bit1;
		  int[] b = new int [n];
		  radixSort( a,b, bit1, 0);    // første siffer fra a[] til b[]
		  radixSort( b,a, bit2, bit1);// andre siffer, tilbake fra b[] til a[]
	 } // end


	/** Sort a[] on one digit ; number of bits = maskLen, shiftet up shift bits */
	static void radixSort ( int [] a, int [] b, int maskLen, int shift){
		  int  acumVal = 0, j, n = a.length;
		  int mask = (1<<maskLen) -1;
		  int [] count = new int [mask+1];

		 // b) count=the frequency of each radix value in a
		  for (int i = 0; i < n; i++) {
			 count[(a[i]>> shift) & mask]++;
		  }

		 // c) Add up in 'count' - accumulated values
		  for (int i = 0; i <= mask; i++) {		  
			   j = count[i];
				count[i] = acumVal;				
				acumVal += j;

		   }
		  
		 // d) move numbers in sorted order a to b
		  for (int i = 0; i < n; i++) {
			 b[count[(a[i]>>shift) & mask]++] = a[i];
		  }

	}// end radixSort
}// end SekvensiellRadix
