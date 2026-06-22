package hamming;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
public class HammingFileProccesor {
	       public static Path processFileError(Path pathString, int moduleBits, boolean one) {
    Hamming hamming = new Hamming(moduleBits);
    try {
        byte[] allBytes = Files.readAllBytes(pathString);
        BitSet bitsFile = BitSet.valueOf(allBytes); 
        int totalBits = allBytes.length * 8;
        BitSet result = new BitSet();
        int resultIndex = 0;
        String originalName = pathString.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        int sizeModul = 0;
        switch (moduleBits) { 
            case 8:     sizeModul = 1; break;
            case 1024:  sizeModul = 2; break;
            case 16384: sizeModul = 3; break;
        }
        String newName = baseName + ".HE" + sizeModul;
        Path outputPath = pathString.getParent().resolve(newName);      
        int blockSize = hamming.getLenght(); 
        
        for (int i = 0; i < totalBits; i += blockSize) {
            BitSet blok = bitsFile.get(i, i + blockSize); 
            
            BitSet protecBlock;
            if (one) {
                protecBlock = hamming.errorGeneration(0.8f, blok);
            } else {
                protecBlock = hamming.TwoerrorGeneration(0.8f, blok);
            } 
            
            for (int k = 0; k < blockSize; k++) { 
                result.set(resultIndex++, protecBlock.get(k));
            }
        }
        byte[] output = new byte[(resultIndex + 7) / 8];
        for (int i = 0; i < resultIndex; i++) {
            if (result.get(i)) {
                output[i / 8] |= (1 << (i % 8));
            } 
        }
        
        Files.write(outputPath, output); 
        return outputPath; 
        
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}
    public static Path processFileProtec(Path pathString, int moduleBits) {
    Hamming hamming = new Hamming(moduleBits);
    try {
        byte[] allBytes = Files.readAllBytes(pathString);
        String originalName = pathString.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        
        int sizeModul, blockDataZise;
        sizeModul = 0; blockDataZise = 4;
        switch (moduleBits) { 
            case 8:     sizeModul = 1; blockDataZise = 4; break;
            case 1024:  sizeModul = 2; blockDataZise = 1013; break;
            case 16384: sizeModul = 3; blockDataZise = 16369; break;
        }
        String newName = baseName + ".HA" + sizeModul;
        Path outputPath = pathString.getParent().resolve(newName);

        BitSet bitsFile = BitSet.valueOf(allBytes); // convertir bytes a bits
        int totalBits = allBytes.length * 8;
        BitSet result = new BitSet();
        int resultIndex = 0;
        
        for (int i = 0; i < totalBits; i += blockDataZise) {
            BitSet blok = bitsFile.get(i, i + blockDataZise); 
            
            BitSet protecBloack = hamming.hamming(blok); // hamminisando cada bloque
            for (int k = 0; k < hamming.getLenght(); k++) { 
                result.set(resultIndex++, protecBloack.get(k));
            }
        }
        
        byte[] output = new byte[(resultIndex + 7) / 8];
        for (int i = 0; i < resultIndex; i++) {
            if (result.get(i)) {
                output[i / 8] |= (1 << (i % 8));
            } 
        }
        
        Files.write(outputPath, output); 
        return outputPath; 
        
    } catch (IOException e) {
        e.printStackTrace();
        return null; // Si falla, devolvemos null
    }
}
    

public static Path unprotectFileProtect(Path pathString, int moduleBits, boolean correct) {
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
        block = bitsFile.get(i,i+blockSize ); // 
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
        else { //nani esta medio meh el boolean correct,pero bue
            datos = hamming.translate(block);
        }
        for (int k = 0; k < hamming.getDataBits(); k++) {
            result.set(resultIndex++, datos.get(k));
        }
    }
    byte[] output = result.toByteArray();
    String originalName = pathString.toString();
    int dotIndex = originalName.lastIndexOf('.');
    String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
    int sizeModul=0;
    switch (moduleBits){ //luego modififcar  clase hamming para que me de el largo del bloque de datos y no tener que hacer esto
        case 8: sizeModul = 1;break;
        case 1024: sizeModul = 2; break;
        case 16384: sizeModul = 3; break;
    }
    String preExten = correct ? ".DC" : ".DE";
    String newName = baseName + preExten + sizeModul;
    Files.write(Path.of(newName), output);
    return Path.of(newName);
} catch (IOException e) {
    e.printStackTrace();
    return null;
}
}
public static Path processFileProtecWithDate(Path pathString, int moduleBits, long unlockDateMs) {
        Hamming hamming = new Hamming(moduleBits);
        try {
            byte[] allBytes = Files.readAllBytes(pathString);
            String originalName = pathString.getFileName().toString();
            int dotIndex = originalName.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
            
            int sizeModul, blockDataZise;
            sizeModul = 0; blockDataZise = 4;
            switch (moduleBits) { 
                case 8:     sizeModul = 1; blockDataZise = 4; break;
                case 1024:  sizeModul = 2; blockDataZise = 1013; break;
                case 16384: sizeModul = 3; blockDataZise = 16369; break;
            }
            String newName = baseName + ".HAT" + sizeModul;
            Path outputPath = pathString.getParent().resolve(newName);

            BitSet bitsFile = BitSet.valueOf(allBytes); 
            int totalBits = allBytes.length * 8;
            BitSet result = new BitSet();
            int resultIndex = 0;
            
            for (int i = 0; i < totalBits; i += blockDataZise) {
                BitSet blok = bitsFile.get(i, i + blockDataZise); 
                BitSet protecBloack = hamming.hamming(blok); 
                for (int k = 0; k < hamming.getLenght(); k++) { 
                    result.set(resultIndex++, protecBloack.get(k));
                }
            }
            
            byte[] output = new byte[(resultIndex + 7) / 8];
            for (int i = 0; i < resultIndex; i++) {
                if (result.get(i)) {
                    output[i / 8] |= (1 << (i % 8));
                } 
            }
            
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(8 + output.length);
            buffer.putLong(unlockDateMs);
            buffer.put(output);        

            Files.write(outputPath, buffer.array()); 
            return outputPath; 
            
        } catch (IOException e) {
            e.printStackTrace();
            return null; 
        }
    }
    public static Path unprotectFileProtectWithDate(Path pathString, int moduleBits, boolean correct) {
        Hamming hamming = new Hamming(moduleBits);
        try {
            byte[] fileBytes = Files.readAllBytes(pathString);
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(fileBytes);
            long unlockDateMs = buffer.getLong(); // Lee los primeros 8 bytes
            
            if (System.currentTimeMillis() < unlockDateMs) {
                long segundosFaltantes = (unlockDateMs - System.currentTimeMillis()) / 1000;
                System.err.println("ARCHIVO BLOQUEADO. Faltan: " + segundosFaltantes + " seg.");
                return null; // Rebota el proceso
            }
            byte[] allBytes = new byte[fileBytes.length - 8];
            buffer.get(allBytes);
            BitSet bitsFile = BitSet.valueOf(allBytes);
            int totalBits = allBytes.length * 8;
            BitSet result = new BitSet();
            int resultIndex = 0;
            int blockSize = hamming.getLenght();

            for (int i = 0; i < totalBits; i += blockSize) {
                BitSet block = bitsFile.get(i, i + blockSize); 
                BitSet datos;
                if(correct) {
                    datos = hamming.ErrorHandler(block);
                    if(datos != null) {
                        datos = hamming.translate(datos);
                    } else {
                        return null;
                    }
                } else { 
                    datos = hamming.translate(block);
                }
                for (int k = 0; k < hamming.getDataBits(); k++) {
                    result.set(resultIndex++, datos.get(k));
                }
            }
            
            byte[] output = result.toByteArray();
            String originalName = pathString.getFileName().toString();
            int dotIndex = originalName.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
            int sizeModul=0;
            switch (moduleBits){ 
                case 8: sizeModul = 1; break;
                case 1024: sizeModul = 2; break;
                case 16384: sizeModul = 3; break;
            }
            String preExten = correct ? ".DC" : ".DE";
            String newName = baseName + preExten + sizeModul;
            Path outputPath = pathString.getParent().resolve(newName);
            
            Files.write(outputPath, output);
            return outputPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Path processFileErrorWithDate(Path pathString, int moduleBits, boolean one) {
        Hamming hamming = new Hamming(moduleBits);
        try {
            byte[] fileBytes = Files.readAllBytes(pathString);
            
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(fileBytes);
            long unlockDateMs = buffer.getLong();
            
            byte[] allBytes = new byte[fileBytes.length - 8];
            buffer.get(allBytes);
            // ----------------------------------------
            
            BitSet bitsFile = BitSet.valueOf(allBytes); 
            int totalBits = allBytes.length * 8;
            BitSet result = new BitSet();
            int resultIndex = 0;
            String originalName = pathString.getFileName().toString();
            int dotIndex = originalName.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
            int sizeModul = 0;
            switch (moduleBits) { 
                case 8:     sizeModul = 1; break;
                case 1024:  sizeModul = 2; break;
                case 16384: sizeModul = 3; break;
            }
            String newName = baseName + ".HET" + sizeModul;
            Path outputPath = pathString.getParent().resolve(newName);      
            int blockSize = hamming.getLenght(); 
            
            for (int i = 0; i < totalBits; i += blockSize) {
                BitSet blok = bitsFile.get(i, i + blockSize); 
                BitSet protecBlock;
                if (one) {
                    protecBlock = hamming.errorGeneration(0.8f, blok);
                } else {
                    protecBlock = hamming.TwoerrorGeneration(0.8f, blok);
                } 
                for (int k = 0; k < blockSize; k++) { 
                    result.set(resultIndex++, protecBlock.get(k));
                }
            }
            
            byte[] output = new byte[(resultIndex + 7) / 8];
            for (int i = 0; i < resultIndex; i++) {
                if (result.get(i)) {
                    output[i / 8] |= (1 << (i % 8));
                } 
            }
            
            java.nio.ByteBuffer finalBuffer = java.nio.ByteBuffer.allocate(8 + output.length);
            finalBuffer.putLong(unlockDateMs);
            finalBuffer.put(output);

            Files.write(outputPath, finalBuffer.array()); 
            return outputPath; 
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}


