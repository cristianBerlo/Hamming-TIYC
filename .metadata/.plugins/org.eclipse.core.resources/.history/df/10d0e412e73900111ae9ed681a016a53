package byteController;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Section {
 public List<BitSet> section(String string,int size) {
	List<BitSet> parts = new ArrayList<>();
	BitSet  part = BitSet.valueOf(string.getBytes());
	int iter= string.length()/size;
	for(int i=0;i<iter;i++) {
		parts.add(part.get(i*size,(i*size)+size));
	}
	 return parts;
 }
}
