import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Response {
    private static final String FL = "\r\n";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final Map<Integer, String> statuses = Map.of(200, "200 OK",
            201, "201 Created",
            404, "404 Not Found");
    private String status;
    private CompressType compressType;
    Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public Response(Builder builder) {
        this.status = statuses.get(builder.status);
        this.headers = builder.headers;
        this.body = builder.body;
        this.compressType = builder.compressType;
    }

    public byte[] buildResponse() throws IOException {
        //first line
        String firstLine = String.format("%s %s" + FL, HTTP_VERSION, status);
        byte[] responseBody = body;
        if (compressType == CompressType.GZIP) {
            headers.put("Content-Encoding", "gzip");
            responseBody = gzipCompress();
        }
        if (responseBody != null) {
            headers.put("Content-Length", String.valueOf(responseBody.length));
        }

        //headers
        String headersResponse = this.headers.entrySet().stream()
                .map(entry -> String.format("%s: %s" + FL, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());


        String responseStr = String.join(FL, firstLine, headersResponse, "");
        byte[] response = new byte[responseStr.getBytes().length + body.length];
        ByteBuffer buffer = ByteBuffer.wrap(response);
        buffer.put(responseStr.getBytes());
        buffer.put(body);
        return buffer.array();
    }

    public static class Builder {
        private Integer status;
        private Map<String, String> headers = new HashMap<>();
        private byte[] body;
        private CompressType compressType;

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder compressType(CompressType compressType) {
            this.compressType = compressType;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private byte[] gzipCompress() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
            GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
            gzip.write(body);
            gzip.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            System.out.printf("gzipCompress: error while compressing message %s\n", e);
            throw e;
        }
    }
}
