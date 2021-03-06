package kg.java;

import kg.java.logger.Logger;
import kg.java.messages.Message;
import kg.java.messages.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;


public class Server {
    private static final int PORT = 4444;
    private static final HashSet<ObjectOutputStream> senders = new HashSet<>();
    private static final ArrayList<String> onlineUsers = new ArrayList<>();

    public static void main(String[] args) {

        try {
            Logger.log("Chat Server is running");
            ServerSocket listener = new ServerSocket(PORT);
            while (true) {
                Handler handler = new Handler(listener.accept());
                handler.start();
            }
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
        public void run() {
            Logger.log("Attempting to connect a user...");
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Message firstMessage = (Message) objectInputStream.readObject();
                // TODO: checkDuplicateUsername(firstMessage);
                senders.add(objectOutputStream);
                username = firstMessage.getName();
                onlineUsers.add(username);
                sendMessage(firstMessage);

                while (socket.isConnected()) {
                    Message message = (Message) objectInputStream.readObject();
                    //TODO: handle message if DISCONNECT type
                    sendMessage(message);
                }
            } catch (SocketException e) {
                onlineUsers.remove(username);
                senders.remove(objectOutputStream);
                Message message = new Message();
                message.setName(username);
                message.setType(MessageType.DISCONNECTED);
                message.setMsg(String.format("User %s has been disconnected",username));
                sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


/*        private void sendNotification(Message message) {
            Message notification = new Message();
            notification.setName(message.getName());
            notification.setType(MessageType.NOTIFICATION);
        }*/

        private void sendMessage(Message message) {
                message.setUsers(onlineUsers);
                message.setOnlineCount(onlineUsers.size());
                for (ObjectOutputStream oos : senders) {
                    try {
                        oos.writeObject(message);
                        oos.flush();
                        oos.reset();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
