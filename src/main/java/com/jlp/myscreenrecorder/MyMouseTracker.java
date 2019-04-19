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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseListener;



import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MyMouseTracker implements NativeMouseInputListener, NativeKeyListener {
	Scene scene;
	AnchorPane root;
	Stage primaryStage;
	public static Rectangle rectToSee = new Rectangle(0, 0, 10, 10);
	double x = 0, y = 0, l = 0, h = 0;
	int maxCount = 10;
	int count = 0;
	boolean isClicked = false;
	boolean recording = false;

	Image imBeforePressed;
	Image imPressed;
	ImageView ivBeforePressed;
	ImageView ivPressed;
	public static double decalX = 0;
	public static double decalY = 0;

	public MyMouseTracker(Stage stage) {
		
		rectToSee = new Rectangle(0, 0, 10, 10);
		primaryStage = stage;
		recording = false;
		isClicked = false;
		root = new AnchorPane();
		root.setStyle("-fx-background-color: transparent");
		root.setBackground(Background.EMPTY);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
	
		scene = new Scene(root, primaryScreenBounds.getMaxX(), primaryScreenBounds.getMaxY());
		scene.setFill(Color.TRANSPARENT);

		primaryStage.setAlwaysOnTop(true);
		stage.setScene(scene);
		root.getChildren().add(rectToSee);

		rectToSee.setFill(Color.TRANSPARENT);
		rectToSee.setStrokeWidth(3.0);
		rectToSee.setStroke(Color.RED);
		// stage.setOpacity(0.0);
		// chargement du texte d'aide sous forme d'image
		URL imageURL;
		String prefix = "";
		if ((System.getProperty("user.language") + "_" + System.getProperty("user.country")).contains("fr")) {
			prefix = "fr_";
		}
		try {
			imageURL = new File(Main.rootProject + File.separator + "config" + File.separator + prefix + "leftClic.png")
					.toURI().toURL();
			imBeforePressed = new Image(imageURL.toExternalForm());
			;
			ivBeforePressed = new ImageView(imBeforePressed);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			imageURL = new File(
					Main.rootProject + File.separator + "config" + File.separator + prefix + "releaseClic.png").toURI()
							.toURL();
			imPressed = new Image(imageURL.toExternalForm());
			ivPressed = new ImageView(imPressed);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stage.show();
		stage.setAlwaysOnTop(true);
		Main.choiceRec = true;
		addListeners();
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nativeMousePressed(NativeMouseEvent arg0) {

		if (arg0.getButton() == 1) {
			x = arg0.getPoint().getX();
			y = arg0.getPoint().getY();
			isClicked = true;
			// rectToSee.setX(arg0.getPoint().getX());
			// rectToSee.setY(arg0.getPoint().getY());
			System.out.println("Pressed at X=" + arg0.getPoint().getX() + " , Y=" + arg0.getPoint().getY());
		}

	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent arg0) {

		if (arg0.getButton() == 1) {
			isClicked = false;
			recording = true;

			final double X = Math.min(x, arg0.getPoint().getX());
			final double Y = Math.min(y, arg0.getPoint().getY());
			final double L = Math.abs(x - arg0.getPoint().getX());
			final double H = Math.abs(y - arg0.getPoint().getY());
			if (L < 20 || H < 20) {
				// Rectangle trop petit on retourne
				return;
			}
		
			Platform.runLater(new Runnable() {
				@Override

				public void run() {
					if (root.getChildren().contains(ivBeforePressed))
						root.getChildren().remove(ivBeforePressed);
					if (root.getChildren().contains(ivPressed))
						root.getChildren().remove(ivPressed);
					rectToSee.setX(X);
					rectToSee.setY(Y);
					rectToSee.setWidth(L);
					rectToSee.setHeight(H);
					rectToSee.setVisible(true);
					rectToSee.toFront();
				}
			});
			// } else if (arg0.getButton() == 3 || arg0.getButton() == 2) {
			Main.choiceRec = false;

			GlobalScreen.removeNativeMouseListener(this);
			GlobalScreen.removeNativeMouseMotionListener(this);

			demarrerCapture();

			// }
		}
	}

	private void demarrerCapture() {
		// On demarre ici On modifie les valeur <X>, <Y>, <screeSize>
		Main.strCmd = Main.strCmd.replace("<X>", ((Integer) ((Double) rectToSee.getX()).intValue()).toString());
		Main.strCmd = Main.strCmd.replace("<Y>", ((Integer) ((Double) rectToSee.getY()).intValue()).toString());
		Main.strCmd = Main.strCmd.replace("<videosize>",
				((Integer) ((Double) rectToSee.getWidth()).intValue()).toString() + "x"
						+ ((Integer) ((Double) rectToSee.getHeight()).intValue()).toString());

		if (Main.isWin) {
			System.out.println("Rectangle lancement =>" + Main.strCmd);
			String[] args = { "cmd.exe", "/c", Main.strCmd };
			try {
				Main.pb = new ProcessBuilder(args);
				Main.pb = Main.pb.redirectErrorStream(true); // on mélange les sorties du processus
				Main.p = Main.pb.start();
				InputStream is = Main.p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				is.close();
				InputStream err = Main.p.getErrorStream();
				err.close();
				Main.osStop = Main.p.getOutputStream();

			} catch (IOException e) {

			}
		} else if (Main.isLinux()) {
			String[] args = { "bash", Main.strCmd };

			try {
				Main.pb = new ProcessBuilder(args);
				Main.pb = Main.pb.redirectErrorStream(true); // on mélange les sorties du processus
				Main.p = Main.pb.start();
				InputStream is = Main.p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				is.close();
				InputStream err = Main.p.getErrorStream();
				err.close();
				Main.osStop = Main.p.getOutputStream();

			} catch (IOException e) {

			}

		}
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent arg0) {
		count++;
		if (count < maxCount) {
			// double X = 0, Y = 0, L = 0, H = 0;
			final double X = Math.min(x, arg0.getPoint().getX());
			final double Y = Math.min(y, arg0.getPoint().getY());
			final double L = Math.abs(x - arg0.getPoint().getX());
			final double H = Math.abs(y - arg0.getPoint().getY());
			final double X_M = arg0.getPoint().getX();
			final double Y_M = arg0.getPoint().getY();

			Platform.runLater(new Runnable() {
				@Override

				public void run() {
					rectToSee.setX(X);
					rectToSee.setY(Y);
					rectToSee.setWidth(L);
					rectToSee.setHeight(H);
					rectToSee.setVisible(true);
					rectToSee.toFront();
					if (!recording) {
						if (root.getChildren().contains(ivBeforePressed))
							root.getChildren().remove(ivBeforePressed);
						if (!root.getChildren().contains(ivPressed))
							root.getChildren().add(ivPressed);
						decalX=imPressed.getWidth();
						decalY=imPressed.getHeight();
						ivPressed.setX(calculDecalX(X_M  ));

						ivPressed.setY(calculDecalY(Y_M ));
					}
				}
			});

			count = 0;
		}
	}


	@Override
	public void nativeMouseMoved(NativeMouseEvent arg0) {
		// TODO Auto-generated method stub
		if (!isClicked && !recording) {
			
			final double X = arg0.getPoint().getX();
			final double Y = arg0.getPoint().getY();
			Platform.runLater(new Runnable() {
				@Override

				public void run() {
					if (root.getChildren().contains(ivPressed))
						root.getChildren().remove(ivPressed);
					if (!root.getChildren().contains(ivBeforePressed))
						root.getChildren().add(ivBeforePressed);
					decalX=imBeforePressed.getWidth();
					decalY=imBeforePressed.getHeight();
					ivBeforePressed.setX(calculDecalX(X  ));
					ivBeforePressed.setY(calculDecalY(Y ));
				}
			});

		} else {
			// Treated in mouse dragged
		}

	}

	private void addListeners() {
		
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
			GlobalScreen.addNativeMouseListener(this);

			GlobalScreen.addNativeMouseMotionListener(this);
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
		int code = arg0.getKeyCode();
		// System.out.println("Key released :" + code);
		if (code == Main.intModifStop1 || code == Main.intModifStop2 || code == Main.intLettreStop) {
			System.out.println(arg0.getKeyCode());
			Main.mapIntegerLong.hashmap.put((Integer) code, (Long) new Date().getTime());
			if (Main.mapIntegerLong.stop()) {
				
				try {

					if (null !=Main. osStop) {
						Main.osStop.write('q');
						Main.osStop.write('\n');

						Main.osStop.flush();
						Main.osStop.close();
						System.out.println("Rectangle Arret Enregistrement");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recording = false;
			Main.stopRec(Main.stage);
					
				
			}
		}

	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	private double calculDecalX(double xMouse) {
		

		if (xMouse > Main.screenWidth - decalX) {
			return (xMouse -decalX -20);
		} else {
			return (xMouse + 20);
		}

	}

	private double calculDecalY(double yMouse) {
		if (yMouse > Main.screenHeigh - decalY ) {
			return (yMouse - 20 );
		} else {
			return (yMouse+decalY);
		}
	}
}
