package org.javamexico.protobuf.example;

import com.codahale.metrics.Timer;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * Cliente asíncrono de protobuf.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 12:59 PM
 */
public class ProtoClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private BufferedOutputStream bout;
    private BufferedInputStream bin;
    private volatile int pending;
    private final Timer.Context[] timers = new Timer.Context[Main.ROUNDS];

    public void benchmark(final int rounds) {
        final Random rng = new Random(System.currentTimeMillis());
        Socket sock;
        try {
            sock = new Socket("localhost", 9999);
            bout = new BufferedOutputStream(sock.getOutputStream());
            bin = new BufferedInputStream(sock.getInputStream());
        } catch (IOException ex) {
            log.error("ProtoBench no pude abrir conexion");
            return;
        }
        Thread reader = new Thread(this::lectura, "ProtoReader");
        reader.start();
        final byte[] hlen = new byte[2];
        pending = rounds;
        Timer timer = Main.METRICS.timer("proto");
        try {
            for (int i = 0; i < rounds; i++) {
                Protos.Peticion req = Protos.Peticion.newBuilder().setId(i)
                        .setUser("usuario").setPassword("password")
                        .setAccount("P"+rng.nextInt()).setAmount(rng.nextFloat())
                        .setProduct("Something")
                        .build();
                timers[i] = timer.time();
                Utils.write(req, bout, hlen);
            }
            while (reader.isAlive()) {
                Thread.sleep(100);
            }
            sock.close();
        } catch (IOException | InterruptedException ex) {
            log.error("ProtoBench enviando peticiones", ex);
        }
        log.info("Fin.");
    }

    private void lectura() {
        final byte[] buf = new byte[65536];
        Parser<Protos.Respuesta> parser = Protos.Respuesta.parser();
        try {
            while (pending-- > 0) {
                Protos.Respuesta resp = Utils.read(buf, bin, parser);
                if (resp.getRcode() == 0) {
                    log.debug("OK conf {}", resp.getConfirmacion());
                } else {
                    log.debug("ERR {}", resp.getError());
                }
                if (timers[resp.getId()] != null) {
                    timers[resp.getId()].stop();
                }
            }
        } catch (IOException ex) {
            log.error("Leyendo de socket", ex);
        }
    }

    public void writeToDisk() {
        try (FileOutputStream fout = new FileOutputStream("/tmp/protobufdemo.bin")) {
            Protos.Peticion.newBuilder().setId(1234).setUser("username")
                    .setPassword("password").setProduct("product").setAmount(12.34f)
                    .setAccount("12345678").setDate(System.currentTimeMillis())
                    .build().writeTo(fout);
        } catch (IOException ex) {
            log.error("No se pudo escribir a disco");
        }
    }
}
