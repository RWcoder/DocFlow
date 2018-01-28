package docflow;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PDFCreationService extends Service<File> {

    private Font font;
    private HashMap<String, String> options;
    private String sourceName;
    private File sourceFile;
    private File headerFile;
    private boolean keepTeX = false;

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

                    writeHeaderFile();


                    super.updateMessage("Converting to TeX...");

                    String texName = sourceName + ".tex";

                    System.out.println("executing pandoc");
                    List<String> pandocArgsList = Arrays.asList(
                            "pandoc",
                            "-f markdown-latex_macros",
                            headerFile.getName(),
                            sourceFile.getName(),
                            "-s",
                            "-o",
                            texName
                    );

                    ArrayList<String> pandocArgs = new ArrayList<>(pandocArgsList);

                    File sourceDir = sourceFile.getParentFile();
                    String pandocCmd = String.join(" ", pandocArgs);
                    Runtime.getRuntime().exec(pandocCmd, null, sourceDir).waitFor();
                    System.out.println("Pandoc done");

                    super.updateMessage("Compiling PDF...");

                    ProcessBuilder latexProcess = new ProcessBuilder("pdflatex", texName);
                    latexProcess.directory(sourceFile.getParentFile());
                    latexProcess.inheritIO();
                    System.out.println(latexProcess.command());
                    latexProcess.start().waitFor();

                    String pdfName = sourceName + ".pdf";

                    if (!cleanUp()) {
                        super.failed();
                        return null;
                    }

                    return new File(sourceDir, pdfName);
                } catch (IOException | InterruptedException e) {
                    super.failed();
                    return null;
                }
            }

            private boolean cleanUp() {
//                boolean headerDeleted = headerFile.delete();
                boolean headerDeleted = true;

                File dir = sourceFile.getParentFile();
                File[] junk = {
                        new File(dir, sourceName + ".aux"),
                        new File(dir, sourceName + ".log"),
                        new File(dir, sourceName + ".out")
                };

                boolean junkDeleted = true;
                for (File f : junk) {
                    junkDeleted &= f.delete();
                }

                if (!keepTeX) {
                    File texFile = new File(dir, sourceName + ".tex");
                    junkDeleted &= texFile.delete();
                }

                return headerDeleted && junkDeleted;
            }
        };
    }

    private boolean createYAMLFile() throws IOException {
        File sourceDirectory = sourceFile.getParentFile();
        String yamlFileName = sourceName + ".txt";
        headerFile = new File(sourceDirectory, yamlFileName);
        return headerFile.createNewFile();
    }

    private void writeHeaderFile() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(headerFile);
        writer.println("---");

        for (Map.Entry<String, String> option : options.entrySet()) {
            String optionName = option.getKey();
            String optionValue = option.getValue();
            String yamlEntry = String.format("%s: %s", optionName, optionValue);
            writer.println(yamlEntry);
        }

        writer.println("indent: yes");
        writer.println("geometry: margin=1in");
//        writer.println("mainfontoptions: Scale=1");
        writer.println("header-includes:");
        writer.println("\t- \\usepackage{titling}");

        if (font != null) {
            writer.println("\t- " + font.getUsage());
        }

        writer.println("...");

        writer.println("\\renewcommand{\\t}[1]{\\title{#1} \\date{\\today} \\author{David Thomson} \\maketitle}");
        writer.println("\\newcommand{\\tna}[1]{\\title{#1} \\preauthor{} \\author{} \\postauthor{} \\date{\\today} \\maketitle}");
        writer.println("\\newcommand{\\tnd}[1]{\\title{#1} \\author{David Thomson} \\predate{} \\date{} \\postdate{} \\maketitle}");
        writer.println("\\newcommand{\\tnand}[1]{\\title{#1} \\preauthor{} \\author{} \\postauthor{} \\predate{} \\date{} \\postdate{} \\maketitle}");
//        writer.println("\\newcommand{\\sansfont}{\\renewcommand*\\familydefault{\\sfdefault}}");
//        writer.println("\\sansfont");

        writer.close();
    }

    public void setKeepTeX(boolean keepTeX) {
        this.keepTeX = keepTeX;
    }

    public void setFontSize(String fontSize) {
        options.put("fontsize", fontSize);
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void clearFont() {
        this.font = null;
    }

    public void setTitlePage(boolean titlePage) {
        if (titlePage) {
            options.put("classoption", "titlepage");
        } else if (options.containsKey("classoption")) {
            options.remove("classoption");
        }
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
