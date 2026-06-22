package byteController;
import huffman.HuffmanFileProcess;
import javax.swing.*;
import hamming.HammingFileProccesor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.text.SimpleDateFormat;

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
        JButton protectWithDateButton = new JButton("Comprimir con fecha");
        JButton injectOneWithDateButton = new JButton("Introducir 1 error (fecha)");
        JButton injectTwoWithDateButton = new JButton("Introducir 2 errores (fecha)");
        JButton unprotectRawWithDateButton = new JButton("Desproteger sin corregir (fecha)");
        JButton unprotectCorrectWithDateButton = new JButton("Desproteger corrigiendo (fecha)");
        JButton statsButton = new JButton("Ver estadísticas");
        JButton clearButton = new JButton("Limpiar");

        selectButton.addActionListener(this::selectFile);
        compactButton.addActionListener(this::compactFile);
        decompactButton.addActionListener(this::decompactFile);
        protectButton.addActionListener(this::protectFile);
        protectWithDateButton.addActionListener(this::showProtectWithDateDialog);

        injectOneWithDateButton.addActionListener(this::injectOneErrorWithDate);
        injectTwoWithDateButton.addActionListener(this::injectTwoErrorsWithDate);
        unprotectRawWithDateButton.addActionListener(this::unprotectRawWithDate);
        unprotectCorrectWithDateButton.addActionListener(this::unprotectCorrectWithDate);
        
        
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
        gbc.gridwidth = 2;
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        filePanel.add(selectButton);
        buttonContainer.add(filePanel);
        
        JPanel huffmanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        huffmanPanel.setBorder(BorderFactory.createTitledBorder("Huffman"));
        huffmanPanel.add(compactButton);
        huffmanPanel.add(decompactButton);
        buttonContainer.add(huffmanPanel);
        
        JPanel hammingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        hammingPanel.setBorder(BorderFactory.createTitledBorder("Hamming"));
        hammingPanel.add(protectButton);
        hammingPanel.add(injectOneButton);
        hammingPanel.add(injectTwoButton);
        hammingPanel.add(decodeRawButton);
        hammingPanel.add(decodeCorrectButton);
        buttonContainer.add(hammingPanel);

        JPanel hammingDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        hammingDatePanel.setBorder(BorderFactory.createTitledBorder("Hamming con Fechas"));
        hammingDatePanel.add(protectWithDateButton);
        hammingDatePanel.add(injectOneWithDateButton);
        hammingDatePanel.add(injectTwoWithDateButton);
        hammingDatePanel.add(unprotectRawWithDateButton);
        hammingDatePanel.add(unprotectCorrectWithDateButton);
        buttonContainer.add(hammingDatePanel);
        
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolsPanel.setBorder(BorderFactory.createTitledBorder("Herramientas"));
        toolsPanel.add(statsButton);
        toolsPanel.add(clearButton);
        buttonContainer.add(toolsPanel);
 
        
        topPanel.add(buttonContainer, gbc); 



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

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setBorder(null); 

        setContentPane(mainScroll);
        // ------------------------------------------

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        pack();
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        pack();
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void showGui() {
        setVisible(true);
    }

    private JTextArea createTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
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

   

    private void showProtectWithDateDialog(ActionEvent event) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para proteger.");
            return;
        }
        int moduleBits = selectedModuleBits();
        if (moduleBits < 0) {
            showError("Seleccione un bloque válido para protección.");
            return;
        }

        JDialog dialog = new JDialog(this, "Seleccionar fecha de lectura", true);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        center.add(new JLabel("¿Hasta qué fecha/hora el archivo podra ser Leido?"));

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm:ss"));
        center.add(dateSpinner);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Confirmar y Proteger");
        JButton cancel = new JButton("Cancelar");

        ok.addActionListener(e -> {
            Date selected = (Date) dateSpinner.getValue();
            long unlockDateMs = selected.getTime();

            statusLabel.setText("Protegiendo archivo con candado temporal...");
            dialog.dispose();
            HuffmanFileProcess.processFile(selectedFile);
            Path generated = Path.of(compactedFilePath(selectedFile));
            Path archivoGenerado = HammingFileProccesor.processFileProtecWithDate(generated, moduleBits, unlockDateMs);
            try {
				Files.deleteIfExists(generated);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            if (archivoGenerado != null) {
                selectedFile = archivoGenerado;
                statusLabel.setText("¡Éxito! Archivo bloqueado hasta: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(selected));
                generatedFileLabel.setText("Generado: " + archivoGenerado.getFileName());
                recoveredArea.setText(loadFilePreview(archivoGenerado));
            } else {
                showError("Error al procesar y proteger el archivo con fecha.");
            }
        });

        cancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(ok);
        btnPanel.add(cancel);

        dialog.add(center, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void injectOneErrorWithDate(ActionEvent event) {
        injectErrorWithDate(true);
    }

    private void injectTwoErrorsWithDate(ActionEvent event) {
        injectErrorWithDate(false);
    }

    private void injectErrorWithDate(boolean oneError) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para introducir errores.");
            return;
        }
        String nombreArchivo = selectedFile.getFileName().toString();
        if (!nombreArchivo.toUpperCase().contains(".HAT")) {
            showError("Error: Solo se pueden introducir errores con este botón en archivos protegidos con fecha (.HAT1, .HAT2, .HAT3).");
            return;
        }
        int moduleBits = selectedModuleBits();
        if (moduleBits < 0) {
            showError("Seleccione un bloque válido.");
            return;
        }
        statusLabel.setText("Introduciendo error(es) saltando el candado temporal...");
        
        Path pathOut = HammingFileProccesor.processFileErrorWithDate(selectedFile, moduleBits, oneError);
        
        if (pathOut != null) {
            this.selectedFile = pathOut;
            this.selectedFileLabel.setText("Archivo: " + pathOut.getFileName().toString());
            statusLabel.setText("Archivo modificado con error exitosamente.");
            generatedFileLabel.setText("Generado: " + pathOut.getFileName().toString());
            recoveredArea.setText(loadFilePreview(pathOut));
        } else {
            showError("Error al introducir errores.");
        }
    }

    private void unprotectRawWithDate(ActionEvent event) {
        unprotectWithDate(false);
    }

    private void unprotectCorrectWithDate(ActionEvent event) {
        unprotectWithDate(true);
    }

    private void unprotectWithDate(boolean correct) {
        if (selectedFile == null) {
            showError("Seleccione primero un archivo para desproteger.");
            return;
        }
        int moduleBits = selectedModuleBits();
        if (moduleBits < 0) {
            showError("Seleccione un bloque válido.");
            return;
        }
        statusLabel.setText("Verificando candado temporal y decodificando...");

        Path archivoDesprotegido = HammingFileProccesor.unprotectFileProtectWithDate(selectedFile, moduleBits, correct);
        String resultPath = HuffmanFileProcess.processFileHuf(archivoDesprotegido);
        Path generated = Path.of(resultPath);
        try {
			Files.deleteIfExists(archivoDesprotegido);
		} catch (IOException e) {
			e.printStackTrace();
		}
        if (archivoDesprotegido == null) {
            showError("ACCESO DENEGADO O FALLO FATAL: El archivo está bloqueado por fecha o tiene demasiados errores que impiden su lectura.");
        } else {
            statusLabel.setText("¡Proceso completo! Archivo validado y recuperado.");
            generatedFileLabel.setText(generated.getFileName().toString());
            String preview = loadFilePreview(generated);
            recoveredArea.setText(preview);
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
    JDialog compareDialog = new JDialog(this, "Estadísticas y Comparación Visual", true);
    compareDialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel labelA = new JLabel("Archivo A (Original): Ninguno");
    JButton btnA = new JButton("Seleccionar A");
    final Path[] fileA = {null};
    
    JLabel labelB = new JLabel("Archivo B (Resultado): Ninguno");
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

    JButton btnProcesar = new JButton("Generar Gráfica");
    btnProcesar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    
    btnProcesar.addActionListener(e -> {
        if (fileA[0] == null || fileB[0] == null) {
            JOptionPane.showMessageDialog(compareDialog, "Por favor, seleccione ambos archivos.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            long sizeA = Files.size(fileA[0]);
            long sizeB = Files.size(fileB[0]);
            double reduction = sizeA == 0 ? 0 : ((double) (sizeA - sizeB) / sizeA) * 100;

            JDialog reportDialog = new JDialog(this, "Reporte de Rendimiento", true);
            reportDialog.setLayout(new BorderLayout(10, 10));

            StatsChartPanel chart = new StatsChartPanel(
                fileA[0].getFileName().toString(), sizeA, 
                fileB[0].getFileName().toString(), sizeB
            );
            reportDialog.add(chart, BorderLayout.CENTER);
            JPanel textSummaryPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            textSummaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            
            JLabel lblReduccion = new JLabel(String.format("Porcentaje de variación / Reducción: %.2f %%", reduction));
            lblReduccion.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            
            long mismatchIndex = Files.mismatch(fileA[0], fileB[0]);
            JLabel lblIntegridad = new JLabel(mismatchIndex == -1 ? "✓ Los archivos son idénticos binariamente." : "⚠ Los archivos difieren (Con ruido/cambios).");
            lblIntegridad.setForeground(mismatchIndex == -1 ? new Color(34, 139, 34) : Color.RED);

            textSummaryPanel.add(lblReduccion);
            textSummaryPanel.add(lblIntegridad);
            reportDialog.add(textSummaryPanel, BorderLayout.SOUTH);

            reportDialog.pack();
            reportDialog.setSize(550, 320);
            reportDialog.setLocationRelativeTo(this);
            compareDialog.dispose();
            reportDialog.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(compareDialog, "Error al procesar tamaños: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            try {
                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                return decoder.decode(ByteBuffer.wrap(data)).toString();
            } catch (CharacterCodingException e) {
                CharsetDecoder decoderLatin1 = StandardCharsets.ISO_8859_1.newDecoder();
                decoderLatin1.onMalformedInput(CodingErrorAction.REPORT);
                decoderLatin1.onUnmappableCharacter(CodingErrorAction.REPORT);
                try {
                    return decoderLatin1.decode(ByteBuffer.wrap(data)).toString();
                } catch (CharacterCodingException ex) {
                    return "Vista previa no disponible para archivo binario.";
                }
            }
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

        private String compactedFilePath(Path file) {
        String originalName = file.getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        return file.getParent().resolve(baseName + ".HUF").toString();
    }

private static class StatsChartPanel extends JPanel {
    private final long sizeA;
    private final long sizeB;
    private final String nameA;
    private final String nameB;

    public StatsChartPanel(String nameA, long sizeA, String nameB, long sizeB) {
        this.nameA = nameA;
        this.sizeA = sizeA;
        this.nameB = nameB;
        this.sizeB = sizeB;
        setPreferredSize(new Dimension(450, 200));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        
        long max = Math.max(sizeA, sizeB);
        if (max == 0) max = 1;

        int barMaxW = width - 240; 
        if (barMaxW < 50) barMaxW = 50;

        int barWidthA = (int) ((sizeA * barMaxW) / max);
        int barWidthB = (int) ((sizeB * barMaxW) / max);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // --- DIBUJAR BARRA A ---
        g2.setColor(new Color(70, 130, 180)); 
        g2.fillRect(130, 40, barWidthA, 35);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(nameA, 15, 62);
        g2.drawString(sizeA + " B", 140 + barWidthA, 62);

        // --- DIBUJAR BARRA B ---
        g2.setColor(new Color(46, 139, 87));
        g2.fillRect(130, 100, barWidthB, 35);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(nameB, 15, 122);
        g2.drawString(sizeB + " B", 140 + barWidthB, 122);

        // Eje de simetría
        g2.setColor(Color.GRAY);
        g2.drawLine(130, 25, 130, 155);
    }
}
}



