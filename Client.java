import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader serverIn;
    private BufferedReader consoleIn;
    private PrintWriter out;
    // private static final String SERVER_ADDRESS = "localhost";
    // private static final int SERVER_PORT =
    // private String username;

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            consoleIn = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }).start();

            String message;
            while ((message = consoleIn.readLine()) != null) {
                out.println(message);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    void closeConnection() {
        try {
            if (socket != null)
                socket.close();
            if (serverIn != null)
                serverIn.close();
            if (consoleIn != null)
                consoleIn.close();
            if (out != null)
                out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client("localhost", 12345);

    }
}
