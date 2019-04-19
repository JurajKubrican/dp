package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataFocusPolicyType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dataFocusPolicyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="manual"/>
 *     &lt;enumeration value="auto_empty_required"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "dataFocusPolicyType")
@XmlEnum
public enum DataFocusPolicyType {

    @XmlEnumValue("manual")
    MANUAL("manual"),
    @XmlEnumValue("auto_empty_required")
    AUTO_EMPTY_REQUIRED("auto_empty_required");
    private final String value;

    DataFocusPolicyType(String v) {
        value = v;
    }

    public static DataFocusPolicyType fromValue(String v) {
        for (DataFocusPolicyType c : DataFocusPolicyType.values()) {
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
