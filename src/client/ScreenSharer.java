package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ScreenSharer implements Runnable {
    DatagramSocket socket;
    InetAddress svaddress;
    int partnerId;
    static String host = "localhost";
    int port = 5002;

    BufferedImage image;
    Robot robot;
    boolean running;

    public ScreenSharer(int partnerId) {
        this.partnerId = partnerId;
        try {
            socket = new DatagramSocket();
            svaddress = InetAddress.getByName(host);
            running = true;
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(2000);


                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRectangle = new Rectangle(screenSize);
                image = robot.createScreenCapture(screenRectangle);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                baos.flush();
                byte[] bufimg = baos.toByteArray();
                ByteString bs = ByteString.copyFrom(bufimg);

                Mess.UDPMessage mess = Mess.UDPMessage.newBuilder().setId(partnerId).setImg(bs).setType(Mess.UDPMessage.MessageType.SCREEN).build();

                byte[] buff = mess.toByteArray();

                DatagramPacket packet = new DatagramPacket(buff, buff.length, svaddress, port);
                socket.send(packet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
