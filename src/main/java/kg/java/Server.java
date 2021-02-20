package kg.java;

import kg.java.logger.Logger;
import kg.java.messages.Message;
import kg.java.messages.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Server {
    private static final int PORT = 4444;
    private static final HashSet<ObjectOutputStream> senders = new HashSet<>();
    private static ArrayList<String> onlineUsers = new ArrayList<>();

    public static void main(String[] args) {

        try {
            Logger.log("Chat Server is running");
            ServerSocket listener = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private String username;
        private Socket socket;
        private OutputStream outputStream;
        private InputStream inputStream;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public synchronized void run() {
            Logger.log("Attempting to connect a user...");
            try {
                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);

                Message firstMessage = (Message) objectInputStream.readObject();
                // TODO: checkDuplicateUsername(firstMessage);
                senders.add(objectOutputStream);
                username = firstMessage.getName();
                onlineUsers.add(username);
                sendMessage(firstMessage);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*private void sendNotification(Message message) {
            Message notification = new Message();
            notification.setName(message.getName());
            notification.setType(message.getType());

        }*/

        private void sendMessage(Message message) {
            try {
                message.setUsers(onlineUsers);
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
                objectOutputStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
