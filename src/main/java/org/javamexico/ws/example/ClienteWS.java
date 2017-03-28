package org.javamexico.ws.example;

import com.codahale.metrics.Timer;
import org.javamexico.protobuf.example.Main;
import org.javamexico.service.Respuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Blabla.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 6:57 PM
 */
public class ClienteWS {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private InterfazWS ws;
    private final AtomicInteger pending = new AtomicInteger();
    /** Las peticiones se van a enviar con este thread pool. */
    private final ExecutorService worker = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    public void benchmark(final int rounds) {
        URL url;
        try {
            url = new URL("http://localhost:9998/ws?wsdl");
        } catch (MalformedURLException ex) {
            return;
        }
        Service service = Service.create(url,
            new QName("http://example.ws.javamexico.org/", "WebServiceService"));
        ws = service.getPort(new QName("http://example.ws.javamexico.org/",
        "WebServicePort"), InterfazWS.class);
        Timer timer = Main.METRICS.timer("ws");
        pending.set(rounds);
        for (int i = 0; i < rounds; i++) {
            final int c = i;
            worker.execute(() -> timer.time(() -> invocar(c)));
        }
        try {
            while (pending.get() > 0) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            //whatever
        }
    }

    private void invocar(int id) {
        Respuesta r = ws.servicio(id, "usuario", "pass", "Prod",
                new BigDecimal(0), "cuenta", new Date());
        if (r.getRcode() == 0) {
            log.debug("OK conf {}", r.getConfirmacion());
        } else {
            log.debug("ERR {}", r.getMensaje());
        }
        pending.decrementAndGet();
    }

}
