package byteController;
import huffman.HuffmanFileProcess;
import javax.swing.*;

import hamming.HammingFileProccesor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HaffminGUI extends JFrame {
    private Path selectedFile;
    private final JLabel selectedFileLabel;
    private final JLabel generatedFileLabel;
    private final JComboBox<String> moduleCombo;
    private final JComboBox<String> protectionTargetCombo;
    private final JTextArea originalArea;
    private final JTextArea recoveredArea;
    private final JLabel statusLabel;

    public HaffminGUI() {
        super("Hamming + Huffman") ;

        selectedFileLabel = new JLabel("Archivo: ninguno");
        generatedFileLabel = new JLabel("Salida: ninguno");
        moduleCombo = new JComboBox<>(new String[]{"8 bits (.HA1)", "1024 bits (.HA2)", "16384 bits (.HA3)"});
        protectionTargetCombo = new JComboBox<>(new String[]{"Texto original", "Archivo compactado"});

        JButton selectButton = new JButton("Cargar archivo");
        JButton compactButton = new JButton("Compactar Huffman");
        JButton decompactButton = new JButton("Descompactar Huffman");
        JButton protectButton = new JButton("Proteger Hamming");
        JButton injectOneButton = new JButton("Introducir 1 error");
        JButton injectTwoButton = new JButton("Introducir 2 errores");
        JButton decodeRawButton = new JButton("Desproteger sin corregir");
        JButton decodeCorrectButton = new JButton("Desproteger corrigiendo");
        JButton statsButton = new JButton("Ver estadísticas");
        JButton clearButton = new JButton("Limpiar");

        selectButton.addActionListener(this::selectFile);
        compactButton.addActionListener(this::compactFile);
        decompactButton.addActionListener(this::decompactFile);
        protectButton.addActionListener(this::protectFile);
        injectOneButton.addActionListener(this::injectOneError);
        injectTwoButton.addActionListener(this::injectTwoErrors);
        decodeRawButton.addActionListener(this::unprotectRaw);
        decodeCorrectButton.addActionListener(this::unprotectCorrect);
        statsButton.addActionListener(this::viewStatistics);
        clearButton.addActionListener(e -> clearAll());

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(selectedFileLabel, gbc);

        gbc.gridx = 1;
        topPanel.add(generatedFileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("Bloque Hamming:"), gbc);

        gbc.gridx = 1;
        topPanel.add(moduleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Aplicar Hamming a:"), gbc);

        gbc.gridx = 1;
        topPanel.add(protectionTargetCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.add(selectButton);
        buttons.add(compactButton);
        buttons.add(decompactButton);
        buttons.add(protectButton);
        buttons.add(injectOneButton);
        buttons.add(injectTwoButton);
        buttons.add(decodeRawButton);
        buttons.add(decodeCorrectButton);
        buttons.add(statsButton);
        buttons.add(clearButton);
        topPanel.add(buttons, gbc);

        originalArea = createTextArea("Texto original");
        recoveredArea = createTextArea("Texto recuperado / salida");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(originalArea), new JScrollPane(recoveredArea));
        splitPane.setResizeWeight(0.5);

        statusLabel = new JLabel("Listo.");

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(topPanel, BorderLayout.NORTH);
        content.add(splitPane, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(content);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1920, 1080);
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
        generatedFileLabel.setText("Salida: ninguno");
        originalArea.setText(loadFilePreview(selectedFile));
        recoveredArea.setText("");
        statusLabel.setText("Archivo cargado: " + selectedFile.getFileName());
    }

    private void compactFile(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para compactar.");
            return;
        }
        statusLabel.setText("Compactando archivo...");
        boolean success = HuffmanFileProcess.processFile(selectedFile);
        if (success) {
            Path generated = Path.of(compactedFilePath(selectedFile));
            String generatedFileName = generated.getFileName().toString();
            statusLabel.setText("Archivo compactado exitosamente.");
            generatedFileLabel.setText("Generado: " + generatedFileName);
            String preview = loadFilePreview(generated);
            if (preview.isBlank()) {
                recoveredArea.setText("Archivo compactado creado: " + generatedFileName);
            } else {
                recoveredArea.setText(preview);
            }
        } else {
            showError("Error al compactar el archivo.");
        }
    }

    private void decompactFile(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para descompactar.");
            return;
        }
        statusLabel.setText("Descompactando archivo...");
        String resultPath = HuffmanFileProcess.processFileHuf(selectedFile);
        if (resultPath == null || resultPath.isBlank()) {
            showError("Error al descompactar el archivo.");
        } else {
            Path generated = Path.of(resultPath);
            generatedFileLabel.setText("Generado: " + generated.getFileName());
            statusLabel.setText("Archivo descompactado exitosamente.");
            recoveredArea.setText(loadFilePreview(generated));
        }
    }

    private void protectFile(ActionEvent event) {
    if (selectedFile == null) {
        showError("Seleccione primero un archivo para proteger.");
        return;
    }
    int moduleBits = selectedModuleBits();
    if (moduleBits < 0) {
        showError("Seleccione un bloque válido para protección.");
        return;
    }
    statusLabel.setText("Protegiendo archivo...");
    Path archivoGenerado = HammingFileProccesor.processFileProtec(selectedFile, moduleBits);
    if (archivoGenerado != null) {
        statusLabel.setText("Archivo protegido exitosamente.");
        generatedFileLabel.setText("Generado: " + archivoGenerado.getFileName());
        recoveredArea.setText(loadFilePreview(archivoGenerado));
    } else {
        showError("Error al procesar y proteger el archivo.");
    }
}



   private void injectOneError(ActionEvent event) {
    if (selectedFile == null) {
        showError("Seleccione primero un archivo para introducir errores.");
        return;
    }
    String nombreArchivo = selectedFile.getFileName().toString();
    int dotIndex = nombreArchivo.lastIndexOf('.');
    String extension = (dotIndex == -1) ? "" : nombreArchivo.substring(dotIndex);
    if (!extension.equalsIgnoreCase(".HA1") && 
        !extension.equalsIgnoreCase(".HA2") && 
        !extension.equalsIgnoreCase(".HA3")) {
        showError("Error: Solo se pueden introducir errores en archivos protegidos por Hamming (.HA1, .HA2, .HA3).\n" +
                  "Por favor, proteja el archivo primero.");
        return;
    }
    int moduleBits = selectedModuleBits();
    if (moduleBits < 0) {
        showError("Seleccione un bloque válido para protección.");
        return;
    }
    statusLabel.setText("Introduciendo un error...");
    Path pathOut = HammingFileProccesor.processFileError(selectedFile, moduleBits, true);
    if (pathOut != null) {
        this.selectedFile = pathOut;
        this.selectedFileLabel.setText("Archivo: " + pathOut.getFileName().toString());
        
        statusLabel.setText("Archivo modificado con 1 error exitosamente.");
        generatedFileLabel.setText("Generado: " + pathOut.getFileName().toString());
        recoveredArea.setText(loadFilePreview(pathOut));
    } else {
        showError("Error al introducir el error en el archivo.");
    }
}



    private void injectTwoErrors(ActionEvent event) {
         if (selectedFile == null) {
        showError("Seleccione primero un archivo para introducir errores.");
        return;
    }
    String nombreArchivo = selectedFile.getFileName().toString();
    int dotIndex = nombreArchivo.lastIndexOf('.');
    String extension = (dotIndex == -1) ? "" : nombreArchivo.substring(dotIndex);
    
    if (!extension.equalsIgnoreCase(".HA1") && 
        !extension.equalsIgnoreCase(".HA2") && 
        !extension.equalsIgnoreCase(".HA3")) {
        showError("Error: Solo se pueden introducir errores en archivos protegidos por Hamming (.HA1, .HA2, .HA3).\n" +
                  "Por favor, proteja el archivo primero.");
        return;
    }
    int moduleBits = selectedModuleBits();
    if (moduleBits < 0) {
        showError("Seleccione un bloque válido para protección.");
        return;
    }
    statusLabel.setText("Introduciendo un error...");
    Path pathOut = HammingFileProccesor.processFileError(selectedFile, moduleBits, false);
    if (pathOut != null) {
        this.selectedFile = pathOut;
        this.selectedFileLabel.setText("Archivo: " + pathOut.getFileName().toString());
        statusLabel.setText("Archivo modificado con 1 error exitosamente.");
        generatedFileLabel.setText("Generado: " + pathOut.getFileName().toString());
        recoveredArea.setText(loadFilePreview(pathOut));
    } else {
        showError("Error al introducir el error en el archivo.");
    }
    }




    private void unprotectRaw(ActionEvent event) {
        if (selectedFile == null) {
        showError("Seleccione primero un archivo para desproteger.");
        return;
    }
    int moduleBits = selectedModuleBits();
    if (moduleBits < 0) {
        showError("Seleccione un bloque válido para protección.");
        return;
    }
    statusLabel.setText("Desprotegiendo, corrigiendo errores y descompactando Huffman...");
    Path archivoDesprotegidox = HammingFileProccesor.unprotectFileProtect(selectedFile, moduleBits, false);

    if (archivoDesprotegidox == null) {
        showError("Error en Hamming: El archivo tiene demasiados errores (posible doble error detectado).");
        return;
    }else{
         statusLabel.setText("¡Proceso completo! Archivo recuperado con éxito.");
         String preview = loadFilePreview(archivoDesprotegidox);
        recoveredArea.setText(preview);

    }
    }


  private void unprotectCorrect(ActionEvent event) {
    if (selectedFile == null) {
        showError("Seleccione primero un archivo para desproteger.");
        return;
    }
    int moduleBits = selectedModuleBits();
    if (moduleBits < 0) {
        showError("Seleccione un bloque válido para protección.");
        return;
    }
    statusLabel.setText("Desprotegiendo, corrigiendo errores y descompactando Huffman...");
    Path archivoDesprotegidox = HammingFileProccesor.unprotectFileProtect(selectedFile, moduleBits, true);

    if (archivoDesprotegidox == null) {
        showError("Error en Hamming: El archivo tiene demasiados errores (posible doble error detectado).");
        return;
    }else{
         statusLabel.setText("¡Proceso completo! Archivo recuperado con éxito.");
         String preview = loadFilePreview(archivoDesprotegidox);
        recoveredArea.setText(preview);

    }
}

    private void viewStatistics(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para ver estadísticas.");
            return;
        }
        statusLabel.setText("Preparando estadísticas...");
        showInfo("Funcionalidad de estadísticas pendiente. Inserte su lógica de análisis aquí.");
    }

    private void clearAll() {
        selectedFile = null;
        selectedFileLabel.setText("Archivo: ninguno");
        generatedFileLabel.setText("Salida: ninguno");
        originalArea.setText("");
        recoveredArea.setText("");
        statusLabel.setText("Listo.");
    }

    private String loadFilePreview(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer decoded = decoder.decode(ByteBuffer.wrap(data));
            return decoded.toString();
        } catch (CharacterCodingException e) {
            return "Vista previa no disponible para archivo binario.";
        } catch (IOException e) {
            return "No se pudo leer el archivo para vista previa.";
        }
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText(message);
    }
        private String compactedFilePath(Path file) {
        String originalName = file.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        return file.getParent().resolve(baseName + ".HUF").toString();
    }
}

