package wazaa.ui;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class StartScreen implements Initializable {
	@FXML
	private TextField portField;
	@FXML
	private Button startButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
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
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				try {
					Integer port = 
							Integer.valueOf(portField.getText());
					if (port >= 0 && port <= 65535) {
						HTTPServer srv = new HTTPServer(port);
						Wazaa.setHTTPServer(srv);
						ScenesController root = (ScenesController) 
								startButton.getScene().getRoot();
						((MainScreen)root.getController("main"))
							.setListeningStatusLabelText(portField.getText());
						root.switchToScene("main");
					} else {
						portField.requestFocus();
					}
				} catch (NumberFormatException e) {
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
		            p.show(portField.getScene().getWindow());
				} catch (IOException e) {
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
		            p.show(portField.getScene().getWindow());
				}
			}
		});
	}

}
