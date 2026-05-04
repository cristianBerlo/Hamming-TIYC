package huffman;

public class Node implements Comparable<Node>{
    private byte value;
    private float frecuency;
    private Node left;
    private Node right;
    public Node(byte value, float frecuency) {
        this.value = value;
        this.frecuency = frecuency;
        this.left = null;
        this.right = null;
    }
    public Node(Node left, Node right) {
        this.value = 0;
        this.frecuency = left.frecuency + right.frecuency;
        this.left = left;
        this.right = right;
    }
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.frecuency, other.frecuency);
    }
    public Node getLeft() {
        return left;
    }
    public Node getRight() {
        return right;
    }  
    public byte getValue() {
        return value;
    }       
}