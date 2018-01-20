package docflow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class Controller {

    public ComboBox fontPicker;
    public Label fileLabel;

    private File workingFile;

    @FXML
    private void onCreatePDFClicked(ActionEvent event) {
        System.out.println("Clicked button");
    }

    @FXML
    private void openFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Markdown or Text file");
        File file = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());

        if (file == null) {
            return;
        }

        workingFile = file;
        fileLabel.setText(workingFile.getAbsolutePath());
    }
}
