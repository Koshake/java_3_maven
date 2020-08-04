package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ClientHandler {
    Server server;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    private String nick;
    private String login = null;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            final String AUTH = "/auth ";
            final  String AUTH_OK = "/authok ";
            final String PRIVAT_MSG = "/w ";
            final String END = "/end";
            final String REG = "/reg ";
            final String REG_RESULT = "/regresult ";
            final int AUTH_WORDS_COUNT = 3;
            final int PRIVATE_WORDS_COUNT = 3;
            final  int TIMEOUT = 120000;
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new Thread(() -> {
                try {
                    socket.setSoTimeout(TIMEOUT);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith(AUTH)) {
                            String[] token = str.split("\\s");
                            if (token.length < AUTH_WORDS_COUNT) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNick != null) {
                                if(!server.isLoginAuthorized(login)) {

                                    sendMsg(AUTH_OK + newNick + " " + login);
                                    nick = newNick;

                                    server.subscribe(this);
                                    //System.out.printf("Клиент %s подключился \n", nick);
                                    Server.logger.log(Level.INFO, String.format("Клиент %s подключился \n", nick));
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("С этим логином уже авторизовались!");
                                    Server.logger.log(Level.INFO, "Повторное использование логина");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                                Server.logger.log(Level.INFO, "Был введен неверный логин / пароль");
                            }
                        }

                        if (str.startsWith(REG)) {
                            String[] token = str.split("\\s");
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg(REG_RESULT + "ok");
                                Server.logger.log(Level.INFO, "Регистрация прошла упешно!");
                            } else {
                                sendMsg(REG_RESULT + "failed");
                                Server.logger.log(Level.INFO, "Регистрация провалена!");
                            }
                        }
                    }
                    //цикл работы
                    while (!socket.isClosed()) {
                        String str = in.readUTF();

                        if(str.startsWith(PRIVAT_MSG)) {
                            String[] token = str.split("\\s", 3);
                            if (token.length < PRIVATE_WORDS_COUNT) {
                                continue;
                            }

                            server.privateMsg(this, token[1], token[2]);
                            continue;
                        }
                        if (str.equals(END)) {
                            out.writeUTF(END);
                            break;
                        }
                        server.broadcastMsg(this, str);
                    }
                } catch (SocketTimeoutException e) {
                    sendMsg(END);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //System.out.println("Клиент отключился");
                    Server.logger.log(Level.INFO, "Клиент отключился");
                    server.unsubscribe(this);
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                        service.shutdown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMsg(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public  String getLogin() {
        return login;
    }

}
