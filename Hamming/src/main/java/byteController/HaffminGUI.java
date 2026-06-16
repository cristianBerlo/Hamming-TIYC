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
    private final JTextArea originalArea;
    private final JTextArea recoveredArea;
    private final JLabel statusLabel;

    public HaffminGUI() {
        super("Hamming + Huffman") ;

        selectedFileLabel = new JLabel("Archivo: ninguno");
        generatedFileLabel = new JLabel("Salida: ninguno");
        moduleCombo = new JComboBox<>(new String[]{"8 bits (.HA1)", "1024 bits (.HA2)", "16384 bits (.HA3)"});

        JButton selectButton = new JButton("Cargar archivo");
        JButton compactButton = new JButton("Compactar Huffman");
        JButton decompactButton = new JButton("Descompactar Huffman");
        JButton protectButton = new JButton("Proteger Hamming");
        JButton injectOneButton = new JButton("Introducir 1 error");
        JButton injectTwoButton = new JButton("Introducir 2 errores");
        JButton decodeRawButton = new JButton("Desproteger sin corregir");
        JButton decodeCorrectButton = new JButton("Desproteger corrigiendo");
        JButton compareButton = new JButton("Comparar archivos");
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
        compareButton.addActionListener(this::compareFilesAction);
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
        buttons.add(compareButton);
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
            selectedFile = archivoGenerado;
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
        statusLabel.setText("Introduciendo dos errores...");
        Path pathOut = HammingFileProccesor.processFileError(selectedFile, moduleBits, false);
        if (pathOut != null) {
            this.selectedFile = pathOut;
            this.selectedFileLabel.setText("Archivo: " + pathOut.getFileName().toString());
            statusLabel.setText("Archivo modificado con 2 errores exitosamente.");
            generatedFileLabel.setText("Generado: " + pathOut.getFileName().toString());
            recoveredArea.setText(loadFilePreview(pathOut));
        } else {
            showError("Error al introducir los errores en el archivo.");
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
        } else {
            statusLabel.setText("¡Proceso completo! Archivo recuperado con éxito.");
            generatedFileLabel.setText(archivoDesprotegidox.getFileName().toString());
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
        } else {
            statusLabel.setText("¡Proceso completo! Archivo recuperado con éxito.");
            generatedFileLabel.setText(archivoDesprotegidox.getFileName().toString());
            String preview = loadFilePreview(archivoDesprotegidox);
            recoveredArea.setText(preview);
        }
    }

    private void viewStatistics(ActionEvent event) {
        statusLabel.setText("Preparando estadísticas...");
        compareFilesAction(event);
    }

   private void compareFilesAction(ActionEvent event) {
    JDialog compareDialog = new JDialog(this, "Comparador de Archivos", true);
    compareDialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel labelA = new JLabel("Archivo A: Ninguno");
    JButton btnA = new JButton("Seleccionar A");
    final Path[] fileA = {null}; // Contenedor para la ruta A
    JLabel labelB = new JLabel("Archivo B: Ninguno");
    JButton btnB = new JButton("Seleccionar B");
    final Path[] fileB = {null};
    btnA.addActionListener(e -> {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(compareDialog) == JFileChooser.APPROVE_OPTION) {
            fileA[0] = chooser.getSelectedFile().toPath();
            labelA.setText("Archivo A: " + fileA[0].getFileName().toString());
        }
    });

    btnB.addActionListener(e -> {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(compareDialog) == JFileChooser.APPROVE_OPTION) {
            fileB[0] = chooser.getSelectedFile().toPath();
            labelB.setText("Archivo B: " + fileB[0].getFileName().toString());
        }
    });
    JButton btnProcesar = new JButton("Comparar Archivos");
    btnProcesar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    
    btnProcesar.addActionListener(e -> {
        if (fileA[0] == null || fileB[0] == null) {
            JOptionPane.showMessageDialog(compareDialog, "Por favor, seleccione ambos archivos antes de continuar.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String report = compareFiles(fileA[0], fileB[0]);
            recoveredArea.setText(report);
            selectedFile = fileA[0];
            selectedFileLabel.setText("Archivo: " + fileA[0].getFileName());
            generatedFileLabel.setText("Comparación completa");
            statusLabel.setText("Comparación realizada con éxito.");
            compareDialog.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(compareDialog, "Error al leer los archivos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
    compareDialog.add(labelA, gbc);
    gbc.gridx = 1; gbc.weightx = 0.0;
    compareDialog.add(btnA, gbc);
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0;
    compareDialog.add(labelB, gbc);
    gbc.gridx = 1; gbc.weightx = 0.0;
    compareDialog.add(btnB, gbc);
    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    compareDialog.add(btnProcesar, gbc);
    compareDialog.pack();
    compareDialog.setSize(550, 180);
    compareDialog.setLocationRelativeTo(this);
    compareDialog.setVisible(true);
}
    private String compareFiles(Path fileA, Path fileB) throws IOException {
        long sizeA = Files.size(fileA);
        long sizeB = Files.size(fileB);
        long diff = sizeB - sizeA;
        double ratio = sizeA == 0 ? Double.NaN : (double) sizeB / sizeA;
        double reduction = sizeA == 0 ? 0 : ((double) (sizeA - sizeB) / sizeA) * 100;

        StringBuilder report = new StringBuilder();
        report.append("Comparación de archivos:\n");
        report.append("Archivo A: ").append(fileA.getFileName()).append("\n");
        report.append("Archivo B: ").append(fileB.getFileName()).append("\n\n");
        report.append("Tamaño A: ").append(sizeA).append(" bytes (" ).append(humanReadableSize(sizeA)).append(")\n");
        report.append("Tamaño B: ").append(sizeB).append(" bytes (" ).append(humanReadableSize(sizeB)).append(")\n");
        report.append("Diferencia: ").append(diff).append(" bytes\n");
        report.append("Relación B/A: ").append(String.format("%.2f", ratio)).append("\n");
        report.append("Reducción de tamaño de B respecto a A: ").append(String.format("%.2f", reduction)).append(" %\n");
        report.append("Extensión A: ").append(fileExtension(fileA)).append("\n");
        report.append("Extensión B: ").append(fileExtension(fileB)).append("\n");

        if (fileA.equals(fileB)) {
            report.append("\nLos archivos son el mismo archivo.");
        } else if (sizeA == sizeB) {
            report.append("\nLos archivos tienen el mismo tamaño.");
        } else if (sizeB > sizeA) {
            report.append("\nEl segundo archivo es más grande que el primero.");
        } else {
            report.append("\nEl segundo archivo es más pequeño que el primero.");
        }

        long mismatchIndex = Files.mismatch(fileA, fileB);
        if (mismatchIndex == -1) {
            report.append("\nLos archivos son binariamente idénticos.");
        } else {
            report.append("\nLos archivos difieren a partir del byte ").append(mismatchIndex).append(".");
        }

        return report.toString();
    }

    private String humanReadableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.2f MB", mb);
    }

    private String fileExtension(Path file) {
        String name = file.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex == -1 ? "(sin extensión)" : name.substring(dotIndex + 1).toUpperCase();
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

