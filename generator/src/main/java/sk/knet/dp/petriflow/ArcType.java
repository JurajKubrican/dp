package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for arc_type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="arc_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="regular"/>
 *     &lt;enumeration value="reset"/>
 *     &lt;enumeration value="inhibitor"/>
 *     &lt;enumeration value="read"/>
 *     &lt;enumeration value="variable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "arc_type")
@XmlEnum
public enum ArcType {

    @XmlEnumValue("regular")
    REGULAR("regular"),
    @XmlEnumValue("reset")
    RESET("reset"),
    @XmlEnumValue("inhibitor")
    INHIBITOR("inhibitor"),
    @XmlEnumValue("read")
    READ("read"),
    @XmlEnumValue("variable")
    VARIABLE("variable");
    private final String value;

    ArcType(String v) {
        value = v;
    }

    public static ArcType fromValue(String v) {
        for (ArcType c : ArcType.values()) {
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
