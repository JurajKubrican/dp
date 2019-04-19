package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eventType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="eventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="assign"/>
 *     &lt;enumeration value="cancel"/>
 *     &lt;enumeration value="finish"/>
 *     &lt;enumeration value="delegate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "eventType")
@XmlEnum
public enum EventType {

    @XmlEnumValue("assign")
    ASSIGN("assign"),
    @XmlEnumValue("cancel")
    CANCEL("cancel"),
    @XmlEnumValue("finish")
    FINISH("finish"),
    @XmlEnumValue("delegate")
    DELEGATE("delegate");
    private final String value;

    EventType(String v) {
        value = v;
    }

    public static EventType fromValue(String v) {
        for (EventType c : EventType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

}
