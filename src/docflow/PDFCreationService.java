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

        StringBuilder pc = new StringBuilder();
        pc.append("pandoc");
        pc.append(" " + sourceFile.getName());
        pc.append(" " + yamlFile.getName());
        pc.append(" " + "-s");
        pc.append(" " + "-o");
        pc.append(" " + texName);
        pc.append(" " + "--pdf-engine=lualatex");
        pc.append(" " + "-V mainfont:\"TeX Gyre Heros\"");
        System.out.println("Command pc: " + pc.toString());

        System.out.println("executing pandoc");
        List<String> pandocArgsList = Arrays.asList(
                "pandoc",
                sourceFile.getName(),
                yamlFile.getName(),
                "-s",
                "-o",
                texName,
                "--pdf-engine=lualatex",
                "-V mainfont:Garamond"
        );

        ArrayList<String> pandocArgs = new ArrayList<>(pandocArgsList);

//        if (fontName != null) {
//            System.out.println("Adding font");
//            String fontThing = String.format("-V mainfont:\"%s\"", fontName);
//            pandocArgs.add(fontThing);
//            System.out.println("Font added");
//            System.out.println(pandocArgs);
//        }

        System.out.println("Font added");

        Runtime.getRuntime().exec(pc.toString(), null, sourceFile.getParentFile()).waitFor();

//        ProcessBuilder pandocProcess = new ProcessBuilder(pc.toString());
//        pandocProcess.directory(sourceFile.getParentFile());
//        pandocProcess.inheritIO();
//        System.out.println(pandocProcess.command());
//        pandocProcess.start().waitFor();
        System.out.println("Pandoc done");

//        yamlFile.delete();

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
