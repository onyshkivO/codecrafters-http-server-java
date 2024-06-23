import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static ServerSocket serverSocket;
    static Socket clientSocket;
    static PrintWriter out;
    static BufferedReader in;
    static Pattern headerPattern = Pattern.compile("([\\w-]+): (.*)");
    static String reqLine = null;
    static Map<String, String> headers = new HashMap<>();
    static String reqBody = null;

    public static void main(String[] args) {
        try {
            init();
            ExecutorService executorService = Executors.newCachedThreadPool();
            while(true){
                executorService.submit(()-> {
                    try {
                        processRequest();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void processRequest() throws IOException {
        parseRequest();

        String path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();

        sendResponse(path);
        out.flush();
        out.close();
    }

    private static void sendResponse(String path) {
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
    }

    private static void init() throws IOException {
        serverSocket = new ServerSocket(4221);
        serverSocket.setReuseAddress(true);

    }

    private static void parseRequest() throws IOException {
        clientSocket = serverSocket.accept(); // Wait for connection from client.
        out = new PrintWriter(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        reqLine = in.readLine();
        String line = in.readLine();
        while (line != null && !line.isBlank()) {
            Matcher headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches()) {
                headers.put(headerMatcher.group(1), headerMatcher.group(2));
            }
            line = in.readLine();
        }
    }


}
