package wazaa.ui;

import java.net.URL;
import java.util.ResourceBundle;

import wazaa.Machine;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

	}

}
