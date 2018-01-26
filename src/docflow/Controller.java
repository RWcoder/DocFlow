package docflow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Controller {

    public Button createPDFButton;
    public ListView<String> fontSizePicker;
    public ComboBox<String> fontPicker;
    public Label fileLabel;
    public ProgressIndicator progressIndicator;
    public Label progressLabel;
    public CheckBox keepTeXCheck;
    public CheckBox defaultFontCheck;

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

        fontSizePicker.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            pdfService.setFontSize(newValue);
        });
        fontSizePicker.getSelectionModel().select(0);

        fontPicker.getSelectionModel().select(0);

        File f = new File("C:\\Users\\David\\Desktop\\tex_test\\project_notes.md");
        pdfService.setSourceFile(f);
        fileLabel.setText(f.getName());
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
        String fontSize = fontSizePicker.getSelectionModel().getSelectedItem();
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
        pdfService.restart();
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

    public void setKeepTeX(ActionEvent actionEvent) {
        pdfService.setKeepTeX(keepTeXCheck.isSelected());
    }

    public void onDefaultFontChecked(ActionEvent actionEvent) {
        if (defaultFontCheck.isSelected()) {
            fontPicker.setDisable(true);
            pdfService.clearFont();
        } else {
            fontPicker.setDisable(false);
            String selectedFont = fontPicker.getSelectionModel().getSelectedItem();
            pdfService.setFont(selectedFont);
        }
    }
}
