package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;
import util.MessageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Client implements Runnable {
    Socket socket, fileSocket;
    InputStream is;
    OutputStream os;
    boolean status;
    int id, partnerId;

    ScreenSharer ss;

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
        // send message to view screen
        Mess.Message vmess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.VIEW_SCREEN).setId(id).build();
        MessageUtil.sendMessage(os, vmess);

        JFrame frame = new JFrame();
        ImagePanel panel = new ImagePanel();
        frame.setResizable(true);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        try {
            DatagramSocket socket = new DatagramSocket();
            // setId
            Mess.UDPMessage mess = Mess.UDPMessage.newBuilder().setId(this.id).setType(Mess.UDPMessage.MessageType.SET_ID).build();
            byte[] idbuff = mess.toByteArray();
            DatagramPacket idpacket = new DatagramPacket(idbuff, idbuff.length, address, 5002);
            byte[] buff = new byte[5*1024*1024];

            while (true) {
                Arrays.fill(buff, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                mess = Mess.UDPMessage.parseFrom(packet.getData());
                byte[] imgbuff = mess.getImg().toByteArray();

                InputStream is = new ByteArrayInputStream(imgbuff);
                BufferedImage image = ImageIO.read(is);
                panel.setImg(image);

            }

        } catch (Exception e) {
            e.printStackTrace();
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
            ss = new ScreenSharer(partnerId);
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
