package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    public static final Logger logger;
    static {
        logger = Logger.getLogger(Server.class.getName());
    }

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        //authService = new SimpleAuthService();
        authService = new DataBaseAuthService();

        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            //System.out.println("Сервер запущен!");
            logger.log(Level.INFO, "Сервер запущен!");

            while (true) {
                socket = server.accept();
                logger.log(Level.INFO, "Клиент подключился");
                logger.log(Level.INFO, "socket.getRemoteSocketAddress(): "+ socket.getRemoteSocketAddress());
                logger.log(Level.INFO, "socket.getLocalSocketAddress() "+ socket.getLocalSocketAddress());
                //System.out.println("Клиент подключился");
                //System.out.println("socket.getRemoteSocketAddress(): "+ socket.getRemoteSocketAddress());
                //System.out.println("socket.getLocalSocketAddress() "+ socket.getLocalSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("%s : %s", sender.getNick(), msg);

        for (ClientHandler client : clients) {
            client.sendMsg(message);
        }
    }

    void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler client : clients) {
            sb.append(client.getNick()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[%s] private [%s] : %s", sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if(c.getNick().equals(receiver)){
                c.sendMsg(message);
                if (!sender.getNick().equals(receiver))
                    sender.sendMsg(message);
                return;
            }
        }
        sender.sendMsg(String.format("Client %s not found", receiver));
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean isLoginAuthorized(String login) {
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

}
