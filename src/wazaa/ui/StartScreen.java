package wazaa.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.FileChooser.ExtensionFilter;

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
	
    @FXML
    private Button machinesFileButton;
    private FileChooser machinesFC = new FileChooser();

    @FXML
    private CheckBox machinesFileCheckBox;

    @FXML
    private TextField machinesFileField;
    private File initialMachinesFile;

    @FXML
    private CheckBox machinesURLCheckBox;

    @FXML
    private TextField machinesURLField;
    private URL initialMachinesURL;
	
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
		initialMachinesFile = new File(Wazaa.DEFAULTMACHINESFILE);
		if (initialMachinesFile.exists()) {
			machinesFileCheckBox.setSelected(true);
			machinesFileField.setText(
					initialMachinesFile.getAbsolutePath());
		}
    	machinesFC.setTitle("Open machines file");
		machinesFC.setInitialDirectory(
				Paths.get("").toAbsolutePath().toFile());
    	machinesFC.getExtensionFilters().add(new ExtensionFilter(
    			"JSON", "*.json", "*.txt"));
    	machinesURLField.textProperty().addListener(
    			new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						machinesURLCheckBox.setSelected(false);
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
					
					if (machinesFileCheckBox.isSelected() &&
							initialMachinesFile != null &&
							initialMachinesFile.exists()) {
						Wazaa.getMachinesFromFile(
								initialMachinesFile.
								getAbsolutePath().toString());
					}
					if (machinesURLCheckBox.isSelected() &&
							initialMachinesURL != null) {
						Wazaa.getMachinesFromURL(initialMachinesURL);
					}
					
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
						Label l = new Label(
								"Unable to use specified share folder.");
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
    
    @FXML
    private void machinesFileButtonAction(ActionEvent event) {
    	File file = machinesFC.showOpenDialog(
    			machinesFileButton.getScene().getWindow());
    	if (file != null) {
    		initialMachinesFile = file;
    		machinesFileField.setText(file.getAbsolutePath());
    	}
    }

    @FXML
    private void machinesFileCheckBoxAction(ActionEvent event) {
    	if (initialMachinesFile == null) {
    		machinesFileCheckBox.setSelected(false);
    	}
    }

    @FXML
    private void machinesURLCheckBoxAction(ActionEvent event) {
    	try {
    		initialMachinesURL = new URL(machinesURLField.getText());
    	} catch (MalformedURLException e) {
            machinesURLCheckBox.setSelected(false);
			machinesURLField.requestFocus();
			Popup p = new Popup();
			VBox box = new VBox();
			Label l = new Label("Invalid URL");
			box.getChildren().add(l);
            box.setStyle("-fx-background-color: #FFD0A0;"
            		+ "-fx-border-color: #000000;"
            		+ "-fx-border-width: 1;");
            box.setPadding(new Insets(5));
            p.getContent().add(box);
            p.setAutoHide(true);
            p.setHideOnEscape(true);
            p.show(machinesURLField.getScene().getWindow());
    	}
    }
}
