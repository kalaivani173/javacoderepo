package com.payee.psp;



import com.payee.psp.dto.ReqAuthDetails;
import jakarta.xml.bind.JAXBContext;

public class TestJaxb {
    public static void main(String[] args) throws Exception {
        JAXBContext.newInstance(ReqAuthDetails.class);
        System.out.println("JAXBContext created successfully!");
    }
}