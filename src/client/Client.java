package client;

import protobuf.Mess;
import util.MessageUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {
    Socket socket, fileSocket;
    InputStream is;
    OutputStream os;
    boolean status;
    int id, partnerId;

    ScreenSender ss;

    //    static String serverHost = "35.240.142.155";
    static String serverHost = "localhost";
    static InetAddress address;

    public Client() {
        status = true;
        createConnection();
        setID();
        try {
            address = InetAddress.getByName(serverHost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPartnerId() {
        return this.partnerId;
    }

    void createConnection() {
        try {
            socket = new Socket(serverHost, 5000);
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setID() {
        Mess.Message mess = MessageUtil.recvMessage(is);
        this.id = mess.getId();
        System.out.println(this.id);
        Main.mainWindow.setupID(this.id);
    }

    void connectTo(int id) {
        this.partnerId = id;
        Mess.Message mess = MessageUtil.buildConnectMess(id);
        MessageUtil.sendMessage(os, mess);
    }

    void sendFile(String filepath) {
        Main.mainWindow.logSys("Send file to " + partnerId);
        try {
            Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.SEND_FILE).setId(id).build();
            MessageUtil.sendMessage(os, mess);

            fileSocket = new Socket(serverHost, 5001);
            FileSender fs = new FileSender(fileSocket, filepath);
            Thread t = new Thread(fs);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void sendChatMessage(String s) {
        Mess.Message mess = MessageUtil.buildChatMess(this.id, s);
        MessageUtil.sendMessage(os, mess);
    }

    void sendDisconnectMessage() {
        if (!status)
            return;
        Mess.Message mess = MessageUtil.buildDisconnectMess();
        MessageUtil.sendMessage(os, mess);
    }

    void viewScreen() {
        Mess.Message vmess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.VIEW_SCREEN).setId(id).build();
        MessageUtil.sendMessage(os, vmess);

        ScreenReceiver sr = new ScreenReceiver();
        Thread t = new Thread(sr);
        t.start();
    }

    void stopViewScreen() {
        if (ss != null) {
            ss.stop();
        }
    }

    void handleMessage(Mess.Message mess) {
        if (mess.getType() == Mess.Message.MessageType.CHAT) {
            Main.mainWindow.logMess(mess.getId(), mess.getMess());
        }
        if (mess.getType() == Mess.Message.MessageType.INFO || mess.getType() == Mess.Message.MessageType.ERROR) {
            Main.mainWindow.logSys(mess.toString());
        }
        if (mess.getType() == Mess.Message.MessageType.SEND_FILE) {
            int id = mess.getId();
            Main.mainWindow.logSys("Receive file from " + id);
            try {
                fileSocket = new Socket(serverHost, 5001);
                FileReceiver fr = new FileReceiver(fileSocket);
                Thread t = new Thread(fr);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mess.getType() == Mess.Message.MessageType.CONNECT) {
            int partnerId = mess.getId();
            this.partnerId = partnerId;
        }
        if (mess.getType() == Mess.Message.MessageType.VIEW_SCREEN) {
            ss = new ScreenSender();
            try {
                Thread t = new Thread(ss);
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (status) {
            Mess.Message mess = MessageUtil.recvMessage(is);
            if (mess != null) {
                this.handleMessage(mess);
            }
        }
    }
}
