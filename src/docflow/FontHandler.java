package docflow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

public class FontHandler {

    private static final String FONT_PATH = "C:\\Users\\David\\Desktop\\DocFlow\\fonts.xml";

    private boolean newFonts = false;
    private ObservableList<Font> fonts;

    public FontHandler() {
        try {
            fonts = FXCollections.observableArrayList();
            List<Font> fontList = readFontData();
            fonts.addAll(fontList);
        } catch (JAXBException e) {
            fonts = null;
        }
    }

    public ObservableList<Font> getFonts() {
        return fonts;
    }

    public void addFont(Font font) {
        fonts.add(font);
        newFonts = true;
    }

    public void updateFonts() {
        if (newFonts) {
            writeFontData();
            newFonts = false;
        }
    }

    public List<Font> readFontData() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(FontListWrapper.class);
        Unmarshaller um = context.createUnmarshaller();

        File fontFile = new File(FONT_PATH);
        FontListWrapper wrapper = (FontListWrapper) um.unmarshal(fontFile);

        return wrapper.getFonts();
    }

    public void writeFontData() {

    }
}
