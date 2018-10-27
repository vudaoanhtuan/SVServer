package server;

import protobuf.Mess;
import util.MessageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;

import static util.MessageUtil.recvMessage;

public class ClientThreadScreenSharer implements Runnable {
    Socket socket;
    InputStream is;
    OutputStream os;
    int id;
    boolean running;
    //    ArrayList<ClientThreadScreenSharer> listConnection; // mutex
    static Hashtable listConnection = new Hashtable();


    public ClientThreadScreenSharer(Socket socketOfServer) {
        this.socket = socketOfServer;
        running = true;
        try {
            this.is = socketOfServer.getInputStream();
            this.os = socketOfServer.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            Mess.Message mess = recvMessage(is);
            if (mess != null) {
                this.handleMessage(mess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void handleMessage(Mess.Message mess) {
        if (mess.getType() == Mess.Message.MessageType.SET_ID) {
            synchronized (this) {
                listConnection.put(mess.getId(), this);
            }
        }
        if (mess.getType() == Mess.Message.MessageType.SEND_FILE) {
            byte[] fileContent = mess.getContent().toByteArray();
            synchronized (this) {
                for (int i=0; i<mess.getListIdCount(); i++) {
                    int id = mess.getListId(i);
                    ClientThreadScreenSharer client = (ClientThreadScreenSharer) listConnection.get(id);
                    if (client != null) {
                        MessageUtil.sendMessage(client.os, mess);
                    }
                    // close connection of receiver client;
                    try {
                        client.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // delete list connection
                    listConnection.remove(id);
                }
            }
            // close this connection
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
