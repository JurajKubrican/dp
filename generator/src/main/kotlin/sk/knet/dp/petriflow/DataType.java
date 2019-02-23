
package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for data_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="data_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="number"/>
 *     &lt;enumeration value="text"/>
 *     &lt;enumeration value="enumeration"/>
 *     &lt;enumeration value="multichoice"/>
 *     &lt;enumeration value="boolean"/>
 *     &lt;enumeration value="date"/>
 *     &lt;enumeration value="file"/>
 *     &lt;enumeration value="user"/>
 *     &lt;enumeration value="caseref"/>
 *     &lt;enumeration value="dateTime"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "data_type")
@XmlEnum
public enum DataType {

    @XmlEnumValue("number")
    NUMBER("number"),
    @XmlEnumValue("text")
    TEXT("text"),
    @XmlEnumValue("enumeration")
    ENUMERATION("enumeration"),
    @XmlEnumValue("multichoice")
    MULTICHOICE("multichoice"),
    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("file")
    FILE("file"),
    @XmlEnumValue("user")
    USER("user"),
    @XmlEnumValue("caseref")
    CASEREF("caseref"),
    @XmlEnumValue("dateTime")
    DATE_TIME("dateTime");
    private final String value;

    DataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataType fromValue(String v) {
        for (DataType c: DataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
