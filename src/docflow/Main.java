package docflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("docflow.fxml"));
        Parent root = loader.load();
        MainController mainController = loader.getController();
        primaryStage.setOnHidden(event -> mainController.onClose());
        primaryStage.setTitle("DocFlow");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
    }
}
