package wazaa.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import wazaa.Download;
import wazaa.FileIOUtil;
import wazaa.Machine;
import wazaa.Wazaa;
import wazaa.WazaaFile;
import wazaa.WazaaFoundFile;
import wazaa.http.HTTPUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;

public class MainScreen implements Initializable {
	@FXML
	private Label statusLabel;
	
    @FXML
    private Label listeningStatusLabel;

    @FXML
    private Button fileDownloadButton;
    private FileChooser downloadFC = new FileChooser();
    
    @FXML
    private TableView<WazaaFoundFile> fileSearchTable;

    @FXML
    private TableColumn<WazaaFoundFile, String> fileSearchNameCol;

    @FXML
    private TableColumn<WazaaFoundFile, String> fileSearchPeerCol;

    @FXML
    private TableColumn<WazaaFoundFile, String> fileSearchSizeCol;
    
    @FXML
    private TextField fileSearchField;

    @FXML
    private Button fileSearchButton;
    private String latestSearchUUID;

    @FXML
    private Button downloadsClearFinishedButton;

    @FXML
    private ListView<Download> downloadsList;

    @FXML
    private Button downloadsOpenSelectedButton;

    @FXML
    private TableView<WazaaFile> sharedFilesTable;

    @FXML
    private TableColumn<WazaaFile, String> sharedFilesFileNameCol;

    @FXML
    private TableColumn<WazaaFile, String> sharedFilesFileSizeCol;
    
    @FXML
    private Button sharedFilesShowShareFolderButton;

    @FXML
    private Button refreshSharedFilesButton;

    @FXML
    private ListView<Machine> machinesList;

    @FXML
    private TextField addMachineField;
    
    @FXML
    private Button addMachineButton;

    @FXML
    private TextField addMachinesFromURLField;
    
    @FXML
    private Button addMachinesFromURLButton;
    
    @FXML
    private Button addMachinesFromFileButton;
    private FileChooser machinesFC = new FileChooser();
    
