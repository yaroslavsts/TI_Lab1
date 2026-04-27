package rsa;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

public class GuiApp extends JFrame {
    private static final int PREVIEW_LIMIT = 5000;

    private final JTextField pField = new JTextField();
    private final JTextField qField = new JTextField();
    private final JTextField kcField = new JTextField();
    private final JTextField nField = new JTextField();
    private final JTextField phiField = new JTextField();
    private final JTextField koField = new JTextField();
    private final JTextArea inputArea = new JTextArea(8, 60);
    private final JTextArea outputArea = new JTextArea(8, 60);

    private final JButton initButton = new JButton("Принять P/Q/KC");
    private final JButton openButton = new JButton("Открыть файл");
    private final JButton encryptButton = new JButton("Шифровать");
    private final JButton decryptButton = new JButton("Дешифровать");
    private final JButton saveButton = new JButton("Сохранить результат");

    private final RSA rsa = new RSA();
    private byte[] sourceBytes;
    private byte[] resultBytes;

    public GuiApp() {
        super("RSA File Encryptor (Java)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new GridLayout(3, 4, 6, 6));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        top.add(new JLabel("P (простое):"));
        top.add(pField);
        top.add(new JLabel("Q (простое):"));
        top.add(qField);
        top.add(new JLabel("KC (закрытый):"));
        top.add(kcField);
        top.add(new JLabel("n = P*Q:"));
        nField.setEditable(false);
        top.add(nField);
        top.add(new JLabel("phi(n):"));
        phiField.setEditable(false);
        top.add(phiField);
        top.add(new JLabel("KO (открытый):"));
        koField.setEditable(false);
        top.add(koField);

        JPanel center = new JPanel(new GridLayout(2, 1, 6, 6));
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputArea.setRows(5);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setRows(5);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Открытый файл (превью)"));
        outputScroll.setBorder(BorderFactory.createTitledBorder("Результат"));
        center.add(inputScroll);
        center.add(outputScroll);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        Dimension buttonSize = new Dimension(190, 28);
        initButton.setPreferredSize(buttonSize);
        openButton.setPreferredSize(buttonSize);
        encryptButton.setPreferredSize(buttonSize);
        decryptButton.setPreferredSize(buttonSize);
        saveButton.setPreferredSize(buttonSize);
        bottom.add(initButton);
        bottom.add(openButton);
        bottom.add(encryptButton);
        bottom.add(decryptButton);
        bottom.add(saveButton);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        inputArea.setEditable(false);
        outputArea.setEditable(false);
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        saveButton.setEnabled(false);

        bindActions();

        pack();
        setMinimumSize(new Dimension(760, 420));
        setSize(new Dimension(820, 460));
        setLocationRelativeTo(null);
    }

    private void bindActions() {
        initButton.addActionListener(e -> initializeRsa());
        openButton.addActionListener(e -> openFile());
        encryptButton.addActionListener(e -> encrypt());
        decryptButton.addActionListener(e -> decrypt());
        saveButton.addActionListener(e -> saveResult());
    }

    private void initializeRsa() {
        long p;
        long q;
        long kc;
        try {
            p = Long.parseLong(pField.getText().trim());
            q = Long.parseLong(qField.getText().trim());
            kc = Long.parseLong(kcField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("P, Q и KC должны быть целыми числами.");
            return;
        }

        ValidationResult pValid = InputValidator.validatePrime(p, "P");
        if (!pValid.isValid()) {
            showError(pValid.getMessage());
            return;
        }
        ValidationResult qValid = InputValidator.validatePrime(q, "Q");
        if (!qValid.isValid()) {
            showError(qValid.getMessage());
            return;
        }
        ValidationResult distinct = InputValidator.validateDistinctPrimes(p, q);
        if (!distinct.isValid()) {
            showError(distinct.getMessage());
            return;
        }
        ValidationResult modulus = InputValidator.validateModulus(p, q);
        if (!modulus.isValid()) {
            showError(modulus.getMessage());
            return;
        }

        long phi = (p - 1) * (q - 1);
        ValidationResult kcValid = InputValidator.validateKc(kc, phi);
        if (!kcValid.isValid()) {
            showError(kcValid.getMessage());
            return;
        }

        if (!rsa.initialize(p, q, kc)) {
            showError("Не удалось вычислить KO.");
            return;
        }

        nField.setText(String.valueOf(rsa.getN()));
        phiField.setText(String.valueOf(rsa.getPhi()));
        koField.setText(String.valueOf(rsa.getKo()));
        encryptButton.setEnabled(true);
        decryptButton.setEnabled(true);

        JOptionPane.showMessageDialog(this, "Параметры RSA приняты.", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        setControlsEnabled(false);
        inputArea.setText("Загрузка файла, подождите...");
        outputArea.setText("");

        SwingWorker<LoadedFileData, Void> worker = new SwingWorker<>() {
            @Override
            protected LoadedFileData doInBackground() throws Exception {
                byte[] bytes = FileUtils.readAllBytes(selected.getAbsolutePath());
                boolean encrypted = isEncryptedSource(bytes);
                String preview = encrypted
                    ? toUShortString(bytes, PREVIEW_LIMIT)
                    : toByteString(bytes, PREVIEW_LIMIT);
                return new LoadedFileData(bytes, preview);
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    LoadedFileData data = get();
                    sourceBytes = data.bytes();
                    resultBytes = null;
                    saveButton.setEnabled(false);
                    inputArea.setText(data.preview());
                } catch (Exception ex) {
                    inputArea.setText("");
                    showError("Ошибка чтения файла: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void encrypt() {
        if (sourceBytes == null || sourceBytes.length == 0) {
            showError("Сначала откройте входной файл.");
            return;
        }
        resultBytes = rsa.encryptData(sourceBytes);
        outputArea.setText(toUShortString(resultBytes, PREVIEW_LIMIT));
        saveButton.setEnabled(true);
    }

    private void decrypt() {
        if (sourceBytes == null || sourceBytes.length == 0) {
            showError("Сначала откройте входной файл.");
            return;
        }
        resultBytes = rsa.decryptData(sourceBytes);
        if (resultBytes == null) {
            showError("Невозможно дешифровать: длина данных должна быть чётной.");
            return;
        }
        outputArea.setText(toByteString(resultBytes, PREVIEW_LIMIT));
        saveButton.setEnabled(true);
    }

    private void saveResult() {
        if (resultBytes == null) {
            showError("Нет результата для сохранения.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            FileUtils.writeAllBytes(selected.getAbsolutePath(), resultBytes);
            JOptionPane.showMessageDialog(this, "Файл сохранен.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Ошибка сохранения файла: " + ex.getMessage());
        }
    }

    private boolean isEncryptedSource(byte[] bytes) {
        if (bytes.length == 0 || bytes.length % 2 != 0) {
            return false;
        }
        for (int i = 0; i < bytes.length; i += 2) {
            int value = (bytes[i] & 0xFF) | ((bytes[i + 1] & 0xFF) << 8);
            if (value >= 256) {
                return true;
            }
        }
        return false;
    }

    private String toByteString(byte[] data, int limit) {
        if (data.length == 0) {
            return "(пусто)";
        }
        StringBuilder sb = new StringBuilder();
        int actualLimit = Math.min(data.length, limit);
        for (int i = 0; i < actualLimit; i++) {
            sb.append(data[i] & 0xFF);
            if (i < actualLimit - 1) {
                sb.append(' ');
            }
        }
        if (data.length > actualLimit) {
            sb.append("\n... Показаны первые ").append(actualLimit).append(" байт из ").append(data.length).append('.');
        }
        return sb.toString();
    }

    private String toUShortString(byte[] data, int limit) {
        if (data.length == 0) {
            return "(пусто)";
        }
        if (data.length % 2 != 0) {
            return "Ошибка: нечётная длина данных.";
        }
        StringBuilder sb = new StringBuilder();
        int blockCount = data.length / 2;
        int actualLimit = Math.min(blockCount, limit);
        for (int i = 0; i < actualLimit * 2; i += 2) {
            int value = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            sb.append(value);
            if (i < actualLimit * 2 - 2) {
                sb.append(' ');
            }
        }
        if (blockCount > actualLimit) {
            sb.append("\n... Показаны первые ").append(actualLimit).append(" блоков из ").append(blockCount).append('.');
        }
        return sb.toString();
    }

    private void setControlsEnabled(boolean enabled) {
        initButton.setEnabled(enabled);
        openButton.setEnabled(enabled);
        encryptButton.setEnabled(enabled && !koField.getText().isBlank());
        decryptButton.setEnabled(enabled && !koField.getText().isBlank());
        saveButton.setEnabled(enabled && resultBytes != null);
    }

    private record LoadedFileData(byte[] bytes, String preview) {
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new GuiApp().setVisible(true));
    }
}
