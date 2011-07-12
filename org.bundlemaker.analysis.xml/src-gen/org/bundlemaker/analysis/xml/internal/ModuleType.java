//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.07.11 at 08:54:48 PM MESZ 
//


package org.bundlemaker.analysis.xml.internal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moduleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moduleType">
 *   &lt;complexContent>
 *     &lt;extension base="{}abstractArtifactType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="package" type="{}packageType"/>
 *         &lt;element name="resource" type="{}resourceType"/>
 *         &lt;element name="type" type="{}typeType"/>
 *       &lt;/choice>
 *       &lt;anyAttribute/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moduleType", propOrder = {
    "packageOrResourceOrType"
})
public class ModuleType
    extends AbstractArtifactType
{

    @XmlElements({
        @XmlElement(name = "package", type = PackageType.class),
        @XmlElement(name = "resource", type = ResourceType.class),
        @XmlElement(name = "type", type = TypeType.class)
    })
    protected List<AbstractArtifactType> packageOrResourceOrType;

    /**
     * Gets the value of the packageOrResourceOrType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the packageOrResourceOrType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPackageOrResourceOrType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PackageType }
     * {@link ResourceType }
     * {@link TypeType }
     * 
     * 
     */
    public List<AbstractArtifactType> getPackageOrResourceOrType() {
        if (packageOrResourceOrType == null) {
            packageOrResourceOrType = new ArrayList<AbstractArtifactType>();
        }
        return this.packageOrResourceOrType;
    }

}
