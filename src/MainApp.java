

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import view.ConnexionControl;

public class MainApp extends Application {

	private Stage stagePrincipal;
	private AnchorPane PremiereFen;
	private ConnexionControl connexion;
	
	/**
	 * Lance la fenêtre
	 */
	@Override
	public void start(Stage primaryStage) {
		stagePrincipal = primaryStage;
		stagePrincipal.setTitle("Détection intelligente");
		//défini un mot de passe par défault
		/*
		try {
			Password.newPassWord("test");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}*/
		initialisationPremiereFen(stagePrincipal);
	}
	
	/**
	 * Initialise la première fenêtre
	 * @param stagePrincipal
	 */
	private void initialisationPremiereFen(Stage stagePrincipal) {
		FXMLLoader loader = new FXMLLoader();
		
		loader.setLocation(MainApp.class.getResource("view/connexion.fxml"));
		try {
			PremiereFen = (AnchorPane) loader.load();
			Scene scene1 = new Scene(PremiereFen);
			stagePrincipal.setScene(scene1);
			stagePrincipal.show();
			connexion = loader.getController();
			connexion.init(stagePrincipal);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java349");
		launch(args);
	}
}
