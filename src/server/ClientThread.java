package server;

import protobuf.Mess;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

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


    public static int getRandomId() {
        return random.nextInt(MAX_ID);
    }

    private void setIdForClient() {
        Mess.Message.Builder builder = Mess.Message.newBuilder();
        builder.setId(id);
        builder.setType(Mess.Message.MessageType.SET_ID);
        Mess.Message mess = builder.build();

        try {
            mess.writeDelimitedTo(this.os);
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
                Mess.Message mess = recvMessage();
                if (mess != null) {
                    this.handleMessage(mess);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Mess.Message recvMessage() {
        Mess.Message mess = null;
        try {
            while ((mess = Mess.Message.parseDelimitedFrom(is)) != null) {
                return mess;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // this method send message back to sender
    int sendMessage(Mess.Message mess) {
        try {
            mess.writeDelimitedTo(this.os);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    Mess.Message buildInfoMess(String info) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.INFO).setMess(info).build();
        return mess;
    }

    Mess.Message buildErrorMess(String error) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.ERROR).setMess(error).build();
        return mess;
    }

    void handleMessage(Mess.Message mess) {
        if (mess.getType() == Mess.Message.MessageType.CONNECT) {
            long id = mess.getId();
            synchronized (this) {
                for (ClientThread client: listConnection) {
                    if (client.id == id) {
                        clientConnected.add(client);
                        client.clientConnected.add(this);
                        Mess.Message resMess = buildInfoMess("Connected to" + String.valueOf(id));
                        sendMessage(resMess);
                        return;
                    }
                }
                // cannot find id
                Mess.Message resMess = buildErrorMess("Id not found");
                sendMessage(resMess);
            }
        }

        if (mess.getType() == Mess.Message.MessageType.DISCONNECT) {
            synchronized (this) {
                for (ClientThread client: clientConnected) {
                    for (int i=0; i<client.clientConnected.size(); i++) {
                        ClientThread otherClient = client.clientConnected.get(i);
                        if (otherClient.id == this.id) {
                            client.clientConnected.remove(i);
                            break;
                        }
                    }
                }
                clientConnected.clear();
            }
        }

        if (mess.getType() == Mess.Message.MessageType.CHAT) {
            // only 1 client, "this" is sender and client is recveiver
            if (clientConnected.size() == 1) {
                synchronized (this) {
                    ClientThread recver = clientConnected.get(0);
                    recver.sendMessage(mess);
                }
            }
            // handle multi connection
        }


    }
}
