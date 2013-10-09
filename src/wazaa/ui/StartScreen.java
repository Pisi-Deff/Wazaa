package wazaa.ui;

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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

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
					if (port <= 65535) {
						Wazaa.setHTTPServer(new HTTPServer(port));
						ScenesController root = (ScenesController) 
								startButton.getScene().getRoot();
						((MainScreen)root.getController("main"))
							.setListeningStatusLabelText(portField.getText());
						root.switchToScene("main");
					}
				} catch (NumberFormatException e) {
					// Invalid port
				}
			}
		});
	}

}
