package docflow;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Objects;

public class AddFontController {

    public Button cancelButton;
    public TextField nameField;
    public TextArea usageArea;
    private FontHandler fontHandler;

    public void cancel(ActionEvent actionEvent) {
        close();
    }

    private void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setFontHandler(FontHandler fontHandler) {
        this.fontHandler = fontHandler;
    }

    public void addFont(ActionEvent actionEvent) {
        if (!checkInput()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Please enter the font name and usage.");
            alert.showAndWait();
            return;
        }

        String name = nameField.getText().trim();
        String usage = usageArea.getText().trim().replaceAll("\n", " ");

        Font font = new Font(name, usage);
        fontHandler.addFont(font);

        close();
    }

    private boolean checkInput() {
        return !nameField.getText().trim().isEmpty()
            && !usageArea.getText().trim().isEmpty();
    }
}
