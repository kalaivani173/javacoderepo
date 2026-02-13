package com.payee.psp.util;


import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlPrettyPrinter {

    public static String format(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return "<empty-xml/>";
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            StringReader reader = new StringReader(xml);
            StringWriter writer = new StringWriter();
            transformer.transform(new StreamSource(reader), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            return "⚠️ [XmlPrettyPrinter] Failed to format XML: " + e.getMessage() + "\n" + xml;
        }
    }
}
