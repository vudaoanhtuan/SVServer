package util;

import protobuf.Mess;

import java.io.InputStream;
import java.io.OutputStream;

public class MessageUtil {
    public static Mess.Message buildInfoMess(String info) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.INFO).setMess(info).build();
        return mess;
    }

    public static Mess.Message buildErrorMess(String error) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.ERROR).setMess(error).build();
        return mess;
    }

    public static Mess.Message buildChatMess(int id, String message) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.CHAT).setId(id).setMess(message).build();
        return mess;
    }

    public static Mess.Message buildConnectMess(int id) {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.CONNECT).setId(id).build();
        return mess;
    }

    public static Mess.Message buildConnectMess(String id) {
        int int_id = Integer.valueOf(id);
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.CONNECT).setId(int_id).build();
        return mess;
    }

    public static Mess.Message buildDisconnectMess() {
        Mess.Message mess = Mess.Message.newBuilder().setType(Mess.Message.MessageType.DISCONNECT).build();
        return mess;
    }

    public static Mess.Message recvMessage(InputStream is) {
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
    public static int sendMessage(OutputStream os, Mess.Message mess) {
        try {
            mess.writeDelimitedTo(os);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}
