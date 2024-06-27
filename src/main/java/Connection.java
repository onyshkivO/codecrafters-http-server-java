import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class Connection {
    private Socket clientSocket;
    private OutputStream out;
    private String[] args;

    public Connection(Socket socket, String[] args) throws IOException {
        clientSocket = socket;
        out = clientSocket.getOutputStream();
        this.args = args;
    }


    public void processRequest() throws IOException {
        Request request = Request.buildResponse(clientSocket);
        Response response = process(request);
        out.write(response.buildResponse());

        out.flush();
        out.close();
    }

    private Response process(Request request) throws IOException {
        if ("/".equals(request.getUri())) {
            return Response.builder()
                    .status(200)
                    .build();
        } else if (request.getBody().startsWith("/echo")) {
            String encodingHeadersStr = request.getHeaders().getOrDefault("Accept-Encoding", "");
            byte[] message = request.getUri().split("/")[2].getBytes();
            return Response.builder()
                    .status(200)
                    .body(message)
                    .headers(Map.of("Content-Type", "text/plain"))
                    .compressType(encodingHeadersStr.equals("gzip") ? CompressType.GZIP : null)
                    .build();
        } else if (request.getUri().startsWith("/user-agent")) {
            byte[] userAgent = request.getHeaders().get("User-Agent").getBytes();
            return Response.builder()
                    .status(200)
                    .headers(Map.of("Content-Type", "text/plain"))
                    .body(userAgent)
                    .build();
        } else if (request.getUri().startsWith("/files/")) {
            String filePath = request.getUri().split("/")[2];
            if (args.length > 1 && args[0].equals("--directory")) {
                filePath = args[1] + filePath;
            }
            switch (request.getMethod()) {
                case "GET":
                    return getFile(filePath);
                case "POST":
                    return createFile(filePath, request);
            }
        }
        return Response.builder()
                .status(404)
                .build();

    }

    private Response getFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            byte[] fileContent = new String(Files.readAllBytes(file.toPath())).getBytes();
            return Response.builder()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/octet-stream"))
                    .body(fileContent)
                    .build();
        }
        return Response.builder()
                .status(404)
                .build();
    }

    private Response createFile(String filePath, Request request) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileWriter myWriter = new FileWriter(file);
        myWriter.write(request.getBody());
        myWriter.close();
        return Response.builder()
                .status(201)
                .build();
    }


}
