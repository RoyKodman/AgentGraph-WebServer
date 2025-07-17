package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

public static RequestInfo parseRequest(BufferedReader reader, InputStream in) throws IOException {
    // 1. Read request line
    String requestLine = reader.readLine();
    if (requestLine == null || requestLine.isEmpty()) {
        return null;
    }
    String[] requestParts = requestLine.split(" ");
    String httpCommand = requestParts[0];
    String fullUri = requestParts[1];

    // Parse URI
    String uri;
    Map<String, String> parameters = new HashMap<>();
    int idx = fullUri.indexOf('?');
    if (idx >= 0) {
        uri = fullUri.substring(0, idx);
        String query = fullUri.substring(idx + 1);
        for (String param : query.split("&")) {
            if (param.isEmpty()) continue;
            String[] pair = param.split("=", 2);
            String key = pair[0];
            String value = pair.length > 1 ? pair[1] : "";
            parameters.put(key, value);
        }
    } else {
        uri = fullUri;
    }
    String[] uriSegments = Arrays.stream(uri.split("/"))
            .filter(segment -> !segment.isEmpty())
            .toArray(String[]::new);

    // Headers
    Map<String, String> headers = new HashMap<>();
    String line;
    int contentLength = 0;
    while ((line = reader.readLine()) != null && !line.isEmpty()) {
        int sep = line.indexOf(':');
        if (sep > 0) {
            String key = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();
            headers.put(key, value);
            if (key.equalsIgnoreCase("Content-Length")) {
                try {
                    contentLength = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    contentLength = 0;
                }
            }
        }
    }

    // *** STOP HERE for GET *** (unless you know there are more parameters in the body!)
    // If not GET, try to read extra parameters from the body (e.g., after headers, like filename="..." etc):
    Map<String, String> extraParams = new HashMap<>();
    if (!"GET".equals(httpCommand)) {
        reader.mark(4096); // Mark the current position in the reader
        String peek = reader.readLine();
        if (peek != null && peek.contains("=")) {
            do {
                int eq = peek.indexOf('=');
                if (eq > 0) {
                    String key = peek.substring(0, eq).trim();
                    String value = peek.substring(eq + 1);
                    extraParams.put(key, value);
                }
                peek = reader.readLine();
            } while (peek != null && !peek.isEmpty());
        } else {
            reader.reset();
        }
    }
    parameters.putAll(extraParams);

    // Content/body (read if exists)
    byte[] content = new byte[0];
    if (contentLength > 0) {
        content = new byte[contentLength];
        int read = 0;
        while (read < contentLength) {
            int n = in.read(content, read, contentLength - read);
            if (n == -1) break;
            read += n;
        }
    }

    return new RequestInfo(
            httpCommand,
            fullUri,
            uriSegments,
            parameters,
            content
    );
    }



    // RequestInfo inner class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
