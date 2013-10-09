package wazaa.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
//import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ScenesController extends StackPane {
	private Map<String, Parent> scenes = new HashMap<String, Parent>();
//	private Map<String, Initializable> controllers = 
//			new HashMap<String, Initializable>();
	
	public void addSceneFromFXML(String sceneName, String fxmlFilename) 
			throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilename));
		Parent scene = (Parent) loader.load();
		scenes.put(sceneName, scene);
//		controllers.put("sceneName",
//				(Initializable) loader.getController());
	}
	
	public void switchToScene(String sceneName) {
		getScene().getWindow().hide();
		if (!getChildren().isEmpty()) {
			getChildren().clear();
		}
		getChildren().add(scenes.get(sceneName));
		getScene().getWindow().sizeToScene();
		((Stage) getScene().getWindow()).show();
	}
}
