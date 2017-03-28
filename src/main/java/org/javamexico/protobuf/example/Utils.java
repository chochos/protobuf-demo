package org.javamexico.protobuf.example;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Escritura de un mensaje a un stream.
 *
 * @author Enrique Zamudio
 *         Date: 3/21/17 9:40 PM
 */
public class Utils {

    public static void write(GeneratedMessageV3 msg, OutputStream out, byte[] hlen)
            throws IOException {
        final int tam = msg.getSerializedSize();
        hlen[0] = (byte)((tam & 0xff00) >> 8);
        hlen[1] = (byte)(tam & 0xff);
        out.write(hlen);
        msg.writeTo(out);
        out.flush();
    }

    public static <Proto> Proto read(byte[] buf, InputStream stream, Parser<Proto> parser)
            throws IOException {
        int leidos = stream.read(buf, 0, 2);
        if (leidos == 2) {
            final int tam = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
            leidos = stream.read(buf, 0, tam);
            if (leidos > 0) {
                return parser.parseFrom(buf, 0, leidos);
            }
        }
        return null;
    }
}
