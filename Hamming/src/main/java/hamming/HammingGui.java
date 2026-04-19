package hamming;
import byteController.HammingFileProccesor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HammingGui extends JFrame {
    private Path selectedFile;
    private final JLabel selectedFileLabel;
    private final JComboBox<String> moduleCombo;
    private final JTextArea originalArea;
    private final JTextArea recoveredArea;
    private final JLabel statusLabel;

    public HammingGui() {
        super("Hamming File Protector");

        selectedFileLabel = new JLabel("Archivo: ninguno");
        moduleCombo = new JComboBox<>(new String[]{"8 bits (.HA1)", "1024 bits (.HA2)", "16384 bits (.HA3)"});

        JButton selectButton = new JButton("Cargar archivo");
        JButton protectButton = new JButton("Proteger archivo");
        JButton injectButton = new JButton("Introducir errores");
        JButton decodeRawButton = new JButton("Desproteger sin corregir");
        JButton decodeCorrectButton = new JButton("Desproteger corrigiendo");

        selectButton.addActionListener(this::selectFile);
        protectButton.addActionListener(this::protectFile);
        injectButton.addActionListener(this::injectErrors);
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
        buttons.add(decodeRawButton);
        buttons.add(decodeCorrectButton);
        topPanel.add(buttons, gbc);

        originalArea = createTextArea("Texto original");
        recoveredArea = createTextArea("Texto recuperado");

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
        originalArea.setText(loadFilePreview(selectedFile));//
        recoveredArea.setText("");
        statusLabel.setText("Archivo cargado: " + selectedFile.getFileName());
    }

    private void protectFile(ActionEvent event) { //proteccion del archivo
        if (selectedFile == null || !selectedFile.toString().toLowerCase().endsWith(".txt")) {
            showError("Seleccione primero un archivo .txt para proteger.");
            return;
        }

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

    private void injectErrors(ActionEvent event) {
        if (selectedFile == null || !isHammingFile(selectedFile)) {
            showError("Seleccione un archivo .HA1, .HA2 o .HA3 para introducir errores.");
            return;
        }
        int moduleBits = selectedModuleBits();
        if (HammingFileProccesor.processFileError(selectedFile, moduleBits)) {//aqui la proteccion del archivo
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

        if(HammingFileProccesor.unprotectFileProtect(selectedFile, selectedModuleBits(), correct) != null) {
            statusLabel.setText("Archivo desprotegido exitosamente.");
            recoveredArea.setText(loadFilePreview(Path.of("archivo_recuperado.txt")));
        } else {
            showError("Error al desproteger el archivo. Puede haber demasiados errores para corregir.");
        }
    }

    private boolean isHammingFile(Path file) {
        String name = file.getFileName().toString().toUpperCase();
        return name.endsWith(".HA1") || name.endsWith(".HA2") || name.endsWith(".HA3");
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
