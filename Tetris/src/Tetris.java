import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class Tetris extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private static final int FRAME_WIDTH = 350;
	private static final int FRAME_HEIGHT = 450;
	
	static final int SCREEN_WIDTH = 200;
	static final int SCREEN_HEIGHT = 380;
	static final int STATUS_WIDTH = 120;
	static final int STATUS_HEIGHT = 380;
	
	static final int PIXEL_SIZE = 20;
	static final int MAP_WIDTH = SCREEN_WIDTH / PIXEL_SIZE;
	static final int MAP_HEIGHT = SCREEN_HEIGHT / PIXEL_SIZE;
	
	static Screen mainScreen;
	static Status status;
	
	Tetris(String title, int width, int height) {
		setTitle(title);
		setSize(width, height);
		this.getContentPane().setBackground(Color.LIGHT_GRAY);
	}
	
	public static void main(String[] args) {
		
		JFrame tetris = new Tetris("TETRIS", FRAME_WIDTH, FRAME_HEIGHT);
		tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tetris.setResizable(false);
		tetris.setLayout(null);
		
		mainScreen = new Screen(SCREEN_WIDTH, SCREEN_HEIGHT);
		mainScreen.setLocation(15, 30);
		status = new Status(STATUS_WIDTH, STATUS_HEIGHT);
		status.setLocation(20 + SCREEN_WIDTH, 30);
		
		tetris.add(mainScreen);
		tetris.add(status);
		tetris.setResizable(false);
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent ke) { 
				
				if(ke.getID() == KeyEvent.KEY_PRESSED) {
					
					System.out.println("Key pressed");
					
					switch(ke.getKeyCode()) {
					case KeyEvent.VK_W:
						mainScreen.block.fallToBottom();
						mainScreen.update();
						break;
						
					case KeyEvent.VK_A:
						mainScreen.block.moveLeft();
						mainScreen.update();
						break;
						
					case KeyEvent.VK_S:
						mainScreen.block.fallOneStep();
						mainScreen.update();
						break;
						
					case KeyEvent.VK_D:
						mainScreen.block.moveRight();
						mainScreen.update();
						break;
						
					case KeyEvent.VK_Q:
						mainScreen.block.turnCCW();
						mainScreen.update();
						break;
						
					case KeyEvent.VK_E:
						mainScreen.block.turnCW();
						mainScreen.update();
						break;
					}
				}
				
				
				return false;
			}
			
		});
		tetris.setVisible(true);
	}
}