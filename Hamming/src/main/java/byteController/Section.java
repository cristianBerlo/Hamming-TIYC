package byteController;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Section {
	Random random ;
	int size;
    public Section(int size) {
    	random= new Random();
    	this.size = size;
    }
    
	public List<BitSet> section(String string) { // entrada size de los segmentos a generar
	List<BitSet> parts = new ArrayList<>(); // arrayList donde se van a almacenar los segmentos
	BitSet  part = BitSet.valueOf(string.getBytes()); //
	int iter= Math.ceilDiv(string.length()*8,size); // len*8 cantidad total de bits
	// techo entre la cantidad total de bits y el size de los segmentos
	for(int i=0;i<iter;i++) {
		parts.add(part.get(i*size,(i*size)+size)); 
	}
	 return parts;
 }
	
public List<BitSet> errorGeneration(float chance, List<BitSet>parts){
	for(BitSet bytes: parts) { //itero por cada cadena de bits
		if(random.nextFloat()<=chance) { // si el numero generado entra en la probabilidad 
			bytes.flip((random.nextInt(bytes.size()))); // lo invierto
		}
	}
	return parts; 
}
 
 public void printBytes(BitSet bytes) {
	 StringBuilder sb = new StringBuilder();
	   for (int i1 = 0; i1 <size; i1++) {
          sb.append(bytes.get(i1) ? '1' : '0');
      }
		System.out.print(sb.toString());
 }

}