    @FXML
    private Button removeSelectedMachinesButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// file search tab
		fileSearchNameCol.setCellValueFactory(
				new PropertyValueFactory<WazaaFoundFile, String>("fileName"));
		fileSearchPeerCol.setCellValueFactory(
				new PropertyValueFactory<WazaaFoundFile, String>("machine"));
		fileSearchField.textProperty().addListener(
				new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> obsValue,
							String oldValue, String newValue) {
						if (newValue.isEmpty()) {
							fileSearchButton.setDisable(true);
						} else {
							fileSearchButton.setDisable(false);
						}
					}
		});
		fileSearchTable.getSelectionModel().selectedItemProperty().
			addListener(
				new ChangeListener<WazaaFoundFile>() {
					@Override
					public void changed(
							ObservableValue<? extends WazaaFoundFile> observable,
							WazaaFoundFile oldValue, WazaaFoundFile newValue) {
						if (newValue == null) {
							fileDownloadButton.setDisable(true);
						} else {
							fileDownloadButton.setDisable(false);
						}
					}
		});
		downloadFC.setTitle("Choose save location");
		downloadFC.setInitialDirectory(Wazaa.getShareFilePath("").toFile());
		// downloads tab
		downloadsList.setItems(
				FXCollections.observableList(
						Wazaa.getDownloadManager().getDownloads()));
		// my files tab
		sharedFilesFileNameCol.setCellValueFactory(
				new PropertyValueFactory<WazaaFile, String>("fileName"));
		refreshSharedFilesButtonAction(null);
		// machines tab
		machinesFC.setInitialDirectory(Wazaa.getShareFilePath("").toFile());
    	machinesFC.setTitle("Open machines file");
    	machinesFC.getExtensionFilters().add(new ExtensionFilter(
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
		addMachineField.textProperty().addListener(
				new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> obsValue,
							String oldValue, String newValue) {
						if (newValue.isEmpty()) {
							addMachineButton.setDisable(true);
						} else {
							addMachineButton.setDisable(false);
						}
					}
		});

		addMachinesFromURLField.textProperty().addListener(
				new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> obsValue,
							String oldValue, String newValue) {
						if (newValue.isEmpty()) {
							addMachinesFromURLButton.setDisable(true);
						} else {
							addMachinesFromURLButton.setDisable(false);
						}
					}
		});
	}

    @FXML
    private void fileDownloadButtonAction(ActionEvent event) {
    	WazaaFoundFile selectedFile = fileSearchTable.
    			getSelectionModel().getSelectedItem();
    	if (selectedFile != null) {
			try {
				File saveLocation = downloadFC.showSaveDialog(
						fileDownloadButton.getScene().getWindow());
				if (saveLocation != null && !saveLocation.isDirectory()) {
					Wazaa.getDownloadManager().addDownload(
							HTTPUtil.sendGetFileReq(selectedFile),
							saveLocation);
				}
			} catch (IOException e) { 
				// TODO: error, unable to dl file
			}
    	}
    }
	
    @FXML
    private void fileSearchButtonAction(ActionEvent event) {
    	if (!fileSearchField.getText().isEmpty()) {
    		latestSearchUUID = 
    				Wazaa.searchForFile(fileSearchField.getText());
    		refreshFoundFiles();
    	}
    }

	public void refreshFoundFiles() {
		fileSearchTable.setItems(null);
		if (latestSearchUUID != null) {
			List<WazaaFoundFile> files = 
					Wazaa.getFoundFiles().getFilesList(
					latestSearchUUID);
			if (files != null) {
				fileSearchTable.setItems(
						FXCollections.observableArrayList(
								files));
			}
		}
	}

    @FXML
    private void downloadsClearFinishedButtonAction(ActionEvent event) {
    	List<Download> downloads = 
    			Wazaa.getDownloadManager().getDownloads();
    	synchronized (downloads) {
			Iterator<Download> iter = downloads.iterator();
			while (iter.hasNext()) {
				Download d = iter.next();
				if (d.isWritten()) {
					iter.remove();
				}
			}
		}
    	refreshDownloadsList();
    }

    @FXML
    private void downloadsOpenSelectedButtonAction(ActionEvent event) {
    	if (downloadsList.getSelectionModel().getSelectedItem() != null) {
	    	try {
				Desktop.getDesktop().browse(
						downloadsList.getSelectionModel().getSelectedItem().
						getSaveLocation().toURI());
			} catch (InvalidPathException | IOException e) {
				Popup p = new Popup();
				VBox box = new VBox();
				Label l = new Label("File not yet written to disk.");
				box.getChildren().add(l);
	            box.setStyle("-fx-background-color: #FFD0A0;"
	            		+ "-fx-border-color: #000000;"
	            		+ "-fx-border-width: 1;");
	            box.setPadding(new Insets(5));
	            p.getContent().add(box);
	            p.setAutoHide(true);
	            p.show(downloadsOpenSelectedButton.getScene().getWindow());
			}
    	}
    }

	public synchronized void refreshDownloadsList() {
		ObservableList<Download> list = downloadsList.getItems();
		downloadsList.setItems(null);
		downloadsList.setItems(list);
	}

    @FXML
    private void refreshSharedFilesButtonAction(ActionEvent event) {
    	sharedFilesTable.setItems(
				FXCollections.observableList(FileIOUtil.findFiles("")));
    }
    
    @FXML
    private void sharedFilesShowShareFolderButtonAction(
    		ActionEvent event) {
    	try {
			Desktop.getDesktop().browse(
							Wazaa.getShareFilePath("").toUri());
		} catch (InvalidPathException | IOException e) {
			final Popup p = new Popup();
			VBox vBox = new VBox();
			vBox.getChildren().add(new Label("Unable to open folder!"));
			Button popupCloseButton = new Button("Ok");
			popupCloseButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					p.hide();
				}
			});
			vBox.getChildren().add(popupCloseButton);
			p.getContent().add(vBox);
			p.show(sharedFilesShowShareFolderButton.getScene().getWindow());
		}
    }
	
    @FXML
    private void addMachineButtonAction(ActionEvent event) {
    	if (!addMachineField.getText().isEmpty()) {
    		System.out.println("HERE");
    		String[] parts = addMachineField.getText().split(":");
    		System.out.println("HERE2");
    		if (!(parts.length == 2 && 
    				Wazaa.addMachine(parts[0], parts[1]))) {
    			// TODO: add error popup
    		}
    		System.out.println("HERE3");
    	}
    }

    @FXML
    private void addMachinesFromFileButtonAction(ActionEvent event) {
    	File file = machinesFC.showOpenDialog(
    			addMachinesFromFileButton.getScene().getWindow());
    	if (file != null) {
    		int count = Wazaa.getMachinesFromFile(file.getAbsolutePath());
    		// TODO: use count
    	}
    }

    @FXML
    private void addMachinesFromURLButtonAction(ActionEvent event) {
    	if (!addMachinesFromURLField.getText().isEmpty()) {
    		try {
				URL u = new URL(addMachinesFromURLField.getText());
				int count = Wazaa.getMachinesFromURL(u);
				if (count == 0) {
					// TODO: add error popup
				}
			} catch (MalformedURLException e) {
				// TODO: add error popup
			}
    	}
    }

    @FXML
    private void removeSelectedMachinesButtonAction(ActionEvent event) {
		Wazaa.getMachines()
			.removeAll(machinesList.getSelectionModel().getSelectedItems());
    }

	public synchronized void refreshMachinesList() {
		ObservableList<Machine> list = machinesList.getItems();
		machinesList.setItems(null);
		machinesList.setItems(list);
	}
	
	public void setListeningStatusLabelText(String text) {
		listeningStatusLabel.setText(text);
	}
}
