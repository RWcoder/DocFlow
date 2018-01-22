package docflow;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFCreationService extends Service<File> {

    private HashMap<String, String> options;
    private String sourceName;
    private File sourceFile;
    private File yamlFile;

    public PDFCreationService() {
        this.options = new HashMap<>();
    }

    protected Task<File> createTask() {
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                try {
                    return generatePDF();
                } catch (IOException | InterruptedException e) {
                    super.failed();
                    return null;
                }
            }
        };
    }

    private File generatePDF() throws IOException, InterruptedException {
        if (!createYAMLFile()) {
            super.failed();
            return null;
        }

        writeYAMLFile();

        System.out.println("wrote yaml");

        String texName = sourceName + ".tex";

        System.out.println("executing pandoc");
        List<String> pandocArgs = Arrays.asList(
                "pandoc",
                sourceFile.getName(),
                yamlFile.getName(),
                "-s",
                "-o",
                texName,
                "--pdf-engine=lualatex"
        );
        ProcessBuilder pandocProcess = new ProcessBuilder(pandocArgs);
        pandocProcess.directory(sourceFile.getParentFile());
        pandocProcess.start().waitFor();
        System.out.println("Pandoc done");

        yamlFile.delete();

        System.out.println("executing lualatex");
        ProcessBuilder latexProcess = new ProcessBuilder("lualatex", texName);
        File directory = sourceFile.getParentFile();
        File output = new File(directory, "output.txt");
        latexProcess.directory(sourceFile.getParentFile());
        latexProcess.inheritIO();
        System.out.println(latexProcess.command());
        latexProcess.start().waitFor();
        System.out.println("latex done");

        return null;
    }

    private boolean createYAMLFile() throws IOException {
        File sourceDirectory = sourceFile.getParentFile();
        String yamlFileName = sourceName + ".yaml";
        yamlFile = new File(sourceDirectory, yamlFileName);
        return yamlFile.createNewFile();
    }

    private void writeYAMLFile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(yamlFile);
        writer.println("---");

        for (Map.Entry<String, String> option : options.entrySet()) {
            String optionName = option.getKey();
            String optionValue = option.getValue();
            String yamlEntry = String.format("%s:%s", optionName, optionValue);
            writer.println(yamlEntry);
        }

        writer.println("subparagraph: yes");
        writer.println("header-includes:");
        writer.println("\t- \\usepackage[compact]{titlesec}");

        writer.print("...");
        writer.close();
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
        String sourceFileName = sourceFile.getName();
        int periodLoc = sourceFileName.lastIndexOf('.');
        sourceName = sourceFileName.substring(0, periodLoc);
    }
}
