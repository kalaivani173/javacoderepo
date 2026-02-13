package com.npci.UPISim.util;

import com.npci.UPISim.exception.UpiXmlValidationException;
import jakarta.xml.bind.*;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {

    private static final Schema REQPAY_SCHEMA;

    static {
        try {
            SchemaFactory sf =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            REQPAY_SCHEMA = sf.newSchema(new javax.xml.transform.Source[]{
                    new javax.xml.transform.stream.StreamSource(
                            XmlUtil.class.getResourceAsStream("/xsd/UPI-Common.xsd")),
                    new javax.xml.transform.stream.StreamSource(
                            XmlUtil.class.getResourceAsStream("/xsd/UPI-Payment.xsd"))
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to load XSD", e);
        }
    }

    // ---------------- MARSHAL ----------------
    public static <T> String toXml(T obj, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            marshaller.marshal(obj, sw);
            return sw.toString();

        } catch (JAXBException e) {
            throw new RuntimeException("XML_MARSHAL_ERROR:" + e.getMessage(), e);
        }
    }

    // ---------------- UNMARSHAL WITH XSD (ReqPay ONLY) ----------------
    public static <T> T fromXmlWithXsd(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(REQPAY_SCHEMA);

            unmarshaller.setEventHandler(event -> {
                ValidationEventLocator loc = event.getLocator();

                String message =
                        "XSD VALIDATION ERROR\n" +
                                "Severity : " + event.getSeverity() + "\n" +
                                "Message  : " + event.getMessage() + "\n" +
                                "Line     : " + loc.getLineNumber() + "\n" +
                                "Column   : " + loc.getColumnNumber() + "\n" +
                                "Node     : " + loc.getNode() + "\n" +
                                "Object   : " + loc.getObject();

                throw new RuntimeException(message);
            });

            return clazz.cast(unmarshaller.unmarshal(new StringReader(xml)));

        } catch (Exception e) {
            // PRINT FULL STACK TRACE (IMPORTANT)
            e.printStackTrace();
            throw new UpiXmlValidationException("XML_SCHEMA_ERROR", e);
        }
    }




    // ---------------- UNMARSHAL WITHOUT XSD (ReqHbt / RespHbt) ----------------
    public static <T> T fromXmlWithoutXsd(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return clazz.cast(unmarshaller.unmarshal(new StringReader(xml)));
        } catch (Exception e) {
            throw new UpiXmlValidationException("XML_PARSE_ERROR");
        }
    }

}
