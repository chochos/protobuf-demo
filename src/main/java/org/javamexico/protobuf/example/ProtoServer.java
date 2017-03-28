package org.javamexico.protobuf.example;

import lombok.RequiredArgsConstructor;
import org.javamexico.service.Servicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor de protobuf usando esquema tradicional de hilo-por-conexión.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 11:55 AM
 */
@RequiredArgsConstructor
public class ProtoServer implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Servicio servicio;
    private ServerSocket server;

    public void run() {
        try {
            //Escuchar en puerto 9999
            server = new ServerSocket(9999);
            int count = 0;
            log.info("Proto server escuchando en puerto 9999");
            //Recibir conexiones
            while (server.isBound()) {
                Socket sock = server.accept();
                log.info("Proto nueva conexion");
                new Thread(new Conexion(sock, servicio), "Incoming-"+(++count)).start();
            }
        } catch (IOException ex) {
            log.error("Servidor interrumpido! Terminando");
        }
    }

    public void shutdown() throws IOException {
        if (server != null) {
            server.close();
        }
    }
}
