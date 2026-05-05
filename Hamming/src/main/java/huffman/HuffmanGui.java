package huffman;
import javax.swing.*;

import hamming.HammingFileProccesor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HuffmanGui extends JFrame {
    private Path selectedFile;
    private final JLabel selectedFileLabel;
    private final JTextArea originalArea;
    private final JTextArea recoveredArea;
    private final JLabel statusLabel;

    public HuffmanGui() {
        super("Huffman File Protector");
        selectedFileLabel = new JLabel("Archivo: ninguno");
        JButton selectButton = new JButton("Cargar archivo");
        JButton protectButton = new JButton("Comprimir archivo");
        JButton decodeButton = new JButton("Descomprimir archivo");
        selectButton.addActionListener(this::selectFile);
        protectButton.addActionListener(this::protectFile);
        decodeButton.addActionListener(this::decodeFile);
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(selectedFileLabel, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.add(selectButton);
        buttons.add(protectButton);
        buttons.add(decodeButton);
        topPanel.add(buttons, gbc);
        originalArea = createTextArea("Texto original");
        recoveredArea = createTextArea("Texto recuperado");
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(originalArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), new JScrollPane(recoveredArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        splitPane.setResizeWeight(0.5);
        originalArea.setLineWrap(true);
        originalArea.setWrapStyleWord(true);

        recoveredArea.setLineWrap(true);
        recoveredArea.setWrapStyleWord(true);

        statusLabel = new JLabel("Listo.");

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(topPanel, BorderLayout.NORTH);
        content.add(splitPane, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(content);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    public void showGui() {
        setVisible(true);
    }

    private JTextArea createTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(BorderFactory.createTitledBorder(title));
        return area;
    }

    private void selectFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedFile = chooser.getSelectedFile().toPath();
        selectedFileLabel.setText("Archivo: " + selectedFile.getFileName());
        if (selectedFile == null || !isHuffmanFile(selectedFile)) {
        originalArea.setText(loadFilePreview(selectedFile));
        statusLabel.setText("Archivo cargado: " + selectedFile.getFileName());}
    }

    private void protectFile(ActionEvent event) { //proteccion del archivo
        if (selectedFile == null || !selectedFile.toString().toLowerCase().endsWith(".txt")) {
            showError("Seleccione primero un archivo .txt para proteger.");
            return;
        }
        if (HuffmanFileProcess.processFile(selectedFile)) {//aqui la proteccion del archivo
            statusLabel.setText("Archivo comprimido exitosamente.");
        } else {
            showError("Error al comprimir el archivo.");
        }

    }

    private void decodeFile(ActionEvent event) {
        if (selectedFile == null || !isHuffmanFile(selectedFile)) {
            showError("Seleccione un archivo .HUF  para descomprimir.");
            return;
        }
      
        String res = HuffmanFileProcess.processFileHuf(selectedFile);
        if(res.isBlank()) {
            showError("Error al descomprimir") ;
        }else {
        	byte[] data;
			try {
				data = Files.readAllBytes(Path.of(res));
				String txt= new String(data, java.nio.charset.StandardCharsets.UTF_8);
	        	recoveredArea.setText(txt);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             }
    }

    private boolean isHuffmanFile(Path file) {
        String name = file.getFileName().toString().toUpperCase();
        return name.endsWith("HUF");
    }


    private String loadFilePreview(Path file) {
        try {
            if (file.toString().toLowerCase().endsWith(".txt")) {
                byte[] data = Files.readAllBytes(file);
                return new String(data, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "Archivo no es texto plano. Seleccione .txt para ver contenido.";
        } catch (IOException ex) {
            return "No se pudo leer el archivo: " + ex.getMessage();
        }
    }
  

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }
}
