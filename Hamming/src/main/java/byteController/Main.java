package byteController;

import java.util.BitSet;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HuffmanGui().showGui());
    }
}