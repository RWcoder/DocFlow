package docflow;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PDFCreationService extends Service<File> {

    private String fontName;
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

                    super.updateMessage("Writing YAML header...");

                    if (!createYAMLFile()) {
                        super.failed();
                        return null;
                    }

                    writeYAMLFile();


                    super.updateMessage("Converting to TeX...");

                    String texName = sourceName + ".tex";

                    System.out.println("executing pandoc");
                    List<String> pandocArgsList = Arrays.asList(
                            "pandoc",
                            sourceFile.getName(),
                            yamlFile.getName(),
                            "-s",
                            "-o",
                            texName,
                            "--pdf-engine=lualatex"
//                            "--template=template.latex"
                    );

                    ArrayList<String> pandocArgs = new ArrayList<>(pandocArgsList);

                    if (fontName != null) {
                        System.out.println("Adding font");
                        String fontThing = String.format("-V mainfont:\"%s\"", fontName);
                        pandocArgs.add(fontThing);
                        System.out.println("Font added");
                        System.out.println(pandocArgs);
                    }

                    File sourceDir = sourceFile.getParentFile();
                    String pandocCmd = String.join(" ", pandocArgs);
                    Runtime.getRuntime().exec(pandocCmd, null, sourceDir).waitFor();
                    System.out.println("Pandoc done");

//                    yamlFile.delete();

                    super.updateMessage("Compiling PDF...");

                    ProcessBuilder latexProcess = new ProcessBuilder("lualatex", texName);
                    latexProcess.directory(sourceFile.getParentFile());
                    latexProcess.inheritIO();
                    System.out.println(latexProcess.command());
                    latexProcess.start().waitFor();

                    String pdfName = sourceName + ".pdf";
                    return new File(sourceDir, pdfName);
                } catch (IOException | InterruptedException e) {
                    super.failed();
                    return null;
                }
            }
        };
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
            String yamlEntry = String.format("%s: %s", optionName, optionValue);
            writer.println(yamlEntry);
        }

        writer.println("indent: yes");
        writer.println("geometry: margin=1in");
        writer.println("mainfontoptions: Scale=1");
//        writer.println("subparagraph: yes");
//        writer.println("header-includes:");
//        writer.println("\t- \\usepackage[compact]{titlesec}");
//        writer.println("\t- \\defaultfontfeatures{Scale=1}");

        writer.print("...");
        writer.close();
    }

    public void setFontSize(String fontSize) {
        options.put("fontsize", fontSize);
    }

    public void setFont(String fontName) {
        this.fontName = fontName;
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
