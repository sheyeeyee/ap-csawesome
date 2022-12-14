package breakout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.StyledEditorKit;

import pong.Score;
import utilities.GDV5;

public class BreakoutRunner extends GDV5 {
	//create variable for max window sizes
	private static int winX = getMaxWindowX();
	private static int winY = getMaxWindowY();
	
	//set ball parameter
	private static int ballSize = 20;
	
	//set paddle parameters
	private static int pWidth = 200;
	private static int pHeight = 10;
	private static int pOffset = 20;
	private static int pX = (winX / 2 - pWidth / 2);
	private static int pY = winY - pHeight - pOffset;
	
	//creating objects
	static Brick[] brickArray;
	static Particles[] particleArray = new Particles[Particles.getPartNum()];
	BreakoutBall ball = new BreakoutBall(ballSize);
	BreakoutPaddle p = new BreakoutPaddle(pX, pY, pWidth, pHeight);
	Pages scoreboard = new Pages();
	
	//customizing colors
	private static String brickColor = "";
	private static String ballColor = "";
	private static String paddleColor = "";
	
	//gamestates
	private static int gameState = 0;
	private static boolean gameStart = false;
	private static boolean startPage = true;
	
	public BreakoutRunner() {
		super();
//		brickArray = Brick.makeBricks(); //bricks array equals the makeBricks() method
	}
	
	public static void main(String[] args) {
		BreakoutRunner runner = new BreakoutRunner();
		runner.start();
	}

	//bc of this, no loops needed since these are being called continuously
	@Override
	public void update() { //60 fps, driver called 60 times per second
		gameState();
		if (gameStart) {
			ball.move(p, brickArray);
			ballHitBricks(ball, brickArray, particleArray);
			ball.resetBall();
			p.paddleMove();
			Particles.moveParticles();
			PowerUp.powerUpUpdate(ball, p);
		}
		else if (gameState == 0 || BreakoutBall.getLives() == 0) resetBallPaddle();
	}

	@Override
	public void draw(Graphics2D win) { //at the processor speed (~3000 fps, called 3000 times per second)
		if (gameState == 0) {
			Pages.home(win);
			Pages.setScore(0);
			BreakoutBall.setLives(5);
		}
		if (gameState == 4) {
			Pages.pausedGame(win);
		}
		if (gameState == 5) {
			Pages.customize(win);
		}
		
		if (gameStart) {
			win.setColor(Color.black);
			win.fillRect(0, 0, winX, winY);
			
			//bricks (syntactic sugar)
			for (Brick b:brickArray) {
				b.draw(win);
			}
			for (int i = 0; i < particleArray.length; i++) {
				if (particleArray[i] != null) {
					particleArray[i].draw(win);
				}
			}
			
			//ball
			if (ballColor == "O" || ballColor == "") {
				win.setColor(Colors.pastelTan5);
			}
			if (ballColor == "T") {
				win.setColor(Colors.pastelTeal5);
			}
			if (ballColor == "Y") {
				win.setColor(Colors.pastelBlue5);
			}
			if (ballColor == "U") {
				win.setColor(Colors.pastelGreen5);
			}
			win.fillOval((int) ball.getX(), (int) ball.getY(), (int) ball.getWidth(), (int) ball.getHeight());
			
			//paddle
			if (paddleColor == "P" || paddleColor == "") {
				win.setColor(Colors.pastelTan6);
			}
			if (paddleColor == "G") {
				win.setColor(Colors.pastelTeal6);
			}
			if (paddleColor == "H") {
				win.setColor(Colors.pastelBlue6);
			}
			if (paddleColor == "J") {
				win.setColor(Colors.pastelGreen6);
			}
			win.fillRect((int) p.getX(), (int) p.getY(), (int) p.getWidth(), (int) p.getHeight());
			
			Pages.scoreboard(win);
			Pages.youWinLose(win);
		}
	}
	
	//getters
	public static int getWinX() {
		return winX;
	}
	public static int getWinY() {
		return winY;
	}
	public static void setPWidth(int newPWidth, BreakoutPaddle paddle) {
		paddle.setSize(newPWidth, pHeight);
	}
	public static int getPWidth() {
		return pWidth;
	}
	public static int getPHeight() {
		return pHeight;
	}
	
