package docflow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Controller {

    public Button createPDFButton;
    public ComboBox<Integer> fontSizePicker;
    public ComboBox<String> fontPicker;
    public Label fileLabel;
    public ProgressIndicator progressIndicator;
    public Console console;
    public TextArea consoleTextArea;
    public Label progressLabel;

    private PDFCreationService pdfService;

    @FXML
    private void initialize() {
        pdfService = new PDFCreationService();
        pdfService.setOnSucceeded(success -> onPDFCompileSuccess());
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = e.getAvailableFontFamilyNames();
        ObservableList<String> fontNamesList = FXCollections.observableArrayList(fontNames);
        fontPicker.setItems(fontNamesList);
        progressLabel.textProperty().bind(pdfService.messageProperty());
    }

    @FXML
    private void openFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Markdown or Text file");
        File file = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());

        if (file == null) {
            return;
        }

        fileLabel.setText(file.getAbsolutePath());
        pdfService.setSourceFile(file);
    }

    @FXML
    private void onFontSizePicked(ActionEvent event) {
        int fontSize = fontSizePicker.getSelectionModel().getSelectedItem();
        pdfService.setFontSize(fontSize);
    }

    @FXML
    private void onFontPicked(ActionEvent event) {
        String fontName = fontPicker.getSelectionModel().getSelectedItem();
        pdfService.setFont(fontName);
    }

    @FXML
    private void onCreatePDFClicked(ActionEvent event) {
        createPDFButton.setDisable(true);
        progressIndicator.setVisible(true);
        progressLabel.setVisible(true);
        pdfService.reset();
        pdfService.start();
    }

    private void onPDFCompileSuccess() {
        createPDFButton.setDisable(false);
        progressIndicator.setVisible(false);
        progressLabel.setVisible(false);

        File pdfFile = pdfService.getValue();
        try {
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
