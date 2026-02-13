package com.npci.UPISim.util;

import com.npci.UPISim.dto.Ack;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AckUtil {

    public static String successAck(String api, String reqMsgId) {
        try {
            Ack ack = new Ack();
            ack.setApi(api);
            ack.setReqMsgId(reqMsgId);
            ack.setTs(ZonedDateTime.now()
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            return XmlUtil.toXml(ack, Ack.class);
        } catch (Exception e) {
            return "<Ack api=\"" + api + "\"/>";
        }
    }

    public static String errorAck(
            String api,
            String reqMsgId,
            String errorCode
    ) {
        try {
            Ack ack = new Ack();
            ack.setApi(api);
            ack.setReqMsgId(reqMsgId);
            ack.setTs(ZonedDateTime.now()
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            ack.setErr(errorCode);

            return XmlUtil.toXml(ack, Ack.class);
        } catch (Exception e) {
            return "<Ack api=\"" + api + "\" err=\"" + errorCode + "\"/>";
        }
    }
}
