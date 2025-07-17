package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // 1. Read the request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] requestParts = requestLine.split(" ");
        String httpCommand = requestParts[0];
        String fullUri = requestParts[1];

        // 2. Parse URI segments and parameters
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

        // 3. Headers
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

        // 4. Extra parameters (do not strip quotes!)
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int eq = line.indexOf('=');
            if (eq > 0) {
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1); // DO NOT REMOVE QUOTES
                parameters.put(key, value);
            }
        }

        // 5. Read content until next blank line or end of input
        StringBuilder contentBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            contentBuilder.append(line).append('\n');
        }
        byte[] content = contentBuilder.toString().getBytes();

        return new RequestInfo(
                httpCommand,
                fullUri,
                uriSegments,
                parameters,
                content
        );
    }


	
	// RequestInfo given internal class
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
