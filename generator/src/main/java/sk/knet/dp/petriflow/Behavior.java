package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for behavior.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="behavior">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="forbidden"/>
 *     &lt;enumeration value="hidden"/>
 *     &lt;enumeration value="visible"/>
 *     &lt;enumeration value="editable"/>
 *     &lt;enumeration value="required"/>
 *     &lt;enumeration value="immediate"/>
 *     &lt;enumeration value="optional"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "behavior")
@XmlEnum
public enum Behavior {

    @XmlEnumValue("forbidden")
    FORBIDDEN("forbidden"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("visible")
    VISIBLE("visible"),
    @XmlEnumValue("editable")
    EDITABLE("editable"),
    @XmlEnumValue("required")
    REQUIRED("required"),
    @XmlEnumValue("immediate")
    IMMEDIATE("immediate"),
    @XmlEnumValue("optional")
    OPTIONAL("optional");
    private final String value;

    Behavior(String v) {
        value = v;
    }

    public static Behavior fromValue(String v) {
        for (Behavior c : Behavior.values()) {
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
