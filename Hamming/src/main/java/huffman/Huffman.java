package huffman;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Huffman {
    private HashMap<Byte, Double> frecuencyTable;
    private HashMap<Byte, String> huffmanCodes = new HashMap<>();

    public Huffman(HashMap<Byte, Double> frecuencyTable) {
        this.frecuencyTable = frecuencyTable;
    }
    public Node buildTree() {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        frecuencyTable.forEach((byteKey, freq) -> {
            queue.add(new Node(byteKey, freq));
        });
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node newNode = new Node(left, right);
            queue.add(newNode); 
        }
        Node root = queue.poll();
        return root;
    }
    
    public void preOrder(Node root, String code) {
        
        if (root == null) {
            return;
        }
        if (root.getLeft() == null && root.getRight() == null) {
            huffmanCodes.put(root.getValue(), code);
        }
        preOrder(root.getLeft(), code + "0");
        preOrder(root.getRight(), code + "1");
    }
    public HashMap<Byte, String> getHuffmanCodes() {
        return huffmanCodes;
    }
}
