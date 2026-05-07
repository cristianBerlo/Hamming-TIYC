package byteController;

import huffman.HuffmanFileProcess;

import javax.swing.*;
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

public class HuffmanGui extends JFrame {
    private Path selectedFile;
    private final JLabel selectedFileLabel;
    private final JLabel generatedFileLabel;
    private final JTextArea originalArea;
    private final JTextArea compactedArea;
    private final JLabel statusLabel;

    public HuffmanGui() {
        super("Huffman File Compressor");

        selectedFileLabel = new JLabel("Archivo: ninguno");
        generatedFileLabel = new JLabel("Generado: Null");

        JButton selectButton = new JButton("Cargar archivo");
        JButton compactButton = new JButton("Compactar archivo");
        JButton decompactButton = new JButton("Descompactar archivo");
        JButton statisticsButton = new JButton("Ver estadísticas");
        JButton clearButton = new JButton("Limpiar");

        selectButton.addActionListener(this::selectFile);
        compactButton.addActionListener(this::compactFile);
        decompactButton.addActionListener(this::decompactFile);
        statisticsButton.addActionListener(this::viewStatistics);
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
        gbc.gridwidth = 5;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.add(selectButton);
        buttons.add(compactButton);
        buttons.add(decompactButton);
        buttons.add(statisticsButton);
        buttons.add(clearButton);
        topPanel.add(buttons, gbc);

        originalArea = createTextArea("Archivo Original");
        compactedArea = createTextArea("Archivo Compactado/Descompactado");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(originalArea), new JScrollPane(compactedArea));
        splitPane.setResizeWeight(0.5);

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
        setVisible(true);
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
        generatedFileLabel.setText("Generado: Null");
        originalArea.setText(loadFilePreview(selectedFile));
        compactedArea.setText("");
        statusLabel.setText("Archivo cargado: " + selectedFile.getFileName());
    }

    private void compactFile(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para compactar.");
            return;
        }
        statusLabel.setText("Compactando...");
        boolean success = HuffmanFileProcess.processFile(selectedFile);
        if (success) {
            Path generated = Path.of(compactedFilePath(selectedFile));
            String generatedFileName = generated.getFileName().toString();
            statusLabel.setText("Archivo compactado exitosamente.");
            generatedFileLabel.setText("Generado: " + generatedFileName);
            String preview = loadFilePreview(generated);
            if (preview.isBlank()) {
                compactedArea.setText("Archivo compactado creado: " + generatedFileName);
            } else {
                compactedArea.setText(preview);
            }
        } else {
            showError("Error al compactar el archivo.");
        }
    }

    private void decompactFile(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo .HUF para descompactar.");
            return;
        }
        statusLabel.setText("Descompactando...");
        String resultPath = HuffmanFileProcess.processFileHuf(selectedFile);
        if (resultPath == null || resultPath.isBlank()) {
            showError("Error al descompactar el archivo.");
        } else {
            statusLabel.setText("Archivo descompactado exitosamente.");
            compactedArea.setText(loadFilePreview(Path.of(resultPath)));
        }
    }

    private void viewStatistics(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para ver estadísticas.");
            return;
        }
        try {
            long originalSize = Files.size(selectedFile);
            Path compressed = Path.of(compactedFilePath(selectedFile));
            StringBuilder stats = new StringBuilder();
            stats.append("Archivo seleccionado: ").append(selectedFile.getFileName()).append("\n");
            stats.append("Tamaño: ").append(originalSize).append(" bytes\n");

            if (Files.exists(compressed)) {
                long compressedSize = Files.size(compressed);
                stats.append("Archivo comprimido: ").append(compressed.getFileName()).append("\n");
                stats.append("Tamaño comprimido: ").append(compressedSize).append(" bytes\n");
                if (originalSize > 0) {
                    double ratio = (double) compressedSize / originalSize;
                    stats.append(String.format("Relación de compresión: %.2f%%\n", ratio * 100));
                }
            } else {
                stats.append("No se encontró el archivo comprimido esperado: ").append(compressed.getFileName()).append("\n");
            }

            JOptionPane.showMessageDialog(this, stats.toString(), "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Estadísticas mostradas.");
        } catch (IOException e) {
            showError("No se pudo obtener las estadísticas del archivo.");
        }
    }

    private void clearAll() {
        selectedFile = null;
        selectedFileLabel.setText("Archivo: ninguno");
        generatedFileLabel.setText("Generado: Null");
        originalArea.setText("");
        compactedArea.setText("");
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
            return "Archivo no posee formato legible.";
        } catch (IOException e) {
            return "No se pudo leer el archivo para vista previa.";
        }
    }

    private String compactedFilePath(Path file) {
        String originalName = file.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        return file.getParent().resolve(baseName + ".HUF").toString();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }
}
