import com.npci.UPISim.dto.Ack;
import com.npci.UPISim.exception.UpiValidationException;
import com.npci.UPISim.exception.UpiXmlValidationException;
import com.npci.UPISim.service.TransactionCoordinator;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class UpiExceptionHandler {

    @Autowired
    private TransactionCoordinator coordinator;

    @Autowired
    private UPILogUtil upiLogUtil;

    @ExceptionHandler(UpiValidationException.class)
    public ResponseEntity<String> handleUpiValidation(
            UpiValidationException ex,
            HttpServletRequest request) {

        String uri = request.getRequestURI();
        String txnId = uri.substring(uri.lastIndexOf(":") + 1);

        // mark failed
        coordinator.markEvent(txnId, "ReqPay", "FAILED_VALIDATION");

        Ack nack = new Ack();
        nack.setApi("ReqPay");
        nack.setReqMsgId(txnId);
        nack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        nack.setErr(ex.getMessage());

        String nackXml = XmlUtil.toXml(nack, Ack.class);

        // STORE ACK
        upiLogUtil.logAck("ReqPay", nackXml, txnId);

        coordinator.markEvent(txnId, "ReqPay", "FAILED_VALIDATION");

        return ResponseEntity
                .status(200) // VERY IMPORTANT
                .contentType(MediaType.APPLICATION_XML)
                .body(nackXml);
    }

}



