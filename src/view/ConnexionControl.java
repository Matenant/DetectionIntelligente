package view;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Password;

public class ConnexionControl {
	
	/**
	 * Texte qui affiche les erreurs li� au mot de passe
	 */
	@FXML
	private Label erreurMDP;
	
	/**
	 * Saisie du mot de passe 
	 */
	@FXML
	private TextField mdp;
	
	/**
	 * Fen�tre interface
	 */
	private AnchorPane SecondeFen;
	/**
	 * Sc�ne contenant la deuxi�me fen�tre
	 */
	private Scene scene2;
	/**
	 * Fen�tre enti�re
	 */
	private Stage stagePrincipal;
	
	/**
	 * Initialise la fen�tre
	 * @param stagePrincipal
	 */
	public void init(Stage stagePrincipal) {
		this.stagePrincipal = stagePrincipal;
		initialisationSecondeFen();
	}
	
	/**
	 * Initialise la deuxi�me fen�tre
	 */
	private void initialisationSecondeFen() {
		FXMLLoader loader = new FXMLLoader();
		
		loader.setLocation(ConnexionControl.class.getResource("user.fxml"));
		try {
			SecondeFen = (AnchorPane) loader.load();
			scene2 = new Scene(SecondeFen);
			InterfaceControl inter = loader.getController();
			inter.init();
			
			stagePrincipal.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					inter.setClosed();
				}
			}));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * R�cup�re le mot de passe et v�rifie sa validit�
	 */
	@FXML
	public void recupMdp() {

		byte[] pass;
		File f = new File("passWord");
		
		byte[] encodedhash;
		MessageDigest digest;
		String password;
		boolean memeMDP = true;
		
		if(!mdp.getText().isEmpty() && mdp.getText() != null) {
			password = mdp.getText();
			erreurMDP.setText("");
			
			try {
				pass = Password.getPassWord(f);
				
				digest = MessageDigest.getInstance("SHA-256");
				encodedhash = digest.digest(password.getBytes());
				
				int i = 0;
				do {
					if(pass[i] != encodedhash[i]) {
						erreurMDP.setText("Erreur dans le mot de passe");
						memeMDP = false;
						break;
					}
					i++;
				} while( (i < pass.length) && (i < encodedhash.length) && (pass[i] == encodedhash[i]));
				if(memeMDP) {
					stagePrincipal.setScene(scene2);
					stagePrincipal.show();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			
		}
		else {
			erreurMDP.setText("Mot de passe vide");
		}
	}
}
