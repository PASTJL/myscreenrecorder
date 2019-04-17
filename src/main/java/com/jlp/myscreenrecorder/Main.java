/*Copyright [2019] [Jean-Louis PASTUREL]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package com.jlp.myscreenrecorder;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application implements NativeKeyListener {
	public static String rootProject = ".";
	public static String outputDir = ".";
	public static Button butRec = new Button("Record");
	public static Button butStop = new Button("Stop");
	public static ChoiceBox<String> choiceBox = new ChoiceBox<String>();
	public static ChoiceBox<String> audioChoiceBox = new ChoiceBox<String>();
	public final static TextField tfOutput = new TextField();
	public static Label lbOutput = new Label("Output Dir");
	public static URL STYLECSS;
	public static Button butBrowse = new Button("Browse");
	public static String testW10NVIDIA = "driverquery";
	public static Properties propsConfig = new Properties();
	public static boolean isWin = false;
	public static boolean withNvidia = false;
	public static boolean withRectangle = false; // => false means full screen capture
	public static String keyAudio;
	public static OutputStream osStop = null;
	public static boolean choiceRec = false;;
	public static MyMouseTracker msTrack = null;
	public static String strCmd;
	public static ProcessBuilder pb;
	public static Process p;
	public static Thread myThread;
	public static String strModifStop1;
	public static String strModifStop2;
	public static String strLettreStop;
	public static int intModifStop1;
	public static int intModifStop2;
	public static int intLettreStop;
	public static long delaiStopMs = 100000;
	public static MapIntegerLong mapIntegerLong = null;
	public static Dimension dimFullScreen = Toolkit.getDefaultToolkit().getScreenSize();
	public static double screenWidth = 1920;
	public static double screenHeigh = 1080;
	public static String outFileOnly = "capture-";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		choiceBox.getItems().add("FullScreen");
		choiceBox.getItems().add("Rectangle");
		choiceBox.setId("video");
		rootProject = System.getProperty("root", ".");
		outputDir = System.getProperty("outputDir", ".");
		System.out.println("root=" + rootProject);
		screenWidth = dimFullScreen.getWidth();
		screenHeigh = dimFullScreen.getHeight();
		try (FileInputStream input = new FileInputStream(
				rootProject + File.separator + "config" + File.separator + "config.properties");) {

			propsConfig.load(input);

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		updateNativeKeyevent();

		try {
			STYLECSS = new File(rootProject + File.separator + "config" + File.separator + "style.css").toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Si Windows Test nvidia
		StringBuffer strBuf = new StringBuffer();
		if (isWindows()) {

			try {
				 pb = new ProcessBuilder(testW10NVIDIA);
				pb.redirectErrorStream(true);
				 p = pb.start();

				InputStream processOutput = p.getInputStream();
				try {
					p.getOutputStream().close(); // fermeture du flux stdin inutilisé

					// lecture du flux par bloc de 512 bytes :
					byte[] b = new byte[10240];
					int len;

					while ((len = processOutput.read(b)) > 0) {
						strBuf.append(new String(b, 0, len));
						// System.out.write(b,0,len);

					}

					p.waitFor();
				} finally {
					processOutput.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (strBuf.toString().toLowerCase().contains("nvidia")) {
				if (propsConfig.containsKey("win_full_hw") && propsConfig.containsKey("win_rect_hw")) {
					System.out.println("Windows carte nvidia detected, recording with Hardware Encoding");
					// System.out.println("strBuf.toString()="+strBuf.toString());

					withNvidia = true;
				} else {
					withNvidia = false;
					System.out.println("Windows no suitable carte nvidia detected, recording with software Encoding");

				}

			} else {
				withNvidia = false;
				System.out.println("Windows no carte nvidia detected, recording with software Encoding");

			}
		}
		launch(args);
	}

	public static Stage stage;

	private static void updateNativeKeyevent() {
		// Constituion de la chaine d'arret
		String allKeys = propsConfig.getProperty("keyStopRec", "ALT_CONTROL_F");
		delaiStopMs = Long.valueOf(propsConfig.getProperty("delayStopMs", "1000"));
		strModifStop1 = allKeys.split("_")[0];
		strModifStop2 = allKeys.split("_")[1];
		strLettreStop = allKeys.split("_")[2];

		switch (strModifStop1) {
		case "ALT":
			intModifStop1 = NativeKeyEvent.VC_ALT;
			break;
		case "CONTROL":
			intModifStop1 = NativeKeyEvent.VC_CONTROL;
			break;
		case "SHIFT":
			intModifStop1 = NativeKeyEvent.VC_SHIFT;
			break;

		}
		switch (strModifStop2) {
		case "ALT":
			intModifStop2 = NativeKeyEvent.VC_ALT;
			break;
		case "CONTROL":
			intModifStop2 = NativeKeyEvent.VC_CONTROL;
			break;
		case "SHIFT":
			intModifStop2 = NativeKeyEvent.VC_SHIFT;
			break;

		}
		switch (strLettreStop) {
		case "A":
			intLettreStop = NativeKeyEvent.VC_A;
			break;
		case "B":
			intLettreStop = NativeKeyEvent.VC_B;
			break;
		case "C":
			intLettreStop = NativeKeyEvent.VC_C;
			break;
		case "D":
			intLettreStop = NativeKeyEvent.VC_D;
			break;
		case "E":
			intLettreStop = NativeKeyEvent.VC_E;
			break;
		case "F":
			intLettreStop = NativeKeyEvent.VC_F;
			break;
		case "G":
			intLettreStop = NativeKeyEvent.VC_G;
			break;
		case "H":
			intLettreStop = NativeKeyEvent.VC_H;
			break;
		case "I":
			intLettreStop = NativeKeyEvent.VC_I;
			break;
		case "J":
			intLettreStop = NativeKeyEvent.VC_J;
			break;
		case "K":
			intLettreStop = NativeKeyEvent.VC_K;
			break;
		case "L":
			intLettreStop = NativeKeyEvent.VC_L;
			break;
		case "M":
			intLettreStop = NativeKeyEvent.VC_M;
			break;
		case "N":
			intLettreStop = NativeKeyEvent.VC_N;
			break;
		case "O":
			intLettreStop = NativeKeyEvent.VC_O;
			break;
		case "P":
			intLettreStop = NativeKeyEvent.VC_P;
			break;
		case "Q":
			intLettreStop = NativeKeyEvent.VC_Q;
			break;
		case "R":
			intLettreStop = NativeKeyEvent.VC_R;
			break;
		case "S":
			intLettreStop = NativeKeyEvent.VC_S;
			break;
		case "T":
			intLettreStop = NativeKeyEvent.VC_T;
			break;
		case "U":
			intLettreStop = NativeKeyEvent.VC_U;
			break;
		case "V":
			intLettreStop = NativeKeyEvent.VC_V;
			break;
		case "W":
			intLettreStop = NativeKeyEvent.VC_W;
			break;
		case "X":
			intLettreStop = NativeKeyEvent.VC_X;
			break;
		case "Y":
			intLettreStop = NativeKeyEvent.VC_Y;
			break;
		case "Z":
			intLettreStop = NativeKeyEvent.VC_Z;
			break;

		}
		mapIntegerLong = new MapIntegerLong();
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		stage = primaryStage;
		primaryStage.setTitle("MyScreenRecorder by JLP;Version 2019-04 ");
		BorderPane root = new BorderPane();
		HBox hbox = new HBox();
		HBox hbox2 = new HBox();
		HBox hbox3 = new HBox();
		root.setTop(hbox);
		root.setCenter(hbox3);
		root.setBottom(hbox2);
		hbox3.setPadding(new Insets(10, 0, 0, 40));
		hbox2.getChildren().add(lbOutput);
		Label labStop = new Label("To stop recording by keyboard : " + propsConfig.getProperty("keyStopRec"));
		labStop.setId("labStop");
		hbox3.getChildren().add(labStop);
		tfOutput.setText(outputDir);
		tfOutput.setTooltip(new Tooltip("File name auto  : output-AA-mm-dd-HH-MM-ss.mkv"));
		hbox2.getChildren().add(tfOutput);
		hbox2.getChildren().add(butBrowse);
		Scene scene = new Scene(root, 500, 100);
		primaryStage.setScene(scene);
		root.getStylesheets().add(Main.STYLECSS.toExternalForm());

		scene.getStylesheets().add(Main.STYLECSS.toExternalForm());
		hbox.setSpacing(20);
		hbox2.setSpacing(20);
		hbox.getChildren().add(butRec);
		hbox.getChildren().add(butStop);
		hbox.getChildren().add(choiceBox);
		Label lbAudio = new Label("Audio");
		String[] listAudio = null;
		if (isWin) {
			listAudio = propsConfig.getProperty("win_list_audio").split(";");
		} else if (isLinux()) {
			listAudio = propsConfig.getProperty("lin_list_audio").split(";");
		}
		for (int i = 0; i < listAudio.length; i++) {
			audioChoiceBox.getItems().add(listAudio[i]);
		}
		hbox.getChildren().add(lbAudio);
		hbox.getChildren().add(audioChoiceBox);
		audioChoiceBox.getSelectionModel().selectFirst();
		audioChoiceBox.setId("audio");
		butStop.setDisable(true);
		choiceBox.getSelectionModel().select(0);
		hbox2.setPadding(new Insets(10, 0, 10, 10));

		primaryStage.setAlwaysOnTop(true);
		primaryStage.show();
		butBrowse.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Choose an output directory");
				directoryChooser.setInitialDirectory(new File(outputDir).getParentFile());
				File selectedFile = directoryChooser.showDialog(primaryStage);
				if (selectedFile != null) {
					tfOutput.setText(selectedFile.getAbsolutePath());
					System.out.println("File" + selectedFile.getAbsolutePath());

				}
			}
		});
		choiceBox.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				if (choiceBox.getSelectionModel().getSelectedItem().equalsIgnoreCase("rectangle")) {
					System.out.println("Choix de Rectangle");
					withRectangle = true;
				} else {
					System.out.println("Choix de Full Screen");
					withRectangle = false;
				}

			}
		});

		butRec.setOnAction(new EventHandler<ActionEvent>() {
			String keyCmd;

			public void handle(ActionEvent event) {

				String audioChoose = audioChoiceBox.getSelectionModel().getSelectedItem();

				butStop.setDisable(false);
				butRec.setDisable(true);
				// Choix de la ligne de commande ffmpeg

				if (isWin) {
					// hard or soft ?
					if (withNvidia) {
						if (withRectangle) {
							keyCmd = "win_rect_hw";
							if (null != propsConfig.getProperty(keyCmd, null)) {
								// hardware acceleration
								strCmd = propsConfig.getProperty(keyCmd);
							} else {
								// use soft
								strCmd = propsConfig.getProperty("win_rect");
							}

						} else {
							keyCmd = "win_full_hw";
							if (null != propsConfig.getProperty(keyCmd, null)) {
								// hardware acceleration
								strCmd = propsConfig.getProperty(keyCmd);
							} else {
								// use soft
								strCmd = propsConfig.getProperty("win_full");
							}
						}

					} else {
						// soft
						if (withRectangle) {
							strCmd = propsConfig.getProperty("win_rect");
						} else {
							strCmd = propsConfig.getProperty("win_full");
						}
					}
				} else if (isLinux()) {
					if (withRectangle) {
						strCmd = propsConfig.getProperty("lin_rect");
					} else {
						// Evaluate sizevideo for fullscreen

						strCmd = propsConfig.getProperty("lin_full");
					}
				}
				// System.out.println("ffmeg To parse =>" + strCmd);

				// traitement ligne de commande
				// path to ffmpeg et fichier de sortie
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				Date date = new Date();
				String strDate = format.format(date);
				outFileOnly = outFileOnly + strDate + ".mkv";
				if (strCmd.indexOf("<win_pathffmpeg>") >= 0) {
					strCmd = strCmd.replace("<win_pathffmpeg>", System.getProperty("pathffmpeg"));

					strCmd = strCmd.replace("<file_out>",
							System.getProperty("outputDir") + "\\capture-" + strDate + ".mkv");
				} else if (strCmd.indexOf("<lin_pathffmpeg>") >= 0) {
					strCmd = strCmd.replace("<lin_pathffmpeg>", System.getProperty("pathffmpeg"));
					strCmd = strCmd.replace("<file_out>",
							System.getProperty("outputDir") + "/capture-" + strDate + ".mkv");
				}
				System.out.println("ffmeg To parse apres path ffmpeg et fileoutput=>" + strCmd);
				switch (audioChoose) {
				case "None":
					// no sound
					if (isWin) {
						strCmd = strCmd.replaceAll("<-f dshow.+?>>", "");
					} else if (isLinux()) {
						strCmd = strCmd.replaceAll("<-f alsa.+?lin_dev>>", "");
					}
					System.out.println("ffmeg To parse apres path ffmpeg et fileoutput et audio None=>" + strCmd);
					break;
				default:
					if (isWin) {
						strCmd = strCmd.replace("<-f dshow", "-f dshow").replace(">>", ">");
						strCmd = strCmd.replace("<win-dev double quoted>",
								propsConfig.getProperty("win_dev" + audioChoose));
					} else if (isLinux()) {
						strCmd = strCmd.replaceAll("<-f alsa", "-f alsa").replace(">>", ">");
						strCmd = strCmd.replace("<lin_dev>", propsConfig.getProperty("lin_dev" + audioChoose));
					}
					// System.out.println("ffmeg To parse apres path ffmpeg et fileoutput et With
					// audio=>" + strCmd);
					break;

				}
				if (withRectangle) {
					choiceRec = true;
					lancerWithRectangle(strCmd, primaryStage);
					primaryStage.show();

					// System.out.println("Enregistrement a partir de la");
				} else {
					lancerFullScreen(strCmd);
				}
			}

		});
		butStop.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				if (withRectangle) {
					try {

						if (null != osStop) {
							osStop.write('q');
							osStop.write('\n');

							osStop.flush();
							osStop.close();
							System.out.println("Arret Enregistrement");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					osStop=null;
				}
				stopRec(primaryStage);

			}

		});

		addListeners();
		primaryStage.setOnCloseRequest(e -> {
			stopRec(primaryStage);
			System.out.println("Exiting");

			Platform.exit();
			System.exit(0);
		});
	}

	private void addListeners() {

		// TODO Auto-generated method stub
		// System.out.println("Adding Listeners");
		// Get the logger for "org.jnativehook" and set the level to off.
		java.util.logging.Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());

		logger.setLevel(Level.OFF);

		// Change the level for all handlers attached to the default logger.
		Handler[] handlers = Logger.getLogger("").getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			handlers[i].setLevel(Level.OFF);
		}

		try {
			GlobalScreen.registerNativeHook();

			GlobalScreen.addNativeKeyListener(this);

		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

	}

	protected void lancerFullScreen(String strCmd) {
		if (isWin) {
			String[] args = { "cmd.exe", "/c", strCmd };
			try {
				pb = new ProcessBuilder(args);
				pb = pb.redirectErrorStream(true); // on mélange les sorties du processus
				p = pb.start();
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				osStop = p.getOutputStream();

			} catch (IOException e) {

			}
		} else if (isLinux()) {
			// Il faut donner la taille de l ecran
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			// System.out.println(" Avant screensize Linux / full screen cmd="+ strCmd);
			strCmd = strCmd.replace("<screensize>",
					((Integer) ((Double) primaryScreenBounds.getWidth()).intValue()).toString() + "x"
							+ ((Integer) ((Double) primaryScreenBounds.getHeight()).intValue()).toString());
			String[] args = { "/bin/bash", "-c", strCmd };
			// System.out.println("Linux / full screen cmd="+ strCmd);
			try {
				pb = new ProcessBuilder(args);
				pb = pb.redirectErrorStream(true); // on mélange les sorties du processus
				p = pb.start();
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				osStop = p.getOutputStream();

			} catch (IOException e) {

			}
		}

	}

	protected void lancerWithRectangle(String strCmd, Stage primaryStage) {
		// Creation du rectangle
		// System.out.println("strCmd avant Rectangle => " + strCmd);
		try {
			GlobalScreen.unregisterNativeHook();
			GlobalScreen.removeNativeKeyListener(this);
		} catch (NativeHookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		primaryStage.hide();
		choiceRec = true;

		myThread = new MyThread();

		Platform.runLater(myThread);

	}

	static public String returnOS() {
		if (isWindows()) {
			isWin = true;
			return "Windows";
		}
		if (isLinux()) {
			isWin = false;
			return "Linux";
		}
		if (isMac()) {
			isWin = false;
			return "Mac";
		}
		isWin = false;
		return "";
	}

	public static boolean isWindows() {

		if (OS.indexOf("win") >= 0) {
			isWin = true;
			return true;
		}
		return false;

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isLinux() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("linux") > 0);

	}

	private static String OS = System.getProperty("os.name").toLowerCase();

	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {

	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {

		// On verifie que dans un delai de moins de 1 seconde on a les 3 release:
		// intModifStop1 ALT
		// intModifStop2 CTRL
		// int LettreStop F ou autre lettre choisie dans config.properties

		int code = arg0.getKeyCode();
		if (code == intModifStop1 || code == intModifStop2 || code == intLettreStop) {
			System.out.println(arg0.getKeyCode());
			mapIntegerLong.hashmap.put((Integer) code, (Long) new Date().getTime());
			if (mapIntegerLong.stop()) {
				stopRec(stage);
			}
		}

	}

	static void stopRec(Stage primaryStage) {

		butRec.setDisable(false);
		butStop.setDisable(true);
		if (!withRectangle) {
			try {

				if (null != osStop) {
					osStop.write('q');
					osStop.write('\n');

					osStop.flush();
					osStop.close();
					System.out.println("Arret Enregistrement");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				if (null != Main.msTrack) {
					Main.msTrack.getPrimaryStage().hide();
					Main.msTrack.getPrimaryStage().close();
					Main.msTrack = null;
				}
				String message = "";
				if ((System.getProperty("user.language") + "_" + System.getProperty("user.country")).contains("fr")) {
					message = "Enregistrement terminé , fichier resultat => " + tfOutput.getText() + File.separator
							+ outFileOnly + "\n Cliquer pour fermer";
				} else {
					message = "Recording stopped, file result => " + tfOutput.getText() + File.separator + outFileOnly
							+ "\n Clic to close";
				}
				showPopupMessage(message, primaryStage);
				Main.stage.setIconified(false);

			}

		});

//		
		osStop = null; // last line

	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public static Popup createPopup(final String message) {
		final Popup popup = new Popup();
		popup.setAutoFix(true);
		popup.setAutoHide(true);
		popup.setHideOnEscape(true);
		Label label = new Label(message);
		label.setId("popLabel");
		label.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				popup.hide();
			}
		});

		label.getStyleClass().add("popup");
		popup.getContent().add(label);
		return popup;
	}

	public static void showPopupMessage(final String message, final Stage stage) {
		final Popup popup = createPopup(message);
		popup.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
				popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
			}
		});

		popup.show(stage);

	}
}

class MapIntegerLong {
	public MapIntegerLong() {
		hashmap.put((Integer) Main.intModifStop1, 0L);
		hashmap.put((Integer) Main.intModifStop2, 0L);
		hashmap.put((Integer) Main.intLettreStop, 0L);
	}

	HashMap<Integer, Long> hashmap = new HashMap<Integer, Long>(3, 1);

	boolean stop() {
		long deb = Math.min(Math.min(hashmap.get(Main.intModifStop1), hashmap.get(Main.intModifStop2)),
				hashmap.get(Main.intLettreStop));
		long fin = Math.max(Math.max(hashmap.get(Main.intModifStop1), hashmap.get(Main.intModifStop2)),
				hashmap.get(Main.intLettreStop));
		if (fin != 0 && deb != 0 && fin - deb < Main.delaiStopMs) {
			return true;
		}
		return false;
	}

}

class MyThread extends Thread {
	public boolean cancel = false;

	Stage stage = new Stage(StageStyle.TRANSPARENT);

	public void run() {
		Main.msTrack = new MyMouseTracker(stage);

	}
};
