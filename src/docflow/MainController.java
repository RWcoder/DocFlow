package docflow;

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
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class MainController {

    public Button createPDFButton;
    public ListView<String> fontSizePicker;
    public ComboBox<Font> fontPicker;
    public Label fileLabel;
    public ProgressIndicator progressIndicator;
    public CheckBox keepTeXCheck;
    public CheckBox titlePageCheck;
    public TextArea consoleTextArea;
    public Button editFileButton;
    public Button cancelButton;

    private File sourceFile;
    private boolean fileSelected = false;
    private PDFCreationService pdfService;
    private FontHandler fontHandler;

    @FXML
    private void initialize() {
        pdfService = new PDFCreationService();
        pdfService.setOnSucceeded(s -> onPDFCompileSuccess());
        pdfService.setOnFailed(f -> onPDFServiceStopped());
        pdfService.setOnCancelled(c -> onPDFServiceStopped());

        Console console = new Console(consoleTextArea);
        PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);

        fontSizePicker.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            pdfService.setFontSize(newValue);
        });
        fontSizePicker.getSelectionModel().select(0);

        fontHandler = new FontHandler();
        ObservableList<Font> fonts = fontHandler.getFonts();
        Font defaultFont = new Font("Computer Modern (default)", "");
        fonts.add(0, defaultFont);
        fontPicker.setItems(fonts);
        fontPicker.getSelectionModel().select(0);
    }

    public void onClose() {
        try {
            fontHandler.updateFonts();
        } catch (JAXBException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Failed to save new fonts");
            alert.setContentText("Something went wrong. We couldn't write the new fonts.");
            alert.showAndWait();
        }
    }

    @FXML
    private void openFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Markdown or text file");
        File file = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());
        if (file != null) {
            setFile(file);
        }
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

        consoleTextArea.clear();
        pdfService.restart();
        createPDFButton.setDisable(true);
        progressIndicator.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void onPDFServiceStopped() {
        createPDFButton.setDisable(false);
        progressIndicator.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void onPDFCompileSuccess() {
        onPDFServiceStopped();
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

        AddFontController c = loader.getController();
        c.setFontHandler(fontHandler);

        addFontStage.setScene(addFontScene);
        addFontStage.show();
    }

    public void removeCurrentFile(ActionEvent actionEvent) {
        fileLabel.setTextFill(Color.web("#757575"));
        fileLabel.setText("(Choose or drag a file)");
        fileSelected = false;
        sourceFile = null;
        editFileButton.setDisable(true);
    }

    public void fileDragOver(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles() && !fileSelected) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
            fileLabel.setStyle("-fx-border-color: black; -fx-background-color: #BDBDBD;");
        } else {
            dragEvent.consume();
        }
    }

    public void fileDragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            if (files.size() == 1) {
                success = true;
                File file = files.get(0);
                setFile(file);
                fileLabel.setStyle("-fx-background-color: #BDBDBD;");
            }
        }

        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }

    private void setFile(File file) {
        sourceFile = file;
        fileLabel.setText(file.getName());
        fileLabel.setTextFill(Color.BLACK);
        fileSelected = true;
        pdfService.setSourceFile(file);
        editFileButton.setDisable(false);
    }

    public void editFile(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(sourceFile);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Couldn't open file: IOException");
            alert.showAndWait();
        }
    }

    public void cancel(ActionEvent actionEvent) {
        pdfService.cancel();
    }
}
