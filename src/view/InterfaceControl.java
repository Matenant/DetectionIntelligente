package view;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Password;
import model.Utils;

public class InterfaceControl {
	/**
	 * Bouton démarrer la caméra
	 */
	@FXML
	private Button demarrer;
	/**
	 * Contient l'image actuelle de la caméra
	 */
	@FXML
	private ImageView currentImage;
	/**
	 * TextField de la distance minimal entre le capteur et la personne
	 */
	@FXML
	private TextField distanceValeur;
	/**
	 * TextField de la taille minimal de la personne
	 */
	@FXML
	private TextField tailleValeur;
	/**
	 * TextField de la nouvelle valeur du mot de passe
	 */
	@FXML
	private TextField mdpValeur;
	/**
	 * Contient des informations pour l'utilisateur concernant les champs 
	 */
	@FXML
	private Label labelChamps;
	/**
	 * Contient des informations pour l'utilisateur concernant le mot de passe 
	 */
	@FXML
	private Label labelMdp;
	/**
	 * Label pour les erreurs lié à la caméra
	 */
	@FXML
	private Label erreurMdp;
	/**
	 * indique si la camera est active ou non
	 */
	private boolean cameraActive;
	
	/**
	 * Variable de la camera
	 */
	private VideoCapture capture;
	/**
	 * Variable qui permet de gérer le temps
	 */
	private ScheduledExecutorService timer;
	/**
	 * Variable du model détection tête de face
	 */
	private CascadeClassifier faceCascade;
	/**
	 * Variable du model détection tête de côté
	 */
	private CascadeClassifier profileCascade;
	/**
	 * Variable de la taille minimal de la tête
	 */
	private int absoluteFaceSize;
	
	/**
	 * Valeur de base de la distance minimal
	 */
	private int distanceInt = 100;
	/**
	 * Valeur de base de la taille minimal
	 */
	private int tailleInt = 100;
	/**
	 * Variable contenant le nouveaux mot de passe
	 */
	private String mdp;
	
	/**
	 * Initialise différente valeur comme, la caméra, le model, les textfields
	 */
	public void init() {
		capture = new VideoCapture();
		faceCascade = new CascadeClassifier();
		profileCascade = new CascadeClassifier();
		faceCascade.load("ressources/haarcascade_frontalface_alt.xml");
		profileCascade.load("ressources/haarcascade_profileface.xml");
		absoluteFaceSize = 0;
		distanceValeur.setText(""+distanceInt);
		tailleValeur.setText(""+tailleInt);
	}
	
	
	/**
	 * Permet de récupérer les champs distance et taille quand on appuie sur valider
	 */
	@FXML
	public void recupChamp() {
		try {
			distanceInt = Integer.parseInt(distanceValeur.getText());
			System.out.println("Distance = "+distanceInt);
			tailleInt = Integer.parseInt(tailleValeur.getText());
			System.out.println("Taille = "+tailleInt);
			labelChamps.setText("");
		}
		catch(NumberFormatException e) {
			labelChamps.setText("Erreur dans les nombres");
		}	
	}
	
	/**
	 * Permet de récupérer le nouveau mot de passe quand on appuie sur valider
	 */
	@FXML
	public void recupMdp() {
		
		if(!mdpValeur.getText().isEmpty() && mdpValeur.getText() != null) {
			mdp = mdpValeur.getText();
			labelMdp.setText("");
			System.out.println("Mot de passe = "+mdp);
			try {
				Password.newPassWord(mdp);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		else {
			labelMdp.setText("Mot de passe vide");
		}
		
		
		mdpValeur.clear();
	}
	
	/**
	 * Lance la caméra et met à jour l'écran
	 */
	@FXML
	protected void startCamera() {
		if (!this.cameraActive)
		{
			this.capture.open(0);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				erreurMdp.setText("Caméra Connecté");
				this.cameraActive = true;
				
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentImage, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				//change the 33 for more image per second
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.demarrer.setText("Stop Camera");
			}
			else
			{
				// log the error
				erreurMdp.setText("Erreur de connectivité avec la caméra");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.demarrer.setText("Start Camera");
			
			// stop the timer
			this.stopAcquisition();
		}
	}
	
	/**
	 * Permet de récupérer une image dans le flux vidéo
	 * @return une image de la caméra
	 */
	private Mat grabFrame()
	{
		Mat frame = new Mat();
		if (this.capture.isOpened())
		{
			try
			{
				this.capture.read(frame);
				if (!frame.empty())
				{
					// face detection
					detectAndDisplay(frame);
				}
			}
			catch (Exception e)
			{
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		return frame;
	}
	
	/**
	 * Permet de détecter les visages et affiche les rectangles à l'écran
	 * @param frame
	 */
	private void detectAndDisplay(Mat frame) {
		MatOfRect frontal = new MatOfRect();
		MatOfRect profile = new MatOfRect();
		Mat grayFrame = new Mat();
		
		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		// compute minimum face size (20% of the frame height, in our case)
		if (absoluteFaceSize == 0)
		{
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0)
			{
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, frontal, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
		this.profileCascade.detectMultiScale(grayFrame, profile, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
		
		// each rectangle in faces is a face
		Rect[] frontalArray = frontal.toArray();
		for (int i = 0; i < frontalArray.length; i++) {
			Imgproc.rectangle(frame, frontalArray[i].tl(), frontalArray[i].br(), new Scalar(0, 255, 0), 3);
			detectLeftCenterRight(frontalArray[i], frame);
			detectUpCenterDown(frontalArray[i], frame);
		}
		if(frontalArray.length == 0) {
			Rect[] profileArray = profile.toArray();
			for (int i = 0; i < profileArray.length; i++) {
				Imgproc.rectangle(frame, profileArray[i].tl(), profileArray[i].br(), new Scalar(255, 0, 0), 3);
				detectLeftCenterRight(profileArray[i], frame);
				detectUpCenterDown(profileArray[i], frame);
			}
		}
	}
	
	/**
	 * Indique si la tête se situe, à gauche, au centre, ou à droite de l'écran
	 * @param face
	 * @param frame
	 */
	private void detectLeftCenterRight(Rect face, Mat frame) {
		int centreFace = face.x + (face.width / 2);
		int centre = frame.width()/2;
		if(centreFace <= centre-75) {
			System.out.println("Tourne gauche");
		}
		else if(centreFace >= centre+75) {
			System.out.println("Tourne droite");
		}
		else {
			System.out.println("Déjà au centre");
		}
	}
	
	/**
	 * Indique si la tête se situe, en haut, au centre, ou en bas de l'écran
	 * @param face
	 * @param frame
	 */
	private void detectUpCenterDown(Rect face, Mat frame) {
		int centreFace = face.y + (face.height / 2);
		int centre = frame.height()/2;
		if(centreFace <= centre-75) {
			System.out.println("Tourne haut");
		}
		else if(centreFace >= centre+75) {
			System.out.println("Tourne bas");
		}
		else {
			System.out.println("Déjà au centre");
		}
	}
	
	/**
	 * Permet de mettre à jour l'écran
	 * @param view
	 * @param image
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * Permet d'arrêter la capture vidéo
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	
	/**
	 * Ferme la fenêtre
	 */
	public void setClosed()
	{
		this.stopAcquisition();
	}
	
}
