package hamming;
import java.util.BitSet;

public class Hamming {
	private int control; // cantidad de bits de control
	private int lenght; //largo del mensaje resultante (informacion + controles) 
	
public Hamming(int lenght) {
	this.lenght=lenght;
    control = 0;
    while (Math.pow(2, control) < (lenght)) { //Considero que length ya es +1 por el bit de pariedad
        control++;
    }
}

public BitSet translate(BitSet hamming) {
	BitSet string= new BitSet(lenght-control-1); // Cantidad de bits de informacion
	int j=0;
		for(int i=0;i< lenght;i++) {
			  if( (i & (i+1)) !=0 ) { // ignoro bits de control
				  string.set(j, hamming.get(i)); 
				  j++; // j es la posicion en el string traducido, i la posicion en el string hamminisado
			  }
	}	
	return string;
}

public BitSet ErrorHandler(BitSet hamming) {
	BitSet string;
	int syndrome= 0;
	int errors =0;
	for(int c=0;c<control;c++) { //calculo bits de control
		  boolean xor = false; 
		     int mask = 1 << c;
			 for (int h = mask; h < lenght; h++) {
				 if ((h & mask) != 0) {
		                xor ^= hamming.get(h-1);
		                //System.out.print(hamming.get(h-1) +" "+c+" "+" "+h+"  ");    
				 }
			 }
			  if (xor) {
			        syndrome |= (1 << c);
			    }
		     }
	if(syndrome!=0) {  
			hamming.flip(syndrome-1);
			errors++;} // incremento el contador de errores
	if(hamming.cardinality()%2!=0) { // si la pariedad no se respeta incremento contador de errores
		errors++; 
	}
	return errors<2 ? hamming : null; //si hay dos errores la funcion no retorna nada
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
