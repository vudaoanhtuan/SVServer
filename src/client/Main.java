package client;

import javax.swing.*;

public class Main {
    public static MainWindow mainWindow;
    public static Client client;
    public static JFrame screen;

    public static void main(String[] args) {
        createUI();
        // wait for create UI
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        createClient();
    }

    static void createUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                mainWindow.setSize(400,600);
                mainWindow.setTitle("Client");
            }
        });
    }

    static void createClient() {
        client = new Client();
        Thread thread = new Thread(client);
        thread.start();
    }
}