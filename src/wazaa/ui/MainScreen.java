package wazaa.ui;

import java.net.URL;
import java.util.ResourceBundle;

import wazaa.Machine;
import wazaa.Wazaa;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

public class MainScreen implements Initializable {
    @FXML
    private TableView<?> fileSearchTable;

    @FXML
    private TableView<?> sharedFilesTable;

    @FXML
    private ListView<Machine> machinesList;
    
    @FXML
    private Label listeningStatusLabel;
    
    @FXML
    private Button addMachineButton;
    
    @FXML
    private Button addMachinesFromFileButton;

    @FXML
    private Button addMachinesFromURLButton;
    
    @FXML
    private Button removeSelectedMachinesButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		machinesList.setItems(
						FXCollections.observableList(Wazaa.getMachines()));
		addMachineButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				
			}
		});
	}

	public void setListeningStatusLabelText(String text) {
		listeningStatusLabel.setText(text);
	}
}
