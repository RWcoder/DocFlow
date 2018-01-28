package docflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="fonts")
public class FontListWrapper {

    private List<Font> fonts;

    @XmlElement(name="font")
    public List<Font> getFonts() {
        return fonts;
    }

    public void setFonts(List<Font> fonts) {
        this.fonts = fonts;
    }
}