	public static int getPX() {
		return pX;
	}
	public static int getPY() {
		return pY;
	}
	public static void setBallSize(int newBallSize, BreakoutBall ball) {
		ball.setSize(newBallSize, newBallSize);
	}
	public static int getBallSize() {
		return ballSize;
	}
	public static String getBrickColor() {
		return brickColor;
	}
	public static String getBallColor() {
		return ballColor;
	}
	public static String getPaddleColor() {
		return paddleColor;
	}
	public static int getGameState() {
		return gameState;
	}
	public static boolean getGameStart() {
		return gameStart;
	}
	
	//CHALLENGE
	public void resetBallPaddle() {
		ball.resetBallPosition();
		p.resetPaddlePosition();
	}
	
	public static void changeBrickColor(int colArray) {
		for (Brick b:brickArray) {
			b.setColors(Colors.getColorPaletteShade(colArray, b.getColorShade()));
		}
	}
	
	public static void updateBrickColor() {
		if (brickColor == "I" || brickColor == "") {
			changeBrickColor(0);
		}
		if (brickColor == "W") {
			changeBrickColor(1);
		}
		if (brickColor == "E") {
			changeBrickColor(2);
		}
		if (brickColor == "R") {
			changeBrickColor(3);
		}
	}
	
	public static void gameState() {
		//0: splash page
		//1: easy mode
		//2: medium mode
		//3: hard mode
		//4: pause page
		//5: customization page
				
		if (GDV5.KeysPressed[KeyEvent.VK_ESCAPE] && gameState == 0) {
			gameState = 5; //customization
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_Q] && gameState == 5) {
			gameState = 0;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_1] && gameState == 0) {
			//CHALLENGE
			Brick.setRows(6);
			brickArray = Brick.makeBricks();
//			System.out.println("made bricks");
			updateBrickColor();
//			System.out.println("updated bricks");
			
			gameState = 1;
//			System.out.println("game started");
			gameStart = true;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_2] && gameState == 0) {
			Brick.setRows(9);
			brickArray = Brick.makeBricks();
//			System.out.println("made bricks");
			updateBrickColor();
//			System.out.println("updated bricks");

			gameState = 2;
//			System.out.println("game started");
			gameStart = true;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_3] && gameState == 0) {
			Brick.setRows(12);
			brickArray = Brick.makeBricks();
//			System.out.println("made bricks");
			updateBrickColor();
//			System.out.println("updated bricks");
			
			gameState = 3;
//			System.out.println("game started");
			gameStart = true;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_ESCAPE] && gameStart) {
			gameState = 4; //pause
			gameStart = false;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_SPACE] && !gameStart) {
			gameState = 1; //resume game
			gameStart = true;
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_ENTER] && Pages.getScore() == Brick.getNumBricks()) {
			gameState = 0; //splash page
			gameStart = false;
			PowerUp.resetTimers();
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_ENTER] && BreakoutBall.getLives() == 0) {
			gameState = 0; //splash page
			gameStart = false;
			PowerUp.resetTimers();
		}
		else if (GDV5.KeysPressed[KeyEvent.VK_Q] && gameState == 4) {
			gameState = 0; //splash page
			gameStart = false;
			PowerUp.resetTimers();
		}
		
		if (gameState == 0) {
			if (startPage == true) {
				brickArray = Brick.makeBricks();
				startPage = false;
				
//				System.out.println("bricks made");
			}
		}
		
		if (gameState == 5) {
			//brick colors
			if (GDV5.KeysPressed[KeyEvent.VK_I] && gameState == 5) {
				brickColor = "I";
			}
			if (GDV5.KeysPressed[KeyEvent.VK_W]) {
				brickColor = "W";
			}
			if (GDV5.KeysPressed[KeyEvent.VK_E]) {
				brickColor = "E";
			}
			if (GDV5.KeysPressed[KeyEvent.VK_R]) {
				brickColor = "R";
			}
		}
		
		//ball colors
		if (GDV5.KeysPressed[KeyEvent.VK_T] && gameState == 5) {
			ballColor = "T";
		}
		if (GDV5.KeysPressed[KeyEvent.VK_Y] && gameState == 5) {
			ballColor = "Y";
		}
		if (GDV5.KeysPressed[KeyEvent.VK_U] && gameState == 5) {
			ballColor = "U";
		}
		
		//paddle colors
		if (GDV5.KeysPressed[KeyEvent.VK_G] && gameState == 5) {
			paddleColor = "G";
		}
		if (GDV5.KeysPressed[KeyEvent.VK_H] && gameState == 5) {
			paddleColor = "H";
		}
		if (GDV5.KeysPressed[KeyEvent.VK_J] && gameState == 5) {
			paddleColor = "J";
		}
		
		//defaults
		if (GDV5.KeysPressed[KeyEvent.VK_O] && gameState == 5) {
			ballColor = "O";
		}
		if (GDV5.KeysPressed[KeyEvent.VK_P] && gameState == 5) {
			paddleColor = "P";
		}
		
		//CHALLENGE
