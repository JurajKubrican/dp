
package sk.knet.dp.petriflow;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="exact" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="delay" type="{http://www.w3.org/2001/XMLSchema}duration"/>
 *       &lt;/choice>
 *       &lt;attribute name="type" type="{}triggerType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "exact",
    "delay"
})
@XmlRootElement(name = "trigger")
public class Trigger {

    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar exact;
    protected Duration delay;
    @XmlAttribute(name = "type")
    protected TriggerType type;

    /**
     * Gets the value of the exact property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExact() {
        return exact;
    }

    /**
     * Sets the value of the exact property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExact(XMLGregorianCalendar value) {
        this.exact = value;
    }

    /**
     * Gets the value of the delay property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Sets the value of the delay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setDelay(Duration value) {
        this.delay = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TriggerType }
     *     
     */
    public TriggerType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TriggerType }
     *     
     */
    public void setType(TriggerType value) {
        this.type = value;
    }

}
