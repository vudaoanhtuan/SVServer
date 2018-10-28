package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ScreenSharerServer {
    int port;
    boolean running;
    ServerSocket listener;


    public static void main(String[] args) {
        ScreenSharerServer server = new ScreenSharerServer(5002);
        server.run();
    }

    public ScreenSharerServer(int port) {
        this.port = port;

        try {
            listener = new ServerSocket(port);
            running = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (running) {
                Socket clientSocket = listener.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());
                System.out.println("Total connection: " + ClientThreadScreenSharer.listConnection.size());
                ClientThreadScreenSharer client = new ClientThreadScreenSharer(clientSocket);
                Thread thread = new Thread(client);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Server stoped");
        } finally {
            try {
                listener.close();
            } catch (Exception e) {

            }
        }
    }

}
