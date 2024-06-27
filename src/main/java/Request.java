import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
    private final Pattern headerPattern = Pattern.compile("([\\w-]+): (.*)");
    private BufferedReader in;
    private String method;
    private String uri;
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;

    public static Request buildResponse(Socket clientSocket) throws IOException {
        Request request = new Request(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
        request.parseRequest();
        return request;
    }

    private Request(BufferedReader in) {
        this.in = in;
    }

    private void parseRequest() throws IOException {
        String reqLine = in.readLine();
        uri = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();
        method = reqLine.substring(0, reqLine.indexOf(' '));
        parseHeaders();
        parseBody();
    }

    private void parseBody() throws IOException {
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[contentLength];
            int bytesRead = in.read(buffer, 0, contentLength);
            if (bytesRead != -1) {
                body = String.valueOf(buffer);
            }
        }
    }

    private void parseHeaders() throws IOException {
        Matcher headerMatcher;
        String line = in.readLine();
        while (line != null && !line.isBlank()) {
            headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches()) {
                headers.put(headerMatcher.group(1), headerMatcher.group(2));
            }
            line = in.readLine();
        }
    }

    public Pattern getHeaderPattern() {
        return headerPattern;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
