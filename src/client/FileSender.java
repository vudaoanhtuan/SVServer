package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;
import util.MessageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileSender implements Runnable {
    Socket socket;
    String filepath;

    public FileSender(Socket socket, String filepath) {
        this.socket = socket;
        this.filepath = filepath;
    }

    @Override
    public void run() {
        try {
            Main.mainWindow.logSys("Sending...");
            Thread.sleep(1000);

            File file = new File(filepath);
            String filename = file.getName();
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[(int) file.length()];
            fis.read(buff);
            ByteString bsbuff = ByteString.copyFrom(buff);
            Mess.Message mess = Mess.Message.newBuilder()
                    .setType(Mess.Message.MessageType.SEND_FILE)
                    .addListId(Main.client.partnerId)
                    .setContent(bsbuff)
                    .setFilename(filename)
                    .build();
            OutputStream os = socket.getOutputStream();
            MessageUtil.sendMessage(os, mess);
            Main.mainWindow.logSys("File send");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
