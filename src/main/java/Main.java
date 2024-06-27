import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static ServerSocket serverSocket;


    public static void main(String[] args) {
        try {
            init();
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> {
                    try {
                        new Connection(socket, args).processRequest();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void init() throws IOException {
        serverSocket = new ServerSocket(4221);
        serverSocket.setReuseAddress(true);
    }

}
