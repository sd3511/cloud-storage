package gui;


import commonclasses.authmessages.RegistrationMessage;
import connection.Network;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.vivoxalabs.scenemanager.SceneManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
@Slf4j
public class AuthController implements Initializable {


    public TextField login;
    public PasswordField password;
    FXMLLoader loader;
    private Network network;
    private CloudAreaController cl;
    private Stage stageReg;

    public CloudAreaController getCl() {
        return cl;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(this);

    }


    public void signIn(ActionEvent actionEvent) {
        network.signIn(login.getText(), password.getText());

    }


    public void register(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register.fxml"));
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        RegisterController reg = fxmlLoader.getController();
        Scene scene = new Scene(parent, 200, 200);
        stageReg = new Stage();
        stageReg.setScene(scene);
        stageReg.setWidth(500);
        stageReg.setHeight(200);
        stageReg.initModality(Modality.WINDOW_MODAL);
        stageReg.initOwner(Start.stage);
        stageReg.show();
        reg.register.setOnAction(event ->network.send(new RegistrationMessage(reg.login.getText(),reg.password.getText())));

    }


    public void switchWindow() {
        loader = new FXMLLoader(getClass().getResource("cloudArea.fxml"));
        Parent parent = null;
        try {
            parent = loader.load();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        cl = loader.getController();
        cl.setLogin(login.getText());
        cl.setNetwork(network);
        SceneManager sceneManager = new SceneManager();
        sceneManager.addScene("1", parent, cl);
        Pane pane = sceneManager.getScene("1");

        Platform.runLater(() -> {
            Start.stage.changeScene(pane);
            Start.stage.show();
        });

    }

    public void registerOk() {
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Регистрация успешна");
            

            alert.setOnCloseRequest(event -> stageReg.close());
            alert.show();

        });

    }

    public void alertWarning(String warningMessage) {
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(warningMessage);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(Start.stage);
            alert.setOnCloseRequest(event ->{
                login.clear();
                password.clear();
            } );
            alert.show();
        });

    }
}
