package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private final String CHAT_TITLE_EMPTY = "Messenger";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private HistoryHandler historyHandler;

    private boolean authenticated;
    private String nick;
    private String login;

    private Stage stage;
    private Stage regStage;
    RegController regController;
    final String AUTH = "/auth ";
    final  String AUTH_OK = "/authok ";
    final String END = "/end";
    final String REG_RESULT = "/regresult ";
    final String CLIENTS_LIST = "/clientlist ";
    final int historyLen = 100;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        if (!authenticated) {
            nick = "";
        }
        setTitle(nick);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("bye");
                    if (socket != null && !socket.isClosed()) {
                        try {
                            out.writeUTF(END);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });

        setAuthenticated(false);

        regStage = createRegWindow();

    }

    private Stage createRegWindow() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
            stage.setTitle("Registration");
            stage.setScene(new Scene(root, 350, 250));
            stage.initModality(Modality.APPLICATION_MODAL);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if(str.equals(END)) {
                            throw new RuntimeException();
                        } else

                        if (str.startsWith(AUTH_OK)) {
                            nick = str.split("\\s")[1];
                            login = str.split("\\s")[2];
                            setAuthenticated(true);
                            historyHandler = new HistoryHandler(login);
                            textArea.appendText(historyHandler.readHistory(historyLen));
                            break;
                        }

                        if (str.startsWith(REG_RESULT)) {
                            String result = str.split("\\s")[1];
                            if (result.equals("ok")) {
                                regController.addMessage("Регистрация прошла успешно!");
                            } else {
                                regController.addMessage("Регистрация не пройдена. Возможно логин или ник заняты!");
                            }
                            continue;
                        }
                        textArea.appendText(str + "\n");
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(END)) {
                                setAuthenticated(false);
                                break;
                            }
                            if (str.startsWith(CLIENTS_LIST)) {
                                String[] token = str.split("\\s");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                        } else {
                            textArea.appendText(str + "\n");
                            historyHandler.writeToHistoryFile(str + "\n");
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    System.out.println("Отключение со стороны сервера!");
                } finally {
                    try {
                        in.close();
                        out.close();
                        historyHandler.closeFileWriter();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        System.out.println("Клиент отключился!");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.requestFocus();
            textField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(String.format(AUTH + "%s %s", loginField.getText().trim(), passwordField.getText().trim()));
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(String.format("/reg %s %s %s", login, password, nickname));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nick) {
        Platform.runLater(() -> {
            stage.setTitle(CHAT_TITLE_EMPTY + " : " + nick);
        });
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText(String.format("/w %s ", receiver));
    }

    public void showRegWindow(ActionEvent actionEvent) {
        regStage.show();
    }
}
