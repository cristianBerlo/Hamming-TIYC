package hamming;
import java.util.BitSet;

public class Hamming {
	private int control; // cantidad de bits de control
	private int lenght; //largo del mensaje resultante (informacion + controles) 
	
public Hamming(int lenght) {
	this.lenght=lenght;
    control = 0;
    while (Math.pow(2, control) < (lenght + 1)) {
        control++;
    }
}

public BitSet hammingTrans(BitSet hamming) {
	BitSet string;
	BitSet sindrome= new BitSet(control);
	for(int c=0;c<control;c++) { //calculo bits de control
		  boolean xor = false; 
		     int mask = 1 << c;
			 for (int h = mask; h < lenght; h++) {
				 if ((h & mask) != 0) {
		                xor ^= hamming.get(h-1);
		                //System.out.print(hamming.get(h-1) +" "+c+" "+" "+h+"  ");
		         
				 }
			 }
			 sindrome.set(c, xor);
		    }
	return sindrome;
}
public BitSet parity(BitSet string) {
	int xor= string.cardinality()%2;
	string.set(lenght, xor);
	return string;
}

public BitSet hamming(BitSet string) {
	BitSet hamming = new BitSet(lenght);
	int j = 0;
	for(int i=0;i< lenght;i++) {
	  if( (i & (i+1)) !=0 ) { // seteo bits no de control
		  hamming.set(i, string.get(j));
		  j++;
	  }}
	  for(int c=0;c<control;c++) { //calculo bits de control
	  boolean xor = false; 
	     int mask = 1 << c;
		 for (int h = mask; h < lenght; h++) {
			 if ((h & mask) != 0) {
	                xor ^= hamming.get(h-1);  
	          }
		 }
		 hamming.set(mask-1, xor);
	    }
		int xor= string.cardinality()%2; //cardinality devuelve la cantidad de bits seteados a 1, el resto puede ser 0 si es par o 1 si es impar
		string.set(lenght, xor); // agrego el bit de pariedad en la ultima posicion
	return hamming; 
}


}
