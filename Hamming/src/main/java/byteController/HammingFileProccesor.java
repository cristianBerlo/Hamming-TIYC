package byteController;
import hamming.Hamming;
import java.io.IOException;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
public class HammingFileProccesor {
	 public static Boolean processFileError(Path pathString, int moduleBits) {
	        Hamming hamming = new Hamming(moduleBits);
	        try {
	            byte[] allBytes = Files.readAllBytes(pathString);// leer todo el archivo como bytes
	            BitSet bitsFile = BitSet.valueOf(allBytes);// convertir bytes a bits
	            int totalBits = allBytes.length * 8;
	            BitSet result = new BitSet();
	            int resultIndex = 0;
	            for (int i = 0; i < totalBits; i += moduleBits) {// procesar en bloques de moduleBits
	                BitSet blok = new BitSet(moduleBits);
	                blok = bitsFile.get(i,i+moduleBits ); //Hace lo mismo pero con un nivel de iteracion menos
	                BitSet protecBloack = hamming.errorGeneration(0.8f,blok); //agrego error a cada bloque
	                for (int k = 0; k < hamming.getLenght(); k++) { 
	                    result.set(resultIndex++, protecBloack.get(k));
	                }
	            }
	            byte[] output = new byte[(resultIndex + 7) / 8];
	            for (int i = 0; i < resultIndex; i++) {
	                if (result.get(i)) {
	                    output[i / 8] |= (1 << (i % 8));
	                } //Cristian si estas leyendo esto no uses toByteArray() borra los ultimos bits si son 0s
	            }
	            Files.write(pathString, output); 
	            return true;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	 
    public static Boolean processFileProtec(Path pathString, int moduleBits) {
        Hamming hamming = new Hamming(moduleBits);
        try {
            byte[] allBytes = Files.readAllBytes(pathString);// leer todo el archivo como bytes
            String originalName = pathString.getFileName().toString();
            int dotIndex = originalName.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
            int sizeModul,blockDataZise;
            sizeModul = 0;blockDataZise = 4;
            switch (moduleBits){ //luego modififcar  clase hamming para que me de el largo del bloque de datos y no tener que hacer esto
                case 8: sizeModul = 1;blockDataZise = 4;break;
                case 1024: sizeModul = 2; blockDataZise = 1013;break;
                case 16384: sizeModul = 3;blockDataZise = 16369;break;
            }
            String newName = baseName + ".HA" + sizeModul;
            Path outputPath = pathString.getParent().resolve(newName);

            BitSet bitsFile = BitSet.valueOf(allBytes);// convertir bytes a bits
            int totalBits = allBytes.length * 8;
            BitSet result = new BitSet();
            int resultIndex = 0;
            for (int i = 0; i < totalBits; i += blockDataZise) {// procesar en bloques de moduleBits
                BitSet blok = new BitSet(blockDataZise);
                blok = bitsFile.get(i,i+blockDataZise ); //Hace lo mismo pero con un nivel de iteracion menos
                /* for (int j = 0; j < blockDataZise; j++) {// llenar bloque con bits del archivo
                    if (i + j < totalBits) {
                        blok.set(j, bitsFile.get(i + j));
                    }
                } */
                BitSet protecBloack = hamming.hamming(blok); //hamminisando cada bloque
                for (int k = 0; k < hamming.getLenght(); k++) { 
                    result.set(resultIndex++, protecBloack.get(k));
                }
            }
            byte[] output = new byte[(resultIndex + 7) / 8];
            for (int i = 0; i < resultIndex; i++) {
                if (result.get(i)) {
                    output[i / 8] |= (1 << (i % 8));
                } //Cristian si estas leyendo esto no uses toByteArray() borra los ultimos bits si son 0s
            }
            Files.write(outputPath, output); 
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    

public static Boolean unprotectFileProtect(Path pathString, int moduleBits, boolean correct) {
    Hamming hamming = new Hamming(moduleBits);

try {
    byte[] allBytes = Files.readAllBytes(pathString);
    BitSet bitsFile = BitSet.valueOf(allBytes);

    int totalBits = allBytes.length * 8;
    BitSet result = new BitSet();
    int resultIndex = 0;

    int blockSize = hamming.getLenght();

    for (int i = 0; i < totalBits; i += blockSize) {

        BitSet block = new BitSet(blockSize);
        block = bitsFile.get(i,i+blockSize ); // Un nivel menos de anidacion
        /*for (int j = 0; j < blockSize; j++) {
            if (i + j < totalBits) {
                block.set(j, bitsFile.get(i + j));
            }
        } */
        BitSet datos;
        if(correct) {
         datos = hamming.ErrorHandler(block);
         if(datos!=null) {
         datos = hamming.translate(datos);}else {
        	 return null;
         }}
        else {
            datos = hamming.translate(block);
        }
        for (int k = 0; k < hamming.getDataBits(); k++) {
            result.set(resultIndex++, datos.get(k));
        }
    }
    byte[] output = result.toByteArray();
    Files.write(Path.of("archivo_recuperado.txt"), output);
    return true;

} catch (IOException e) {
    e.printStackTrace();
    return false;
}
}
}


