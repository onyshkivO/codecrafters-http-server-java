import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
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
    private String method;
    private String path;
    private String[] args;

    public Connection(ServerSocket socket, String[] args) throws IOException {
        clientSocket = socket.accept();
        this.args = args;
    }

    private void parseRequest() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        reqLine = in.readLine();
        path = reqLine.substring(reqLine.indexOf(' '), reqLine.lastIndexOf(' ')).trim();
        method = reqLine.substring(0, reqLine.indexOf(' '));
        Matcher headerMatcher;
        String line = in.readLine();
        while (line != null && !line.isBlank()) {
            headerMatcher = headerPattern.matcher(line);
            if (headerMatcher.matches()) {
                headers.put(headerMatcher.group(1), headerMatcher.group(2));
            }
            line = in.readLine();
        }
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[contentLength];
            int bytesRead = in.read(buffer, 0, contentLength);
            if (bytesRead != -1) {
                reqBody = String.valueOf(buffer);
            }
        }
        System.out.println(reqBody);
    }

    public void processRequest() throws IOException {
        parseRequest();
        process();
    }

    private void process() throws IOException {

        if ("/".equals(path)) {
            out.print("HTTP/1.1 200 OK\r\n\r\n");
        } else if (path.startsWith("/echo")) {
            String message = path.split("/")[2];
            String response = String.format("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: %d\r\n\r\n%s", message.length(), message);
            out.print(response);
        } else if (path.startsWith("/user-agent")) {
            String userAgent = headers.get("User-Agent");
            String response = String.format("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: %d\r\n\r\n%s", userAgent.length(), userAgent);
            out.print(response);
        } else if (path.startsWith("/files/")) {
            String fileName = path.split("/")[2];
            String directory = args[1];
            switch (method) {
                case "GET":
                    getFile(directory + fileName);
                    break;
                case "POST":
                    createFile(directory + fileName);
                    break;
            }
        } else {
            out.print("HTTP/1.1 404 Not Found\r\n\r\n");
        }
        out.flush();
        out.close();
    }

    private void getFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            String fileContent = new String(Files.readAllBytes(file.toPath()));
            String response = String.format("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Length: %d\r\n\r\n%s", fileContent.length(), fileContent);
            out.print(response);
        } else {
            out.print("HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }

    private void createFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        myWriter.write(reqBody);
        myWriter.close();
        out.print("HTTP/1.1 201 Created\r\n\r\n");
    }
}
