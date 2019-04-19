package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}id"/>
 *         &lt;element ref="{}logic"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "id",
        "logic"
})
@XmlRootElement(name = "dataRef")
public class DataRef {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected Logic logic;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the logic property.
     *
     * @return possible object is
     * {@link Logic }
     */
    public Logic getLogic() {
        return logic;
    }

    /**
     * Sets the value of the logic property.
     *
     * @param value allowed object is
     *              {@link Logic }
     */
    public void setLogic(Logic value) {
        this.logic = value;
    }

}
