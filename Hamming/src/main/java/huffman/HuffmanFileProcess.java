package huffman;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HuffmanFileProcess{

    public static HashMap<Byte, Double> tableOfFrecuency(Path stringPath){
        Map<Byte, Double> freq = new HashMap<>();
        int b,total=0;
        try (FileInputStream fis = new FileInputStream(stringPath.toFile());

            BufferedInputStream bis = new BufferedInputStream(fis)) {
            while ((b = bis.read()) != -1) {
                total++;
                freq.put((byte) b, freq.getOrDefault((byte) b, 0.0) + 1);
            }
            int totalFinal = total;
            for (Map.Entry<Byte, Double> entry : freq.entrySet()) {
                double frecuency = entry.getValue() / totalFinal;
                entry.setValue(frecuency);
                 
            }
            
            return new HashMap<>(freq);
        } catch (IOException e) {
            return null;
        }
        
    }
}