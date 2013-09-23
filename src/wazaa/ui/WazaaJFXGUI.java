package wazaa.ui;

import wazaa.Wazaa;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class WazaaJFXGUI extends Application {	
	public void start(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		StackPane root = new StackPane();
		Scene scene = new Scene(root, 250, 500);
		
		stage.setTitle(Wazaa.WAZAANAME + " " + Wazaa.WAZAAVER);
		stage.setScene(scene);
		stage.show();
	}

}
