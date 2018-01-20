package docflow;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.HashMap;

public class PDFCreationService extends Service<File> {

    private HashMap<String, String> options;
    private File sourceFile;

    public PDFCreationService() {
        this.options = new HashMap<>();
    }

    protected Task<File> createTask() {
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                return null;
            }
        };
    }

    public void setFontSize(int fontSize) {
        options.put("fontsize", Integer.toString(fontSize));
    }

    public void setFont(String fontName) {
        options.put("mainfont", String.format("\"%s\"", fontName));
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
}
