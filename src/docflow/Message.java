package docflow;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Message {
    /**
     * The sender.
     */
    private String from;
    /**
     * The receiver.
     */
    private String to;
    /**
     * A unique identification.
     */
    private int id;
    /**
     * The actual message.
     */
    private String text;
    /**
     * Default contstructor.
     */
    public Message() {
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getId() {
        return id;
    }
    @XmlAttribute
    public void setId(int id) {
        this.id = id;
    }
    public String toString() {
        return String.format("Message[id=%s, from=%s, to=%s, text=%s]", id, from, to, text);
    }
}