package server;

import protobuf.Mess;
import util.MessageUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import static util.MessageUtil.*;

public class ClientThread implements Runnable {
    static Random random = new Random();
    static int MAX_ID = 1000000000;

    Socket socket;
    InputStream is;
    OutputStream os;
    int id;
    ArrayList<ClientThread> listConnection; // mutex
    ArrayList<ClientThread> clientConnected; // mutex


    public ClientThread(Socket socketOfServer, ArrayList<ClientThread> listConnection) {
        this.socket = socketOfServer;
        this.listConnection = listConnection;
        this.id = getRandomId();
        clientConnected = new ArrayList<ClientThread>();
        try {
            this.is = socketOfServer.getInputStream();
            this.os = socketOfServer.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // set ID when client connect and sent id to client
        this.setIdForClient();

        try {
            while (true) {
                Mess.Message mess = recvMessage(is);
                if (mess == null) {
                    break;
                }
                this.handleMessage(mess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public void closeConnection() {
        System.out.println("Client disconnected");
        Mess.Message mess = MessageUtil.buildInfoMess("User disconnected");
        for (ClientThread client : clientConnected) {
            MessageUtil.sendMessage(client.os, mess);
        }
    }


    public static int getRandomId() {
        return random.nextInt(MAX_ID);
    }

    private void setIdForClient() {
        Mess.Message.Builder builder = Mess.Message.newBuilder();
        builder.setId(id);
        builder.setType(Mess.Message.MessageType.SET_ID);
        Mess.Message mess = builder.build();

        try {
            MessageUtil.sendMessage(this.os, mess);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void handleMessage(Mess.Message mess) {
        if (mess.getType() == Mess.Message.MessageType.CONNECT) {
            int id = mess.getId();
            synchronized (this) {
                for (ClientThread client: listConnection) {
                    if (client.id == id) {
                        clientConnected.add(client);
                        client.clientConnected.add(this);
                        Mess.Message resMess = buildInfoMess("Connected to " + String.valueOf(id));
                        sendMessage(os, resMess);

                        Mess.Message resMessPartner = buildInfoMess("Connected from " + String.valueOf(this.id));
                        sendMessage(client.os, resMessPartner);

                        Mess.Message resConnect = buildConnectMess(this.id);
                        sendMessage(client.os, resConnect);

                        return;
                    }
                }
                // cannot find id
                Mess.Message resMess = buildErrorMess("Id not found");
                sendMessage(os, resMess);
            }
        }

        if (mess.getType() == Mess.Message.MessageType.DISCONNECT) {
            synchronized (this) {
                for (ClientThread client: clientConnected) { // loop on client A
                    for (int i=0; i<client.clientConnected.size(); i++) { // loop on client which connected to A
                        ClientThread otherClient = client.clientConnected.get(i);
                        if (otherClient.id == this.id) {
                            client.clientConnected.remove(i);
                            Mess.Message infoMess = buildInfoMess(this.id + " disconnected");
                            sendMessage(client.os, infoMess);
                            break;
                        }
                    }
                }
                clientConnected.clear();
                Mess.Message infoMess = buildInfoMess("Disconnected from server");
                sendMessage(os, infoMess);
            }
        }

        if (mess.getType() == Mess.Message.MessageType.CHAT) {
            synchronized (this) {
                for (ClientThread client : clientConnected) {
                    sendMessage(client.os, mess);
                }
            }
        }

        if (mess.getType() == Mess.Message.MessageType.SEND_FILE) {
            synchronized (this) {
                for (ClientThread client : clientConnected) {
                    sendMessage(client.os, mess);
                }
            }
        }


    }
}
