package client;

import com.google.protobuf.ByteString;
import jdk.internal.util.xml.impl.Input;
import protobuf.Mess;
import util.MessageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class ScreenSender implements Runnable {
    Socket socket;
    int port = 5002;

    BufferedImage image;
    Robot robot;
    boolean running;

    InputStream is;
    OutputStream os;

    public ScreenSender() {
        try {
            socket = new Socket(Client.serverHost, 5002);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            running = true;
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        // set id
        Mess.Message setIdMess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.SET_ID).setId(Main.client.id).build();
        MessageUtil.sendMessage(os, setIdMess);
        System.out.println("Set id");

        try {
            while (running) {

                Thread.sleep(40);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRectangle = new Rectangle(screenSize);
                image = robot.createScreenCapture(screenRectangle);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                baos.flush();
                byte[] bufimg = baos.toByteArray();
                ByteString bs = ByteString.copyFrom(bufimg);


                Mess.Message mess = Mess.Message.newBuilder()
                        .addListId(Main.client.partnerId)
                        .setImg(bs)
                        .setType(Mess.Message.MessageType.VIEW_SCREEN)
                        .build();

                int error = MessageUtil.sendMessage(os, mess);
                if (error == 1) {
                    running = false;
                    System.out.println("Disconnected");
                }
            }

        } catch (Exception e) {
//                e.printStackTrace();
            running = false;
            System.out.println("Disconnected");
        } finally {
            this.stop();
        }

    }

    public void stop() {
        running = false;
        Mess.Message mess = Mess.Message.newBuilder()
                .setType(Mess.Message.MessageType.DISCONNECT)
                .addListId(Main.client.id)
                .addListId(Main.client.partnerId)
                .build();
        MessageUtil.sendMessage(os, mess);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
