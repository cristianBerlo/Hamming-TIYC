package hamming;

import java.util.BitSet;

public class Main {
	  public static void main(String[] args) {
			Hamming hamming = new Hamming(7,3);
			BitSet set= new BitSet(4);
			set.clear();
			set.set(0);
			BitSet ham = hamming.hamming(set);

	        StringBuilder sb = new StringBuilder();
	        for (int i = 7 - 1; i >= 0; i--) {
	            sb.append(ham.get(i) ? '1' : '0');
	        }
	        StringBuilder ab = new StringBuilder();
	        for (int i = 4-1; i >= 0; i--) {
	            ab.append(set.get(i) ? '1' : '0');
	        }
			System.out.print(sb.toString());
			System.out.print("       "+ab.toString());

			
			}
}
