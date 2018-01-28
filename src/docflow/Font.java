package docflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="font")
public class Font {

    private String name;
    private String usage;

    public Font() {
        this.name = "default";
        this.usage = "default";
    }

    public Font(String name, String usage) {
        this.name = name;
        this.usage = usage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    @XmlElement
    public String getUsage() {
        return usage;
    }

    @Override
    public String toString() {
        return name;
    }
}
