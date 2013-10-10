package wazaa.ui;

import java.net.URL;
import java.util.ResourceBundle;

import wazaa.Machine;
import wazaa.Wazaa;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
		machinesList.getSelectionModel().
			setSelectionMode(SelectionMode.MULTIPLE);
		machinesList.setItems(
						FXCollections.observableList(Wazaa.getMachines()));
		machinesList.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Machine>() {
					@Override
					public void changed(
							ObservableValue<? extends Machine> observable,
							Machine oldValue, Machine newValue) {
						if (newValue == null) {
							removeSelectedMachinesButton.setDisable(true);
						} else {
							removeSelectedMachinesButton.setDisable(false);
						}
					}
				});
	}
	
    @FXML
    private void addMachineButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void addMachinesFromFileButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void addMachinesFromURLButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void removeSelectedMachinesButtonAction(ActionEvent event) {
		Wazaa.getMachines()
			.removeAll(machinesList.getSelectionModel().getSelectedItems());
		ObservableList<Machine> list = machinesList.getItems();
		machinesList.setItems(null);
		machinesList.setItems(list);
    }

	public void setListeningStatusLabelText(String text) {
		listeningStatusLabel.setText(text);
	}
}
