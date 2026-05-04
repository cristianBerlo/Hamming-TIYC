package huffman;

import java.nio.file.Path;
import java.util.BitSet;

import javax.swing.SwingUtilities;

import hamming.HammingGui;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HuffmanGui().showGui());

    }
}