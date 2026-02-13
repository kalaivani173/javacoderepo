package com.payee.psp.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small JAXB helper with a convenience overload and simple JAXBContext caching.
 */
public final class XmlUtils {

    private XmlUtils() {}

    // Simple cache for JAXBContext per class to reduce overhead
    private static final Map<Class<?>, JAXBContext> CONTEXT_CACHE = new ConcurrentHashMap<>();

    /**
     * Marshal object to XML using explicit class.
     */
    public static <T> String toXml(T obj, Class<T> clazz) throws JAXBException {
        if (obj == null) return null;
        JAXBContext context = jaxbContextFor(clazz);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter sw = new StringWriter();
        marshaller.marshal(obj, sw);
        return sw.toString();
    }

    /**
     * Convenience overload: infer class from instance (calls typed variant).
     */
    public static String toXml(Object obj) throws JAXBException {
        if (obj == null) return null;
        @SuppressWarnings("unchecked")
        Class<Object> clazz = (Class<Object>) obj.getClass();
        return toXml(obj, clazz);
    }

    /**
     * Unmarshal XML string to the given class.
     */
    public static <T> T fromXml(String xml, Class<T> clazz) throws JAXBException {
        if (xml == null || xml.isBlank()) return null;
        JAXBContext context = jaxbContextFor(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        Object result = unmarshaller.unmarshal(reader);
        return clazz.cast(result);
    }

    private static JAXBContext jaxbContextFor(Class<?> clazz) throws JAXBException {
        // computeIfAbsent lambda cannot throw checked exception, so handle explicitly
        JAXBContext ctx = CONTEXT_CACHE.get(clazz);
        if (ctx != null) return ctx;
        JAXBContext created = JAXBContext.newInstance(clazz);
        JAXBContext prev = CONTEXT_CACHE.putIfAbsent(clazz, created);
        return prev == null ? created : prev;
    }
}
