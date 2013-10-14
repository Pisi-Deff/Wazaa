package wazaa.ui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import wazaa.FileIOUtil;
import wazaa.Machine;
import wazaa.Wazaa;
import wazaa.WazaaFile;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainScreen implements Initializable {
	@FXML
	private Label statusLabel;
	
	@FXML
	private Label machinesStatusLabel;
	
    @FXML
    private Label listeningStatusLabel;
    
    @FXML
    private TableView<?> fileSearchTable;

    @FXML
    private Button fileSearchButton;

    @FXML
    private TableView<WazaaFile> sharedFilesTable;

    @FXML
    private TableColumn<WazaaFile, String> sharedFilesFileNameCol;

    @FXML
    private TableColumn<WazaaFile, String> sharedFilesFileSizeCol;

    @FXML
    private Button refreshSharedFilesButton;

    @FXML
    private ListView<Machine> machinesList;
    
    @FXML
    private Button addMachineButton;
    
    @FXML
    private Button addMachinesFromFileButton;
    private FileChooser fc = new FileChooser();
    
    @FXML
    private Button addMachinesFromURLButton;
    
    @FXML
    private Button removeSelectedMachinesButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// my files tab
		sharedFilesFileNameCol.setCellValueFactory(
				new PropertyValueFactory<WazaaFile, String>("fileName"));
		refreshSharedFilesButtonAction(null);
		// machines tab
    	fc.setTitle("Open machines file");
    	fc.getExtensionFilters().add(new ExtensionFilter(
    			"JSON", "*.json", "*.txt"));
		
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
		machinesStatusLabel.setText("");
	}
	
    @FXML
    private void fileSearchButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void refreshSharedFilesButtonAction(ActionEvent event) {
    	sharedFilesTable.setItems(
				FXCollections.observableList(FileIOUtil.findFiles("")));
    }
	
    @FXML
    private void addMachineButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void addMachinesFromFileButtonAction(ActionEvent event) {
    	File file = fc.showOpenDialog(
    			addMachinesFromFileButton.getScene().getWindow());
    	if (file != null) {
    		int count = Wazaa.getMachinesFromFile(file.getAbsolutePath());
    		refreshMachinesList();
    		machinesStatusLabel.setText("Added " + count + " machines.");
    	}
    }

    @FXML
    private void addMachinesFromURLButtonAction(ActionEvent event) {
    	
    }

    @FXML
    private void removeSelectedMachinesButtonAction(ActionEvent event) {
		Wazaa.getMachines()
			.removeAll(machinesList.getSelectionModel().getSelectedItems());
		refreshMachinesList();
    }

	private void refreshMachinesList() {
		ObservableList<Machine> list = machinesList.getItems();
		machinesList.setItems(null);
		machinesList.setItems(list);
	}

	public void setListeningStatusLabelText(String text) {
		listeningStatusLabel.setText(text);
	}
}
