package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for encryptionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="encryptionType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>boolean">
 *       &lt;attribute name="algorithm" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "encryptionType", propOrder = {
        "value"
})
public class EncryptionType {

    @XmlValue
    protected boolean value;
    @XmlAttribute(name = "algorithm")
    @XmlSchemaType(name = "anySimpleType")
    protected String algorithm;

    /**
     * Gets the value of the value property.
     */
    public boolean isValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     */
    public void setValue(boolean value) {
        this.value = value;
    }

    /**
     * Gets the value of the algorithm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the value of the algorithm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }

}
