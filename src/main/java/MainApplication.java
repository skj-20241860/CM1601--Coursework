import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        stage.setScene(new Scene(loader.load(), 1100, 700));
        stage.setTitle("Malabe Spares Depot");
        stage.setOnCloseRequest(event -> {
            MainController controller = loader.getController();
            if (controller != null) controller.saveBeforeExit();
        });
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
