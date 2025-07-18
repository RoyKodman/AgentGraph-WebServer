package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(InputStream in) throws IOException {
        // 1. Read request line
        String requestLine = readLine(in);
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
        while ((line = readLine(in)) != null && !line.isEmpty()) {
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

        // Body (raw bytes)
        byte[] content = new byte[0];
        if (contentLength > 0) {
            content = new byte[contentLength];
            int read = 0;
            while (read < contentLength) {
                int n = in.read(content, read, contentLength - read);
                if (n == -1) break;
                read += n;
            }
            System.out.println("DEBUG: RequestParser read " + read + " bytes (should match Content-Length: " + contentLength + ")");
            if (read > 0) {
                System.out.println("DEBUG: First 80 chars: " + new String(content, 0, Math.min(80, read), java.nio.charset.StandardCharsets.ISO_8859_1));
            }
        }

        return new RequestInfo(
                httpCommand,
                fullUri,
                uriSegments,
                parameters,
                headers,
                content
        );
    }

    // Reads a line from InputStream, terminated by \r\n, returns ISO-8859-1 String.
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = 0, curr;
        while ((curr = in.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                break;
            }
            if (prev != 0) baos.write(prev);
            prev = curr;
        }
        return baos.size() == 0 && curr == -1 ? null : baos.toString("ISO-8859-1");
    }

    // --- Your inner class stays the same ---
    public static class RequestInfo {
        private final Map<String, String> headers;
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, Map<String, String> headers, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
            this.headers = headers;
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

        public Map<String, String> getHeaders() { return headers; }
        public String getHeader(String key) { return headers.get(key); }
    }
}
