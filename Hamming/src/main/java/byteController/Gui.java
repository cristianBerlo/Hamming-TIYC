package byteController;
import huffman.HuffmanFileProcess;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.tika.Tika;

import hamming.HammingFileProccesor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gui extends JFrame {
    private Path selectedFile;
    private final JLabel selectedFileLabel;
    private final JComboBox<String> moduleCombo;
    private final JTextArea originalArea;
    private final JTextArea recoveredArea;
    private final JLabel statusLabel;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JLabel recoveredImageLabel;
    private final JLabel originalImageLabel;

    public Gui() {
        selectedFileLabel = new JLabel("Archivo: ninguno");
        moduleCombo = new JComboBox<>(new String[]{"8 bits (.HA1)", "1024 bits (.HA2)", "16384 bits (.HA3)"});
        JButton protectHuffmanutton = new JButton("Comprimir archivo");
        JButton decodeButton = new JButton("Descomprimir archivo");
        JButton selectButton = new JButton("Cargar archivo");
        JButton protectButton = new JButton("Proteger archivo");
        JButton injectButton = new JButton("Introducir un errore");
        JButton decodeRawButton = new JButton("Desproteger sin corregir");
        JButton decodeCorrectButton = new JButton("Desproteger corrigiendo");
        JButton errorsButton = new JButton("Introducir dos errores");

        protectHuffmanutton.addActionListener(this::protectFileHuffman);
        decodeButton.addActionListener(this::decodeFile);
        selectButton.addActionListener(this::selectFile);
        protectButton.addActionListener(this::protectFile);
        injectButton.addActionListener(e ->injectErrors(true));
        errorsButton.addActionListener(e ->injectErrors(false));
        decodeRawButton.addActionListener(e -> decodeFile(false));
        decodeCorrectButton.addActionListener(e -> decodeFile(true));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(selectedFileLabel, gbc);

        gbc.gridy = 1;
        topPanel.add(new JLabel("Bloque Hamming:"), gbc);

        gbc.gridx = 1;
        topPanel.add(moduleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.add(selectButton);
        buttons.add(protectButton);
        buttons.add(injectButton);
        buttons.add(errorsButton);
        buttons.add(decodeRawButton);
        buttons.add(decodeCorrectButton);
        buttons.add(protectHuffmanutton);
        buttons.add(decodeButton);
        topPanel.add(buttons, gbc);

        originalArea = createTextArea("Informacion original");
        recoveredArea = createTextArea("Recuperada recuperado");

         originalImageLabel = new JLabel();
        originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
         leftPanel = new JPanel(new CardLayout());
        leftPanel.add(new JScrollPane(originalArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "TEXT");
        leftPanel.add(new JScrollPane(originalImageLabel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "IMAGE");


         recoveredImageLabel = new JLabel();
        recoveredImageLabel.setHorizontalAlignment(JLabel.CENTER);
         rightPanel = new JPanel(new CardLayout());
        rightPanel.add(new JScrollPane(recoveredArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "TEXT");
        rightPanel.add(new JScrollPane(recoveredImageLabel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "IMAGE");


        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
       /* JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(originalArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), new JScrollPane(recoveredArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));*/
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

    private void decodeFile(ActionEvent event) {
     /*   if (selectedFile == null || !isHuffmanFile(selectedFile)) {
            showError("Seleccione un archivo .HUF  para descomprimir.");
            return;
        }
      */
        String res = HuffmanFileProcess.processFileHuf(selectedFile);
        if(res.isBlank()) {
            showError("Error al descomprimir") ;
        }else {
            loadFileRecovered(Path.of(res));
             }
    }

    private void protectFileHuffman(ActionEvent event) { //proteccion del archivo
        if (HuffmanFileProcess.processFile(selectedFile)) {//aqui la proteccion del archivo
            statusLabel.setText("Archivo comprimido exitosamente.");
        } else {
            showError("Error al comprimir el archivo.");
        }

    }

    private boolean isHuffmanFile(Path file) {
        String name = file.getFileName().toString().toUpperCase();
        return name.endsWith("HUF");
    }

    private void selectFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedFile = chooser.getSelectedFile().toPath();
        selectedFileLabel.setText("Archivo: " + selectedFile.getFileName());
        if (selectedFile == null || !isHammingFile(selectedFile) && !isHammingErrorFile(selectedFile)) {
        	loadFilePreview(selectedFile);
            statusLabel.setText("Archivo cargado: " + selectedFile.getFileName());} 
        }

    private void protectFile(ActionEvent event) { //proteccion del archivo
       
        int moduleBits = selectedModuleBits();
        if (moduleBits < 0) {
            showError("Seleccione un bloque válido para protección.");
            return;
        }
        if (HammingFileProccesor.processFileProtec(selectedFile, moduleBits)) {//aqui la proteccion del archivo
            statusLabel.setText("Archivo protegido exitosamente.");
        } else {
            showError("Error al proteger el archivo.");
        }
    }


    private void injectErrors(boolean one) {
        if (selectedFile == null || !isHammingFile(selectedFile)) {
            showError("Seleccione un archivo .HA1, .HA2 o .HA3 para introducir errores.");
            return;
        }
        int moduleBits = selectedModuleBits();
        if (HammingFileProccesor.processFileError(selectedFile, moduleBits, one)) {//aqui la proteccion del archivo
            statusLabel.setText("Errores inyectados exitosamente.");
        } else {
            showError("Error al inyectar errores el archivo.");
        }

      //  statusLabel.setText("Función de inyección de errores aún no implementada.");
    //    JOptionPane.showMessageDialog(this, "Inyección de errores temporalmente deshabilitada.", "Pendiente", JOptionPane.INFORMATION_MESSAGE);
    }
    private void decodeFile(boolean correct) {
        if (selectedFile == null || !isHammingFile(selectedFile) && !isHammingErrorFile(selectedFile)) {
            showError("Seleccione un archivo .HAx o .HEx para desproteger.");
            return;
        }
        Path res = HammingFileProccesor.unprotectFileProtect(selectedFile, selectedModuleBits(), correct);
        if( res!=null) {
        	loadFileRecovered(res);
        } else {
            showError("Error al desproteger el archivo. Puede haber demasiados errores para corregir.");
        }
    }

    private boolean isHammingFile(Path file) {
        String name = file.getFileName().toString().toUpperCase();
        return name.endsWith(".HA1") || name.endsWith(".HA2") || name.endsWith(".HA3") || name.endsWith(".HE1") || name.endsWith(".HE2") || name.endsWith(".HE3");
    }

    private boolean isHammingErrorFile(Path file) {
        String name = file.getFileName().toString().toUpperCase();
        return name.endsWith(".HE1") || name.endsWith(".HE2") || name.endsWith(".HE3");
    }

    private int selectedModuleBits() {
        String selection = (String) moduleCombo.getSelectedItem();
        if (selection == null) {
            return -1;
        }
        if (selection.startsWith("8 ")) return 8;
        if (selection.startsWith("1024")) return 1024;
        if (selection.startsWith("16384")) return 16384;
        return -1;
    }

    private void loadFilePreview(Path file) {
    	Tika tika = new Tika();
    	File fileTika = new File(file.toString());
        String mimeType;
        try {
			mimeType = tika.detect(fileTika);
			if (mimeType != null && mimeType.startsWith("image/")) {
			    BufferedImage img = ImageIO.read(fileTika);

			    if (img != null) {
	                CardLayout cl = (CardLayout) leftPanel.getLayout();
			        // scale to current label size
			        int labelW = recoveredImageLabel.getWidth();
			        int labelH = recoveredImageLabel.getHeight();

			        if (labelW > 0 && labelH > 0) {
			            int imgW = img.getWidth();
			            int imgH = img.getHeight();

			            double scale = Math.min((double) labelW / imgW, (double) labelH / imgH);

			            int newW = (int) (imgW * scale);
			            int newH = (int) (imgH * scale);

			            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		                originalImageLabel.setIcon(new ImageIcon(scaled));
			        }

	                cl.show(leftPanel, "IMAGE");
			    }
			}
                 else {
	            byte[] data = Files.readAllBytes(file);
	            originalArea.setText( new String(data, java.nio.charset.StandardCharsets.UTF_8));
	            CardLayout cl = (CardLayout) leftPanel.getLayout();
	            cl.show(leftPanel, "TEXT");
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void loadFileRecovered(Path file) {
    	Tika tika = new Tika();
    	File fileTika = new File(file.toString());
        String mimeType;
        try {
			mimeType = tika.detect(fileTika);
			if (mimeType != null && mimeType.startsWith("image/")) {
			    BufferedImage img = ImageIO.read(fileTika);

			    if (img != null) {
			        CardLayout cl = (CardLayout) rightPanel.getLayout();
			        // scale to current label size
			        int labelW = recoveredImageLabel.getWidth();
			        int labelH = recoveredImageLabel.getHeight();

			        if (labelW > 0 && labelH > 0) {
			            int imgW = img.getWidth();
			            int imgH = img.getHeight();

			            double scale = Math.min((double) labelW / imgW, (double) labelH / imgH);

			            int newW = (int) (imgW * scale);
			            int newH = (int) (imgH * scale);

			            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
			            recoveredImageLabel.setIcon(new ImageIcon(scaled));
			        }

			        cl.show(rightPanel, "IMAGE");
			    }
			} else {
	            byte[] data = Files.readAllBytes(file);
	            recoveredArea.setText( new String(data, java.nio.charset.StandardCharsets.UTF_8));
	            CardLayout cl = (CardLayout) rightPanel.getLayout();
	            cl.show(rightPanel, "TEXT");
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }
}
