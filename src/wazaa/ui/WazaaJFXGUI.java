package wazaa.ui;

import wazaa.Wazaa;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WazaaJFXGUI extends Application {
	private ScenesController root;
	
	@Override
	public void start(final Stage stage) throws Exception {
		try {
			Wazaa.setGUI(this);
			root = new ScenesController();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setTitle(Wazaa.WAZAANAME + " " + Wazaa.WAZAAVER);
			
			root.addSceneFromFXML("start", "StartScreen.fxml");
			root.addSceneFromFXML("main", "MainScreen.fxml");
			root.switchToScene("start");
			
			stage.show();
			
		} catch (Exception e) {}
	}
	
	@Override
	public void stop() throws Exception {
		System.out.println("Terminating client.");
		super.stop();
		System.exit(0);
	}

	public synchronized void refreshFoundFiles() {
		((MainScreen) root.getController("main")).refreshFoundFiles();
	}
	
	public synchronized void refreshDownloads() {
		((MainScreen) root.getController("main")).refreshDownloadsList();
	}
	
	public synchronized void refreshMachines() {
		((MainScreen) root.getController("main")).refreshMachinesList();
	}
}
