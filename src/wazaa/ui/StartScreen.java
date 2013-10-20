package wazaa.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import wazaa.Wazaa;
import wazaa.http.HTTPServer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;

public class StartScreen implements Initializable {
    @FXML
    private HBox startScreenBox;
    
    @FXML
    private ToggleButton showSettingsToggleButton;

    @FXML
    private VBox settingsBox;
	@FXML
	private TextField portField;
	@FXML
	private TextField shareFolderField;
	private DirectoryChooser shareDirChooser = new DirectoryChooser();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		shareFolderField.setText(
				Wazaa.getShareFolderPath().toAbsolutePath().
				toString());
		settingsBox.setStyle(
				"-fx-border-width: 0 0 0 1;" +
				"-fx-border-color: #000000;"); 
		startScreenBox.getChildren().remove(settingsBox);
		portField.setText(String.valueOf(Wazaa.DEFAULTPORT));
		portField.addEventFilter(KeyEvent.KEY_TYPED,
				new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent inputevent) {
				if (!inputevent.getCharacter().matches("\\d")) {
					inputevent.consume();
				}
			}
		});
		portField.textProperty().addListener(
				new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> obsValue,
							String oldValue, String newValue) {
						try {
							Integer value = 
									Integer.valueOf(newValue);
							if (value > 65535) {
								portField.setText("65535");
							}
						} catch (NumberFormatException e) {}
					}
		});
	}
	
	@FXML
	private void startButtonAction(ActionEvent event) {
		try {
			Integer port = 
					Integer.valueOf(portField.getText());
			try {
				if (port >= 0 && port <= 65535) {
					HTTPServer srv = new HTTPServer(port);
					Wazaa.setHTTPServer(srv);
					// TODO: tmp
					Wazaa.getMachinesFromFile(Wazaa.DEFAULTMACHINESFILE);
					try {
						Wazaa.prepareShareFolder();
						
						ScenesController root = (ScenesController) 
								startScreenBox.getScene().getRoot();
						((MainScreen)root.getController("main")).
							refreshSharedFilesButtonAction(null);
						((MainScreen)root.getController("main"))
							.setListeningStatusLabelText(portField.getText());
						root.switchToScene("main");
					} catch (IOException e) {
						showSettingsToggleButton.setSelected(true);
						if (!startScreenBox.getChildren().contains(
								settingsBox)) {
							showSettingsToggleButtonAction(null);
						}
						shareFolderField.requestFocus();
						Popup p = new Popup();
						VBox box = new VBox();
						Label l = new Label("Unable to use specified share folder.");
						box.getChildren().add(l);
			            box.setStyle("-fx-background-color: #FFD0A0;"
			            		+ "-fx-border-color: #000000;"
			            		+ "-fx-border-width: 1;");
			            box.setPadding(new Insets(5));
			            p.getContent().add(box);
			            p.setAutoHide(true);
			            p.setHideOnEscape(true);
			            p.show(portField.getScene().getWindow());
					}
				} else {
					showSettingsToggleButton.setSelected(true);
					if (!startScreenBox.getChildren().contains(
							settingsBox)) {
						showSettingsToggleButtonAction(null);
					}
					portField.requestFocus();
				}
			} catch (IOException e) {
				showSettingsToggleButton.setSelected(true);
				if (!startScreenBox.getChildren().contains(
						settingsBox)) {
					showSettingsToggleButtonAction(null);
				}
				portField.requestFocus();
				Popup p = new Popup();
				VBox box = new VBox();
				Label l = new Label("Port already in use.");
				box.getChildren().add(l);
	            box.setStyle("-fx-background-color: #FFD0A0;"
	            		+ "-fx-border-color: #000000;"
	            		+ "-fx-border-width: 1;");
	            box.setPadding(new Insets(5));
	            p.getContent().add(box);
	            p.setAutoHide(true);
	            p.setHideOnEscape(true);
	            p.show(portField.getScene().getWindow());
			}
		} catch (NumberFormatException e) {
			showSettingsToggleButton.setSelected(true);
			if (!startScreenBox.getChildren().contains(
					settingsBox)) {
				showSettingsToggleButtonAction(null);
			}
			portField.requestFocus();
			Popup p = new Popup();
			VBox box = new VBox();
			Label l = new Label("Invalid port");
			box.getChildren().add(l);
            box.setStyle("-fx-background-color: #FFD0A0;"
            		+ "-fx-border-color: #000000;"
            		+ "-fx-border-width: 1;");
            box.setPadding(new Insets(5));
            p.getContent().add(box);
            p.setAutoHide(true);
            p.setHideOnEscape(true);
            p.show(portField.getScene().getWindow());
		}
	}
	
    @FXML
    private void showSettingsToggleButtonAction(ActionEvent event) {
    	if (startScreenBox.getChildren().contains(settingsBox)) {
    		startScreenBox.getChildren().remove(settingsBox);
    	} else {
    		startScreenBox.getChildren().add(settingsBox);
    	}
    	startScreenBox.getScene().getWindow().sizeToScene();
    }

    @FXML
    private void shareFolderChangeButtonAction(ActionEvent event) {
    	shareDirChooser.setInitialDirectory(
    			Wazaa.getShareFolderPath().toAbsolutePath().
    			toFile().getParentFile());
    	File folder = shareDirChooser.showDialog(
    			startScreenBox.getScene().getWindow());
    	if (folder != null) {
    		shareFolderField.setText(folder.toString());
    		Wazaa.setShareFolder(folder);
    	}
    }
}
