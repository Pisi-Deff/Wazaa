package wazaa.ui;

import wazaa.Wazaa;
import wazaa.http.HTTPServer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
