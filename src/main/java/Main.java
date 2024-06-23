import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static ServerSocket serverSocket;


    public static void main(String[] args) {
        try {
            init();
            System.out.println(Arrays.toString(args));
            new Connection(serverSocket, args).processRequest();

//            ExecutorService executorService = Executors.newFixedThreadPool(10);
//            while (true) {
//                executorService.submit(() -> {
//                    try {
//                        new Connection(serverSocket, args).processRequest();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void init() throws IOException {
        serverSocket = new ServerSocket(4221);
        serverSocket.setReuseAddress(true);
    }

}
