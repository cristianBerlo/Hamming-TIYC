package huffman;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
public class HuffmanFileProcess{

    public static float[] tableOfFrecuency(Path stringPath){
        float[] freq = new float[256];
        int b,total=0;
        try (FileInputStream fis = new FileInputStream(stringPath.toFile());
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            while ((b = bis.read()) != -1) {
                total++;
                freq[b]++;
            }
            int totalFinal = total;
            if (total > 0) {
                for (int i = 0; i < freq.length; i++) {
                    if (freq[i] > 0) {
                        freq[i] /= total;
                    }else {
                    	freq[i]=(float) 0.0;
                    }
                }
            }
            bis.close();
            return freq;
        } catch (IOException e) {
            return null;
        }
        
    }
    public static String processFileHuf(Path stringPath) {
    	String originalName = stringPath.toString();
         int dotIndex = originalName.lastIndexOf('.');
         String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
         String newName = baseName + ".DHUF";
    	try (FileInputStream fis = new FileInputStream(stringPath.toFile());
    		       BufferedInputStream bis = new BufferedInputStream(fis);
    			DataInputStream dis = new DataInputStream(bis);
    			DataOutputStream dos = new DataOutputStream(
    					new BufferedOutputStream(Files.newOutputStream(Path.of(newName))));) {
    		    	int bytesR = dis.readInt();
    		    	int b;
    		    	float[] frequencies = new float[256];
    		    	for (int i=0 ; i<bytesR ; i++) {
    		    		 b = dis.read();
    		    		float freq = dis.readFloat();
    		    		frequencies[b]=freq;
    		    	}
    		    	int bytes = dis.readInt();
    		        Huffman huffman = new Huffman(frequencies);
    		        Node root = huffman.buildTree();
    		        HashMap codes = huffman.getHuffmanCodes();
    		        huffman.preOrder(root, "");
    		        Node current = root;
            		for(int i=0; i<bytes ;) {
            			b = dis.read();
            			String bits = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    		        	for (char bit : bits.toCharArray()) {
    		                if (bit == '0') {
    		                    current = current.getLeft();
    		                } else {
    		                    current = current.getRight();
    		                }
    		                if (current.getLeft() == null && current.getRight() == null) {
    		                    dos.write(current.getValue());
    		                    i++;
    		                    current = root; 
    		                }
    		            }
    		        }
            		return newName;
    		        }
    		       catch (IOException e) {
    		    	   return "";
    		                      }
    	
    }
    
    public static boolean processFile(Path stringPath) {
    	float[] freq = HuffmanFileProcess.tableOfFrecuency(stringPath);
        String originalName = stringPath.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        String newName = baseName + ".HUF";
        Path outputPath = stringPath.getParent().resolve(newName);
        Huffman huffman = new Huffman(freq);
        Node root = huffman.buildTree();
        HashMap codes = huffman.getHuffmanCodes();
        huffman.preOrder(root, "");
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(outputPath)))) {
        	int uniqueBytes = 0;
            for (float f : freq) if (f > 0) uniqueBytes++;
            dos.writeInt(uniqueBytes); 
            System.out.print(uniqueBytes);
           for (int i = 0; i < freq.length; i++) {
                if (freq[i] > 0) {
                    dos.writeByte((byte) i);
                    dos.writeFloat(freq[i]);
                }
            }
       try (FileInputStream fis = new FileInputStream(stringPath.toFile());
       BufferedInputStream bis = new BufferedInputStream(fis)) {
    	   int b=bis.available();
    	   dos.writeInt(b);
    	   StringBuilder builder = new StringBuilder();
       while ((b = bis.read()) != -1) {
            	builder.append(codes.get((byte) b));
            	while (builder.length() >= 8) {
                    String byteString = builder.substring(0, 8);
                    int byteToWrite = Integer.parseInt(byteString, 2);
                    dos.write(byteToWrite);
                    builder.delete(0, 8);
                }
       }
       if (builder.length() > 0) {
           while (builder.length() < 8) {
               builder.append("0"); 
           }
           dos.write(Integer.parseInt(builder.toString(), 2));
       }
       return true;
                               } catch (IOException e) {
                            	   return false;
                               }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }
}