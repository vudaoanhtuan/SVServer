package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileTransferServer {
    int port;
    boolean status;
    ServerSocket listener;


    public static void main(String[] args) {
        FileTransferServer server = new FileTransferServer(5001);
        server.run();
    }

    public FileTransferServer(int port) {
        this.port = port;

        try {
            listener = new ServerSocket(port);
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (status) {
                Socket clientSocket = listener.accept();
                ClientThreadFileTransfer client = new ClientThreadFileTransfer(clientSocket);
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
