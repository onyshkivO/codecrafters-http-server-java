import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Pattern headerPattern = Pattern.compile("([\\w-]+): (.*)");
    private String reqLine = null;
    private Map<String, String> headers = new HashMap<>();
    private String reqBody = null;

    public Connection(ServerSocket socket) throws IOException {
        clientSocket = socket.accept();
    }

    private void parseRequest() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        reqLine = in.readLine();
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

    public void processRequest() throws IOException {
        parseRequest();
        process();
    }

    private void process() {
        String path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();
        if ("/".equals(path)) {
            out.print("HTTP/1.1 200 OK\r\n\r\n");
        } else if (path.startsWith("/echo")) {
            String message = path.split("/")[2];
            out.print("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                    message.length() + "\r\n\r\n" + message);
        } else if (path.startsWith("/user-agent")) {
            String userAgent = headers.get("User-Agent");
            String format = String.format("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: %d\r\n\r\n%s", userAgent.length(), userAgent);
            out.print(format);
        } else {
            out.print("HTTP/1.1 404 Not Found\r\n\r\n");
        }
        out.flush();
        out.close();
    }

}
