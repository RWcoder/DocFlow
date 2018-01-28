package docflow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.nashorn.internal.parser.JSONParser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class MainController {

    public Button createPDFButton;
    public ListView<String> fontSizePicker;
    public ComboBox<Font> fontPicker;
    public Label fileLabel;
    public ProgressIndicator progressIndicator;
    public Label progressLabel;
    public CheckBox keepTeXCheck;
    public CheckBox defaultFontCheck;
    public CheckBox titlePageCheck;

    private boolean fileSelected = false;
    private PDFCreationService pdfService;
    private FontHandler fontHandler;

    @FXML
    private void initialize() {
        pdfService = new PDFCreationService();
        progressLabel.textProperty().bind(pdfService.messageProperty());

        fontSizePicker.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            pdfService.setFontSize(newValue);
        });
        fontSizePicker.getSelectionModel().select(0);

        fontHandler = new FontHandler();
        fontPicker.setItems(fontHandler.getFonts());

        defaultFontCheck.setSelected(true);
        onDefaultFontChecked(null);
    }

    @FXML
    private void openFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Markdown or Text file");
        File file = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());

        if (file == null) {
            return;
        }

        fileSelected = true;
        fileLabel.setText(file.getName());
        pdfService.setSourceFile(file);
    }

    @FXML
    private void onFontSizePicked(ActionEvent event) {
        String fontSize = fontSizePicker.getSelectionModel().getSelectedItem();
        pdfService.setFontSize(fontSize);
    }

    @FXML
    private void onFontPicked(ActionEvent event) {
        Font font = fontPicker.getSelectionModel().getSelectedItem();
        pdfService.setFont(font);
    }

    @FXML
    private void onCreatePDFClicked(ActionEvent event) {
        if (!fileSelected) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Please select a file");
            alert.showAndWait();
            return;
        }

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
            Font selectedFont = fontPicker.getSelectionModel().getSelectedItem();
            pdfService.setFont(selectedFont);
        }
    }

    public void onTitlePageCheckChanged(ActionEvent actionEvent) {
        if (titlePageCheck.isSelected()) {
            pdfService.setTitlePage(true);
        } else {
            pdfService.setTitlePage(false);
        }
    }

    public void launchAddFontWindow(ActionEvent actionEvent) throws Exception {
        Stage addFontStage = new Stage();
        addFontStage.initModality(Modality.APPLICATION_MODAL);
        Node source = (Node) actionEvent.getSource();
        addFontStage.initOwner(source.getScene().getWindow());
        String fxmlFile = "add_font.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent parent = (Parent) loader.load();
        Scene addFontScene = new Scene(parent, 250, 300);

        AddFontController c = (AddFontController) loader.getController();
        c.setFontHandler(fontHandler);

        addFontStage.setScene(addFontScene);
        addFontStage.show();
    }
}