package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataGroupAlignment.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dataGroupAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="start"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="end"/>
 *     &lt;enumeration value="left"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "dataGroupAlignment")
@XmlEnum
public enum DataGroupAlignment {

    @XmlEnumValue("start")
    START("start"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("end")
    END("end"),
    @XmlEnumValue("left")
    LEFT("left");
    private final String value;

    DataGroupAlignment(String v) {
        value = v;
    }

    public static DataGroupAlignment fromValue(String v) {
        for (DataGroupAlignment c : DataGroupAlignment.values()) {
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
