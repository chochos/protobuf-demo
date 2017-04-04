package org.javamexico.protobuf.example;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.javamexico.service.Servicio;
import org.javamexico.ws.example.ClienteWS;
import org.javamexico.ws.example.WebService;

import javax.xml.ws.Endpoint;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.concurrent.TimeUnit;

/**
 * Clase principal para correr la demo.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 11:54 AM
 */
public class Main {

    private final static Servicio servicio = new Servicio();
    public static final MetricRegistry METRICS = new MetricRegistry();
    public static final int ROUNDS = 50000;

    private static void externalTest() throws Exception {
        ProtoServer server = new ProtoServer(servicio);
        Thread t = new Thread(server, "proto-server");
        t.start();
        System.out.println("LISTO; dale ENTER para terminar");
        synchronized(servicio) {
            servicio.wait();
        }
    }

    private static void proto() throws Exception {
        ProtoServer server = new ProtoServer(servicio);
        Thread t = new Thread(server, "proto-server");
        t.start();
        //Crear un cliente y enviar N peticiones
        final ProtoClient pc = new ProtoClient();
        pc.writeToDisk();
        pc.benchmark(1000);
        METRICS.timer("proto.total").time(() -> pc.benchmark(Main.ROUNDS));
        server.shutdown();
    }

    private static void ws() throws Exception {
        WebService ws = new WebService();
        ws.setService(servicio);
        Endpoint endpoint = Endpoint.create(ws);
        endpoint.publish("http://localhost:9998/ws");
        ClienteWS wsc = new ClienteWS();
        System.out.println("WS warmup");
        wsc.benchmark(1000);
        System.out.println("WS benchmark");
        METRICS.timer("ws.total").time(() -> wsc.benchmark(Main.ROUNDS));
    }

    public static void main(String... args) throws Exception {
        ws();
        proto();
        ConsoleReporter.forRegistry(Main.METRICS).convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS).outputTo(System.out).build().report();
        System.out.println("FYI, " + Runtime.getRuntime().availableProcessors() + " CPUs");
        externalTest();
        System.exit(0);
    }

}
