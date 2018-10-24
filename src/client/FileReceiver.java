package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;
import util.MessageUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileReceiver implements Runnable {
    Socket socket;

    public FileReceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Main.mainWindow.logSys("Receiving...");

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            Mess.Message setIdMess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.SET_ID).setId(Main.client.id).build();
            MessageUtil.sendMessage(os, setIdMess);

            Mess.Message mess = MessageUtil.recvMessage(is);
            String filename = mess.getFilename();

            ByteString bs = mess.getContent();
            byte[] buff = bs.toByteArray();

            OutputStream fos = new FileOutputStream(filename);
            fos.write(buff);
            fos.close();

            Main.mainWindow.logSys("File received");

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
