package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    ArrayList<ClientThread> listConnection;
    int port;
    boolean status;
    ServerSocket listener;

    public Server(int port) {
        this.port = port;

        try {
            listener = new ServerSocket(port);
            status = true;
            listConnection = new ArrayList<ClientThread>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (status) {
                Socket client = listener.accept();
                ClientThread service = new ClientThread(client, listConnection);
                listConnection.add(service);
                Thread thread = new Thread(service);
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
