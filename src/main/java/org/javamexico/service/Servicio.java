package org.javamexico.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Un servicio de demo, bastante simple.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 12:15 PM
 */
public class Servicio {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final AtomicInteger conf = new AtomicInteger((int)(System.currentTimeMillis()%100000));
    private final String[] errores = new String[]{
            "No hay luz", "El perro se comio el hilo", "Ya no hay de bistec",
            "Hoy no circula", "Se me perdio mi pasaporte"
    };
    private final Random rng = new Random(System.currentTimeMillis());

    public Future<Respuesta> atender(final int id, final String user, final String pass,
                                     final String prod, final BigDecimal amt, final String cuenta,
                                     final Date fecha) {
        //Crear un Callable que va a generar la respuesta
        final Futuro futuro = new Futuro(id, user, pass, prod, amt, cuenta, fecha);
        //Ejecutarlo en un threadpool, para devolver un Future
        return threadPool.submit(futuro);
    }

    @RequiredArgsConstructor
    private class Futuro implements Callable<Respuesta> {
        final int id;
        final String user;
        final String pass;
        final String prod;
        final BigDecimal amt;
        final String cuenta;
        final Date fecha;
        private volatile Respuesta resp;

        @Override
        public Respuesta call() throws Exception {
            final Respuesta r = new Respuesta();
            final long t = System.currentTimeMillis();
            r.setRcode(t%4==0 ? 5-(int)(t%4) : 0);
            if (r.getRcode() == 0) {
                r.setConfirmacion(conf.incrementAndGet());
            } else {
                r.setMensaje(errores[rng.nextInt(errores.length)]);
            }
            resp = r;
            return resp;
        }
    }

}
