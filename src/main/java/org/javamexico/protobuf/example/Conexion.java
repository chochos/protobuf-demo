package org.javamexico.protobuf.example;

import com.google.protobuf.Parser;
import lombok.RequiredArgsConstructor;
import org.javamexico.service.Respuesta;
import org.javamexico.service.Servicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Conexión que usa dos hilos para comunicación asíncrona.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 11:55 AM
 */
@RequiredArgsConstructor
public class Conexion implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Socket socket;
    private final Servicio servicio;
    /** La escritura se hará en tareas en este hilo. */
    private final Executor writer = Executors.newSingleThreadExecutor();
    /** Las peticiones se van a despachar en este thread pool. */
    private final ExecutorService worker = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
    private BufferedOutputStream bout;
    private final byte[] hlen = new byte[2];

    public void run() {
        //Esto es el hilo de lectura
        final byte[] hlen = new byte[2];
        final byte[] buf = new byte[65536];
        final Parser<Protos.Peticion> parser = Protos.Peticion.parser();
        try {
            final InputStream ins = new BufferedInputStream(socket.getInputStream());
            bout = new BufferedOutputStream(socket.getOutputStream());
            while (true) {
                final Protos.Peticion req = Utils.read(buf, ins, parser);
                if (req == null) {
                    log.warn("No se leyo peticion");
                    return;
                }
                worker.execute(()->execute(req));
            }
        } catch (IOException ex) {
            log.error("Cerrando conexion", ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                log.error("No puedo ni cerrar");
            }
        }
    }

    private void execute(Protos.Peticion req) {
        try {
            Respuesta r = servicio.atender(req.getId(), req.getUser(), req.getPassword(),
                    req.getProduct(), new BigDecimal(req.getAmount()), req.getAccount(),
                    new Date(req.getDate())).get();
            Protos.Respuesta.Builder resp = Protos.Respuesta.newBuilder().setRcode(r.getRcode())
                    .setId(req.getId());
            if (r.getRcode() == 0) {
                resp.setConfirmacion(r.getConfirmacion());
            } else {
                resp.setError(r.getMensaje());
            }
            writer.execute(()-> write(resp.build()));
        } catch (InterruptedException | ExecutionException ex) {
            log.error("Despachando peticion {}", req, ex);
        }
    }

    private void write(Protos.Respuesta resp) {
        try {
            Utils.write(resp, bout, hlen);
        } catch (IOException ex) {
            log.error("Escribiendo respuesta {}", resp, ex);
        }
    }
}