//		brickArray = Brick.makeBricks(); //bricks array equals the makeBricks() method
	}
	
	//CHALLENGE
	public void ballHitBricks(BreakoutBall ball, Brick[] brick, Particles[] part) {
		int mvmtMax = 2;
		int mvmtMin = 0;
		int mvmt = mvmtMax - mvmtMin;
		int minV = 2;
		int maxV = 6;
		
		if (gameStart) {
			for (int i = 0; i < brickArray.length; i++) {
				if (ball.intersects(brickArray[i]) && brickArray[i].getBrickVis() == true) {
					int powerUpType = (int) (Math.random() * 12);
					System.out.println("Power Up: " + powerUpType);
					
					if (powerUpType == 1) {
						PowerUp.powerUpPaddle(p);
					}
					if (powerUpType == 2) {
						PowerUp.powerDownPaddle(p);
					}
					if (powerUpType == 3) {
						PowerUp.powerUpBall(ball);
					}
					if (powerUpType == 4) {
						PowerUp.powerDownBall(ball);
					}
					if (powerUpType == 5) {
						PowerUp.powerUpLives();
					}
					if (powerUpType == 6) {
						PowerUp.powerDownLives();
					}
					
					brickArray[i].setBrickVis(false);
					particleArray = Particles.makeParticles(brickArray[i]);
					
					int colDir = collisionDirection(brickArray[i], ball, ball.vX, ball.vY);
//					System.out.println(colDir);
					
					//score
					Pages.addScore(1);
					
					//ball intersects top
					if (colDir == 1) {
						if (minV < ball.vX && ball.vX < maxV) ball.vX = ball.vX + (int) (Math.random() * mvmt + mvmtMin);
						else if (-maxV < ball.vX && ball.vX < -minV) ball.vX = ball.vX - (int) (Math.random() * mvmt + mvmtMin);
						else if (ball.vX < -maxV) ball.vX = ball.vX + (int) (Math.random() * mvmt + mvmtMin);
						else ball.vX = ball.vX - (int) (Math.random() + mvmtMin);
						ball.vY = -Math.abs(ball.vY);
//						System.out.println("T vX: " + ball.vX + " vY: " + ball.vY);
					}
					
					//ball intersects bottom
					else if (colDir == 3) {
						if (minV < ball.vX && ball.vX < maxV) ball.vX = ball.vX + (int) (Math.random() * mvmt + mvmtMin);
						else if (-maxV < ball.vX && ball.vX < -minV) ball.vX = ball.vX - (int) (Math.random() * mvmt + mvmtMin);
						else if (ball.vX < -maxV) ball.vX = ball.vX + (int) (Math.random() * mvmt + mvmtMin);
						else ball.vX = ball.vX - (int) (Math.random() + mvmtMin);
						ball.vY = Math.abs(ball.vY);
//						System.out.println("B vX: " + ball.vX + " vY: " + ball.vY);
					}
					
					//ball intersects left
					else if (colDir == 2) {
						ball.vX = -Math.abs(ball.vX);
						if (minV < ball.vY && ball.vY < maxV) ball.vY = ball.vY + (int) (Math.random() * mvmt + mvmtMin);
						else if (-maxV < ball.vY && ball.vY < -minV) ball.vY = ball.vY - (int) (Math.random() * mvmt + mvmtMin);
						else if (ball.vY < -maxV) ball.vY = ball.vY + (int) (Math.random() * mvmt + mvmtMin);
						else ball.vY = ball.vY - (int) (Math.random() + mvmtMin);
//						System.out.println("L vX: " + ball.vX + " vY: " + ball.vY);
					}
					
					//ball intersects right
					else if (colDir == 0) {
						ball.vX = Math.abs(ball.vX);
						if (minV < ball.vY && ball.vY < maxV) ball.vY = ball.vY + (int) (Math.random() * mvmt + mvmtMin);
						else if (-maxV < ball.vY && ball.vY < -minV) ball.vY = ball.vY - (int) (Math.random() * mvmt + mvmtMin);
						else if (ball.vY < -maxV) ball.vY = ball.vY + (int) (Math.random() * mvmt + mvmtMin);
						else ball.vY = ball.vY - (int) (Math.random() + mvmtMin);
//						System.out.println("R vX: " + ball.vX + " vY: " + ball.vY);
					}
				}
			}
		}
	}
}
