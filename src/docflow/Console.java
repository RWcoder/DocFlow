package docflow;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class Console extends OutputStream {

    private TextArea textArea;

    public Console(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int i) throws IOException {
        Platform.runLater(() -> textArea.appendText(String.valueOf((char) i)));
    }
}
