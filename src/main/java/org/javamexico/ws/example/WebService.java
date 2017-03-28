package org.javamexico.ws.example;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.javamexico.service.Respuesta;
import org.javamexico.service.Servicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Misma interfaz pero por servicio.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 4:13 PM
 */
@javax.jws.WebService(endpointInterface = "org.javamexico.ws.example.InterfazWS")
public class WebService implements InterfazWS {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Setter
    private Servicio service;

    @Override
    public Respuesta servicio(
            final int id, final String user, final String pass,
            final String prod, final BigDecimal amt, final String cuenta,
            final Date fecha) {
        Future<Respuesta> resp = service.atender(id, user, pass, prod, amt, cuenta, fecha);
        try {
            return resp.get();
        } catch (InterruptedException|ExecutionException ex) {
            log.error("Esperando respuesta", ex);
            Respuesta r = new Respuesta();
            r.setRcode(99);
            r.setMensaje("Error esperando respuesta: " + ex.getMessage());
            return r;
        }
    }
}
