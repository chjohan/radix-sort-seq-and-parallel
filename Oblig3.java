/* Main class of parallel radix sort algorithm
 * Assignment 3
 * UiO - Christian Johansen (chjohan)
 * 
 */

public class Oblig3 {
	public static void main (String[] args) {
		int n = 0;
		if(args.length == 1) {
			n = Integer.parseInt(args[0]);
			System.out.println("Sekvensiell loesning:");
			SekvensiellRadix s = new SekvensiellRadix(n);
			System.out.println();
			System.out.println("Parallell loesning:");
			ParaRadix p = new ParaRadix(n);
		} else {
			System.out.println("You must run this program with a number as argument.");
		}
	}
}
