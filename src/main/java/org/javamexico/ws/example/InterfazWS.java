package org.javamexico.ws.example;

import org.javamexico.service.Respuesta;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Interfaz del WS.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 6:59 PM
 */
@javax.jws.WebService
@SOAPBinding(parameterStyle= SOAPBinding.ParameterStyle.WRAPPED)
public interface InterfazWS {

    @WebResult(name="resp")
    Respuesta servicio(
            @WebParam(name="id") int id, @WebParam(name="user") String user,
            @WebParam(name="password") String pass,
            @WebParam(name="product") String prod,
            @WebParam(name="amount") BigDecimal amt,
            @WebParam(name="account") String cuenta,
            @WebParam(name="date") Date fecha);
}
