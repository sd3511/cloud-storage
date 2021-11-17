package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lk.vivoxalabs.customstage.CustomStage;
import lk.vivoxalabs.customstage.CustomStageBuilder;
import lk.vivoxalabs.scenemanager.SceneManager;

public class Start extends Application {

    static CustomStage stage;

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("authorization.fxml"));
        SceneManager sceneManager = new SceneManager();
        sceneManager.addScene("0", fxmlLoader.load(), fxmlLoader.getController());
        Pane pane = sceneManager.getScene("0");
        stage = new CustomStageBuilder()
                .setWindowTitle("Cloud storage")
                .setTitleColor("")
                .setWindowColor("#00ffff")

                .build();
        stage.changeScene(pane);
        stage.show();

    }


}
