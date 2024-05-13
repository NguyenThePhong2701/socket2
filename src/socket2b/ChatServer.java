package socket2b;

import java.io.*;
import java.net.*;
import java.util.HashSet;

public class ChatServer {
    private static final int PORT = 12345;
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server chat đã được khởi động...");

            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    out.println("Enter your username:");
                    username = in.readLine();
                    if (username != null && !username.trim().isEmpty()) {
                        synchronized (writers) {
                            for (PrintWriter writer : writers) {
                                writer.println(username + " has joined the chat.");
                            }
                            writers.add(out);
                            break;
                        }
                    }
                }

                String message;
                while ((message = in.readLine()) != null) {
                    synchronized (writers) {
                        for (PrintWriter writer : writers) {
                            writer.println(username + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Lỗi: " + e.getMessage());
            } finally {
                if (username != null) {
                    synchronized (writers) {
                        writers.remove(out);
                        for (PrintWriter writer : writers) {
                            writer.println(username + " has left the chat.");
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Lỗi khi đóng kết nối: " + e.getMessage());
                }
            }
        }
    }
}