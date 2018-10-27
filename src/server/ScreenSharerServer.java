package server;

import com.google.protobuf.InvalidProtocolBufferException;
import javafx.util.Pair;
import protobuf.Mess;

import java.net.*;
import java.util.Hashtable;

public class ScreenSharerServer {
    int port;
    boolean status;
    DatagramSocket socket;
    byte[] buffer = new byte[5*1024*1024];
    Hashtable<Integer, Pair<InetAddress, Integer>> h = new Hashtable<Integer, Pair<InetAddress, Integer>>();

    public static void main(String[] args) {
        ScreenSharerServer server = new ScreenSharerServer(5002);
        server.run();
    }

    public ScreenSharerServer(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (status) {
                // receive packet from client
                DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(recvPacket);
                handlePacket(recvPacket);
            }
        } catch (Exception e) {
            System.out.println("Server stoped");
        } finally {
            try {
                socket.close();
            } catch (Exception e) {

            }
        }
    }

    void handlePacket(DatagramPacket packet) {
        try {
            Mess.UDPMessage mess = Mess.UDPMessage.parseFrom(packet.getData());
            if (mess.getType() == Mess.UDPMessage.MessageType.SET_ID) {
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                int id = mess.getId();
                Pair<InetAddress, Integer> p = new Pair<InetAddress, Integer>(address, port);
                h.put(id, p);
                System.out.println("Set ID: " + id);
            }
            if (mess.getType() == Mess.UDPMessage.MessageType.SCREEN) {
                for (Integer i: mess.getListIdList()) {
                    Pair<InetAddress, Integer> p = h.get(i);
                    if (p != null) {
                        InetAddress address = p.getKey();
                        int port = p.getValue();
                        packet.setAddress(address);
                        packet.setPort(port);
                        socket.send(packet);
                    }
                }
            }
            if (mess.getType() == Mess.UDPMessage.MessageType.DISCONNECT) {
                int id = mess.getId();
                h.remove(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
