import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // private int currentId;
    private static HashMap<String, Socket> clients = new HashMap<>();
    private static final int PORT = 12345;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client connected");

                // Create a new Thread, which, when created, runs the handleClient method, and
                // then starts the thread for the client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        String username = null;
        try { // when the client joins
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // ask client for username
            username = getUsername(in, out);
            clients.put(username, clientSocket);

            broadcastMessage(username + " has joined the room", username);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/commands")) {
                    out.println("Available commands: \n/msg <username> <message> - send a private message to a user");
                    out.println("/commands - list all available commands");
                    out.println("/users - list all users currently in the chat room");
                    out.println("/exit - leave the chat room");
                } else if (message.startsWith("/msg")) {
                    handlePrivateMessage(username, message);
                } else if (message.startsWith("/users")) {
                    showAllUsers(username);
                } else if (message.startsWith("/exit")) {
                    closeConnection(clientSocket);
                } else {
                    broadcastMessage(username + ": " + message, username);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // when the client leaves
            broadcastMessage(username + " has left the room", username);
            clients.remove(username);
            closeConnection(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUsername(BufferedReader in, PrintWriter out) throws IOException {
        String username = "";
        while (true) {
            out.println("Enter your username: ");
            username = in.readLine();
            if (username.isEmpty()) {
                out.println("Username cannot be empty. Try again.");
            } else if (clients.containsKey(username)) {
                out.println("Username already taken. Try another one.");
            } else if (username.contains(" ")) {
                out.println("Username cannot have spaces. Try '-' instead.");
            } else {
                break;
            }
        }
        out.println("Welcome " + username + "! Type /commands for a list of available commands.");
        return username;
    }

    // send this message to everyone in the server
    public void broadcastMessage(String message, String sender) {
        for (Map.Entry<String, Socket> client : clients.entrySet()) {
            if (!client.getKey().equals(sender)) {
                try {
                    PrintWriter out = new PrintWriter(client.getValue().getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showAllUsers(String sender) {
        String allUsers = "Connected users: ";
        for (String user : clients.keySet()) {
            allUsers += user + ", ";
        }

        Socket senderSocket = clients.get(sender);
        try {
            PrintWriter out = new PrintWriter(senderSocket.getOutputStream(), true);
            out.println(allUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePrivateMessage(String sender, String message) {
        System.out.println("handling private message from " + sender + ": " + message);
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            return;
        }

        String recipient = parts[1];
        String messageContent = parts[2];
        if (!clients.containsKey(recipient)) {
            try {
                PrintWriter out = new PrintWriter(clients.get(sender).getOutputStream(), true);
                out.println("This person is not in this chat room");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Socket recipientSocket = clients.get(recipient);
        try {
            PrintWriter out = new PrintWriter(recipientSocket.getOutputStream(), true);
            out.println("Private message from " + sender + ": " + messageContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(Socket clientSocket) {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}