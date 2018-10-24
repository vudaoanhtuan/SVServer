package client;

import com.google.protobuf.ByteString;
import protobuf.Mess;
import util.MessageUtil;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainWindow extends JFrame {
    private JTextField IDTextField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextPane sysLogTextPane;
    private JTextPane messageTextPane;
    private JTextField messageTextField;
    private JButton sendButton;
    private JPanel rootPanel;
    private JTextField myIDTextField;
    private JTextField filepathTextField;
    private JButton browseButton;
    private JButton sendFileButton;


    void setupID(int id) {
        myIDTextField.setText(String.valueOf(id));
    }

    void setupUI() {
        myIDTextField.setEditable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultCaret caretMessagePane = (DefaultCaret) messageTextPane.getCaret();
        caretMessagePane.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        DefaultCaret caretSysLogPane = (DefaultCaret) sysLogTextPane.getCaret();
        caretSysLogPane.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    void logSys(String mess) {
        try {
            Document doc = sysLogTextPane.getDocument();
            doc.insertString(doc.getLength(), mess + "\n", null);
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    void logMess(String id, String mess) {
        try {
            Document doc = messageTextPane.getDocument();
            doc.insertString(doc.getLength(), id + "\n" + mess + "\n", null);
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    void logMess(int id, String mess) {
        try {
            Document doc = messageTextPane.getDocument();
            doc.insertString(doc.getLength(), id + "\n" + mess + "\n", null);
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    public MainWindow() {
        add(rootPanel);
        setupUI();

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sid = IDTextField.getText();
                int id = Integer.valueOf(sid);
                Main.client.connectTo(id);
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = messageTextField.getText();
                Main.mainWindow.logMess("You", s);
                Main.client.sendChatMessage(s);
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.client.sendDisconnectMessage();
            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fd = new FileDialog((Frame)null, "Select File to Open");
                fd.setMode(FileDialog.LOAD);
                fd.setVisible(true);
                String filepath = fd.getDirectory() + fd.getFile();;
                filepathTextField.setText(filepath);
            }
        });

        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filepath = filepathTextField.getText();
                Main.client.sendFile(filepath);
            }
        });
    }
}
