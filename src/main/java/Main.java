import jdk.jfr.Frequency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static ServerSocket serverSocket;
    static Socket clientSocket;
//    static PrintWriter out;
    static BufferedReader in;
    static Pattern headerPattern = Pattern.compile("([\\w-]+): (.*)");
    static String reqLine = null;
    static Map<String, String> headers = new HashMap<>();
    static String reqBody = null;

    public static void main(String[] args) {
        PrintWriter out;
        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            parseRequest();
            String s = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                    11 + "\r\n\r\n" + 12;
            clientSocket.getOutputStream().write(s.getBytes());

//            String path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();
//            if ("/".equals(path)) {
//                out.print("HTTP/1.1 200 OK\r\n\r\n");
//            } else if (path.startsWith("/echo")) {
//                String message = path.split("/")[2];
//                out.print("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
//                        message.length() + "\r\n\r\n" + message);
//            } else if (path.startsWith("/user-agent")) {
//                String userAgent = headers.get("User-Agent");
////                String s = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
////                        userAgent.length() + "\r\n\r\n" + userAgent;
//                clientSocket.getOutputStream().write(s.getBytes());
////                out.print();
//            } else {
//                out.print("HTTP/1.1 404 Not Found\r\n\r\n");
//            }
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void parseRequest() throws IOException {
        reqLine = in.readLine();
        String line = in.readLine();
        while (line != null) {
            Matcher headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches()) {
                headers.put(headerMatcher.group(1), headerMatcher.group(2));
            } else if (line.isBlank()) {
                reqBody = in.readLine();
                break;
            }
            line = in.readLine();
        }
    }


}
