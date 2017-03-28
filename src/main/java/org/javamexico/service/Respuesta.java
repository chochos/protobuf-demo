package org.javamexico.service;

import lombok.Data;

/**
 * Respuesta a una petición.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 12:15 PM
 */
@Data
public class Respuesta {

    private int rcode;
    private String mensaje;
    private int confirmacion;
}
