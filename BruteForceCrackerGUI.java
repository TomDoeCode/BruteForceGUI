import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BruteForceCrackerGUI {
    // Zeichen, die verwendet werden (Buchstaben, Zahlen, Sonderzeichen)
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    // Sonderzeichen, die optional einbezogen werden
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'.,<>?/`~";

    // GUI-Komponenten
    private JPasswordField passwordField;
    private JTextArea outputArea;
    private JButton startButton, cancelButton;
    private JLabel attemptLabel, timeEstimateLabel;
    private JProgressBar progressBar;
    private JCheckBox includeNumbers, includeLetters, includeSpecial;
    private long startTime;
    private volatile boolean isRunning = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Variablen für Versuche und Kombinationen
    private long attemptCount = 0;
    private long totalCombinations = 0;

    // Konstruktor: baut die GUI auf
    public BruteForceCrackerGUI() {
        // Erstelle Hauptfenster
        JFrame frame = new JFrame("BruteForce by Tom:.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 350);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Farben für das Fenster
        Color backgroundColor = new Color(46, 52, 54);
        Color foregroundColor = new Color(211, 215, 207);
        Color buttonColor = new Color(114, 159, 207);
        Color textColor = new Color(233, 185, 110);
        frame.getContentPane().setBackground(backgroundColor);

        // Panel für Eingaben (4 Zeilen, 2 Spalten)
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBackground(backgroundColor);
        Font typewriterFont = new Font("Courier New", Font.PLAIN, 12);

        // Passwort-Eingabe
        JLabel passwordLabel = new JLabel("Passwort eingeben:");
        passwordLabel.setForeground(foregroundColor);
        passwordLabel.setFont(typewriterFont);
        inputPanel.add(passwordLabel);

        passwordField = new JPasswordField(10);
        passwordField.setFont(typewriterFont);
        inputPanel.add(passwordField);

        // Checkbox: Zahlen einbeziehen
        includeNumbers = new JCheckBox("Zahlen einbeziehen", true);
        includeNumbers.setBackground(backgroundColor);
        includeNumbers.setForeground(foregroundColor);
        includeNumbers.setFont(typewriterFont);
        inputPanel.add(includeNumbers);

        // Checkbox: Buchstaben einbeziehen
        includeLetters = new JCheckBox("Buchstaben einbeziehen", true);
        includeLetters.setBackground(backgroundColor);
        includeLetters.setForeground(foregroundColor);
        includeLetters.setFont(typewriterFont);
        inputPanel.add(includeLetters);

        // Checkbox: Sonderzeichen einbeziehen
        includeSpecial = new JCheckBox("Sonderzeichen einbeziehen", false);
        includeSpecial.setBackground(backgroundColor);
        includeSpecial.setForeground(foregroundColor);
        includeSpecial.setFont(typewriterFont);
        inputPanel.add(includeSpecial);

        // Leeres Label als Platzhalter
        inputPanel.add(new JLabel(""));

        // Start-Button
        startButton = new JButton("Starten");
        startButton.setBackground(buttonColor);
        startButton.setForeground(Color.BLACK);
        startButton.setFont(typewriterFont);

        // Abbrechen-Button
        cancelButton = new JButton("Abbrechen");
        cancelButton.setBackground(buttonColor);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(typewriterFont);

        inputPanel.add(startButton);
        inputPanel.add(cancelButton);
        frame.add(inputPanel, BorderLayout.NORTH);

        // Textbereich für Ausgaben
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(backgroundColor);
        outputArea.setForeground(textColor);
        outputArea.setFont(typewriterFont);
        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Panel für Statusanzeigen (Versuch, Zeit, Fortschritt)
        JPanel statusPanel = new JPanel(new GridLayout(3, 1));
        statusPanel.setBackground(backgroundColor);

        attemptLabel = new JLabel("Versuch: ");
        attemptLabel.setForeground(foregroundColor);
        attemptLabel.setFont(typewriterFont);
        statusPanel.add(attemptLabel);

        timeEstimateLabel = new JLabel("Geschätzte Zeit: Berechnung läuft...");
        timeEstimateLabel.setForeground(foregroundColor);
        timeEstimateLabel.setFont(typewriterFont);
        statusPanel.add(timeEstimateLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(backgroundColor);
        progressBar.setForeground(buttonColor);
        statusPanel.add(progressBar);

        frame.add(statusPanel, BorderLayout.SOUTH);

        // Action Listener für Buttons
        startButton.addActionListener(e -> startCracking());
        cancelButton.addActionListener(e -> cancelCracking());

        // Zeige das Fenster an
        frame.setVisible(true);
    }

    // Startet den BruteForce-Vorgang
    private void startCracking() {
        String targetPassword = new String(passwordField.getPassword());
        // Prüfen, ob ein Passwort eingegeben wurde
        if (targetPassword.isEmpty()) {
            appendOutput("Bitte geben Sie ein Passwort ein.\n");
            return;
        }
        appendOutput("Suche nach dem Passwort...\n");
        startButton.setEnabled(false);
        isRunning = true;
        startTime = System.currentTimeMillis();

        // Zeichensatz basierend auf den ausgewählten Optionen ermitteln
        String charset = getCharset();
        totalCombinations = (long) Math.pow(charset.length(), targetPassword.length());
        attemptCount = 0;
        updateTimeEstimateLabel(totalCombinations);

        // Starte die BruteForce-Methode in einem eigenen Thread
        executorService.execute(() -> {
            String foundPassword = bruteForce(targetPassword);
            long endTime = System.currentTimeMillis();
            // Passwort gefunden?
            if (foundPassword != null) {
                appendOutput("Passwort gefunden: " + foundPassword + "\n");
                appendOutput("Benötigte Zeit: " + (endTime - startTime) + " ms\n");
            } else {
                appendOutput("Brute-Force-Versuch gestoppt oder Passwort nicht gefunden.\n");
            }
            startButton.setEnabled(true);
        });
    }

    // Hängt Text an den Ausgabebereich an
    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> outputArea.append(text));
    }

    // Berechnet und zeigt die geschätzte Zeit an
    private void updateTimeEstimateLabel(long totalCombinations) {
        long estimatedMillis = totalCombinations / 1_000_000;
        SwingUtilities.invokeLater(() -> {
            if (estimatedMillis < 1_000) {
                timeEstimateLabel.setText("Geschätzte Zeit: " + estimatedMillis + " ms");
            } else if (estimatedMillis < 60_000) {
                timeEstimateLabel.setText("Geschätzte Zeit: " + (estimatedMillis / 1_000) + " Sekunden");
            } else {
                timeEstimateLabel.setText("Geschätzte Zeit: " + (estimatedMillis / 60_000) + " Minuten");
            }
        });
    }

    // Aktualisiert um den jeweils laufenden verusch
    private void updateAttemptLabel(String attempt) {
        SwingUtilities.invokeLater(() -> attemptLabel.setText("Versuch: " + attempt));
    }

    // Aktualisiert die Fortschrittsanzeige nicht wirklich aussagekräftig
    private void updateProgressBar(long attempts) {
        int progress = (int)((attempts * 100) / totalCombinations);
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    // Beenden
    private void cancelCracking() {
        isRunning = false;
        appendOutput("Brute-Force-Versuch abgebrochen.\n");
        startButton.setEnabled(true);
    }

    // Erzeugt einen Zeichensatz
    private String getCharset() {
        StringBuilder charset = new StringBuilder();
        if (includeLetters.isSelected()) {
            charset.append(LETTERS);
        }
        if (includeNumbers.isSelected()) {
            charset.append(NUMBERS);
        }
        if (includeSpecial.isSelected()) {
            charset.append(SPECIAL_CHARACTERS);
        }
        return charset.toString();
    }

    // Startet den Bruteforce-Prozess
    private String bruteForce(String targetPassword) {
        String charset = getCharset();
        // Array wird erstellt für den jeweiligen Versuch
        char[] currentAttempt = new char[targetPassword.length()];
        return bruteForceHelper(targetPassword, currentAttempt, charset, 0);
    }

    // Rekursive Methode, die alle Kombinationen ausprobiert
    private String bruteForceHelper(String targetPassword, char[] currentAttempt, String charset, int position) {
        // Wenn man Abbrechen drückt - von vorne
        if (!isRunning) {
            return null;
        }
        // checken obs das passende PW war
        if (position == targetPassword.length()) {
            attemptCount++;
            updateProgressBar(attemptCount);
            String attempt = new String(currentAttempt);
            updateAttemptLabel(attempt);
            // Richtiges Passwort gefunden?
            if (attempt.equals(targetPassword)) {
                return attempt;
            }
            return null;
        }
        // Der BruteForce-Vorgang
        for (int i = 0; i < charset.length(); i++) {
            currentAttempt[position] = charset.charAt(i);
            String result = bruteForceHelper(targetPassword, currentAttempt, charset, position + 1);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    // Main :=)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BruteForceCrackerGUI::new);
    }
}
