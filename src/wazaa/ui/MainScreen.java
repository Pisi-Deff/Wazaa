package wazaa.ui;

import java.net.URL;
import java.util.ResourceBundle;

import wazaa.Machine;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

public class MainScreen implements Initializable {
    @FXML
    private Button addMachineButton;

    @FXML
    private TableView<?> fileSearchTable;

    @FXML
    private TableView<?> sharedFilesTable;

    @FXML
    private ListView<Machine> MachinesList;
    
    @FXML
    private Label listeningStatusLabel;
    
    @FXML
    private Button addMachinesFromFileButton;

    @FXML
    private Button addMachinesFromURLButton;
    
    @FXML
    private Button removeSelectedMachinesButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}

	public void setListeningStatusLabelText(String text) {
		listeningStatusLabel.setText(text);
	}
}
