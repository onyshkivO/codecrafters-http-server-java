import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String request = in.readLine();
            String[] reqParts = request.split("\\r\\n");
            String[] reqParts2 = request.split("\\r\\n\\r\\n");
            System.out.println(Arrays.toString(reqParts));
            System.out.println(Arrays.toString(reqParts2));
            String reqLine = reqParts[0];
            String path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();

            if ("/".equals(path)) {
                out.print("HTTP/1.1 200 OK\r\n\r\n");
            } else if (path.startsWith("/echo")) {
                String message = path.split("/")[2];
                out.print("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                        message.length() + "\r\n\r\n" + message);
            } else if (path.startsWith("/user-agent")){


            }
            else {
                out.print("HTTP/1.1 404 Not Found\r\n\r\n");
            }
            out.flush();

            out.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


}
