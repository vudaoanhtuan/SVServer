package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;
import server.ClientThread;
import util.MessageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;

public class ScreenReceiver implements Runnable {
    Socket socket;
    InputStream is;
    OutputStream os;
    int id;
    boolean running;

    public  ScreenReceiver() {
        try {
            this.socket = new Socket(Client.serverHost, 5002);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            id = Main.client.id;
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // send message to view screen
        JFrame frame = new JFrame();
        ImagePanel panel = new ImagePanel();
        frame.setResizable(true);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                super.windowClosing(e);
                Mess.Message mess = Mess.Message.newBuilder()
                        .setType(Mess.Message.MessageType.DISCONNECT)
                        .addListId(Main.client.id)
                        .addListId(Main.client.partnerId)
                        .build();
                MessageUtil.sendMessage(os, mess);
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });



        try {
            Mess.Message setIdMess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.SET_ID).setId(Main.client.id).build();
            MessageUtil.sendMessage(os, setIdMess);
            System.out.println("Set id");

            while (running) {
                Mess.Message mess = MessageUtil.recvMessage(is);
                if (mess == null) {
                    System.out.println("Disconnected");
                    break;
                }

                ByteString bs = mess.getImg();
                byte[] imgbuff = bs.toByteArray();

                InputStream _is = new ByteArrayInputStream(imgbuff);
                BufferedImage image = ImageIO.read(_is);
                panel.setImg(image);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
