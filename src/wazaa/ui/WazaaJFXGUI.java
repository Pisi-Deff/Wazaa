package wazaa.ui;

import wazaa.Wazaa;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WazaaJFXGUI extends Application {
	public void launchApp(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		try {
		Parent root = FXMLLoader.load(
						getClass().getResource("StartScreen.fxml"));
		Scene scene = new Scene(root);
		
		stage.setTitle(Wazaa.WAZAANAME + " " + Wazaa.WAZAAVER);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
		} catch (Exception e) {}
	}
	
	@Override
	public void stop() throws Exception {
		System.out.println("Terminating client.");
		super.stop();
		System.exit(0);
	}

}
