package mapster.UserInterface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserInterface extends Application
{
    public static void main(String[] args) {
        Application.launch(UserInterface.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("InterfaceBuilder.fxml"));

        stage.setTitle("CS3800 Maptser");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }
}
