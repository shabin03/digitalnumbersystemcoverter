import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public class ConverterServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/convert", ConverterServer::handleConvert);
        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:" + PORT);
        System.out.println("Try: http://localhost:" + PORT + "/convert?value=1011&base=2");
    }

    private static void handleConvert(HttpExchange ex) throws IOException {
        // Allow the website (index.html) to call this API
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            send(ex, 204, "");
            return;
        }
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            send(ex, 405, error("Only GET is allowed"));
            return;
        }

        Map<String, String> params = parseQuery(ex.getRequestURI().getRawQuery());
        String value = params.get("value");
        String baseText = params.get("base");

        if (value == null || value.isBlank() || baseText == null) {
            send(ex, 400, error("Both 'value' and 'base' are required"));
            return;
        }

        int base;
        try {
            base = Integer.parseInt(baseText.trim());
        } catch (NumberFormatException e) {
            send(ex, 400, error("'base' must be 2, 8, 10 or 16"));
            return;
        }
        if (base != 2 && base != 8 && base != 10 && base != 16) {
            send(ex, 400, error("'base' must be 2, 8, 10 or 16"));
            return;
        }

        value = value.trim();
        for (char c : value.toCharArray()) {
            if (Character.digit(c, base) == -1) {
                send(ex, 400, error("'" + value + "' is not a valid base " + base + " number"));
                return;
            }
        }

        BigInteger number = new BigInteger(value, base);
        String json = "{"
                + "\"input\":\"" + value + "\","
                + "\"base\":" + base + ","
                + "\"binary\":\"" + number.toString(2) + "\","
                + "\"octal\":\"" + number.toString(8) + "\","
                + "\"decimal\":\"" + number.toString(10) + "\","
                + "\"hexadecimal\":\"" + number.toString(16).toUpperCase() + "\""
                + "}";
        send(ex, 200, json);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    private static String error(String message) {
        return "{\"error\":\"" + message.replace("\"", "'") + "\"}";
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
        ex.close();
    }
}