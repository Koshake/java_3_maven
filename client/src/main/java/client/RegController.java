package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {

    Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public TextField nickField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    @FXML
    public TextArea textArea;

    public void tryToReg(ActionEvent actionEvent) {
        controller.tryToReg(loginField.getText().trim(),
                passwordField.getText().trim(),
                nickField.getText().trim());
    }

    public void clickCancelBtn(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            ((Stage) loginField.getScene().getWindow()).close();
        });
    }

    public  void addMessage(String msg) {
        textArea.appendText(msg + "\n");
    }
}
