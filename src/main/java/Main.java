import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        PrintWriter out;
        BufferedReader in;
        try {
            serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.
            out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            for (int i = 0; i < 1; i++) {
                String request = in.readLine();
                String[] reqParts = request.split("\r\n");
                String reqLine = reqParts[0];
                String path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();
                Pattern pattern = Pattern.compile("\\/echo\\/(.*)");
                Matcher matcher = pattern.matcher(path);
                System.out.println(path);
                System.out.println(matcher.matches());
                System.out.println(matcher.group(1));
                if (matcher.matches()) {
                    String message = matcher.group(1);
                    out.print(String.format("HTTP/1.1 200 OK\r\n\r\n" +
                            "Content-Type: text/plain\\r\\n" +
                            "Content-Length: %d\\r\\n\\r\\n%s", message.length(), message));
                } else {
                    out.print("HTTP/1.1 404 Not Found\r\n\r\n");
                }
                out.flush();
            }
            out.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


}
