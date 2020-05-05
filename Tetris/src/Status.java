import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Status extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	protected int score;
	boolean isPlaying;
	JLabel comingText, nextText;
	JLabel scoreText, scoreDisp;
	MiniLabel comingDisp, nextDisp;
	JButton pause, stop;
	
	Status(int width, int height) {
		
		score = 0;
		isPlaying = false;
		
		setSize(width, height);
		setBackground(Color.LIGHT_GRAY);
		setLayout(new GridBagLayout());
		GridBagConstraints layout = new GridBagConstraints(); // 레이아웃 매니저 설정
		
		///////////////////////////////// 레이블 삽입 /////////////////////////////////
		
		layout.gridwidth = 2;
		layout.anchor = GridBagConstraints.PAGE_END;
		layout.weightx = 1.0;
		layout.weighty = 1.0;
		
		comingText = new JLabel("이번 블록");
		layout.gridx = 0;
		layout.gridy = 0;
		add(comingText, layout);
		
		nextText = new JLabel("다음 블록");
		layout.gridx = 0;
		layout.gridy = 2;
		add(nextText, layout);
		
		scoreText = new JLabel("현재 점수");
		layout.gridx = 0;
		layout.gridy = 4;
		add(scoreText, layout);
		
		///////////////////////////////// comingDisp, nextDisp 삽입 /////////////////////////////////
		
		layout.anchor = GridBagConstraints.CENTER;
		
		comingDisp = new MiniLabel();
		comingDisp.type = Block.Type.none;
		layout.gridx = 0;
		layout.gridy = 1;
		add(comingDisp, layout);
		
		nextDisp = new MiniLabel();
		nextDisp.type = Block.Type.none;
		layout.gridx = 0;
		layout.gridy = 3;
		add(nextDisp, layout);
		
		///////////////////////////////// 점수 레이블 삽입 /////////////////////////////////
		
		layout.ipadx = 50;
		layout.ipady = 0;
		
		scoreDisp = new JLabel(score + ""); // score를 String으로 만들어 레이블에 표시하기 위함
		scoreDisp.setHorizontalAlignment(JLabel.CENTER);
		scoreDisp.setFont(new Font("Serif", Font.PLAIN, 20));
		layout.gridx = 0;
		layout.gridy = 5;
		add(scoreDisp, layout);
		
		///////////////////////////////// 버튼 삽입 /////////////////////////////////
		
		layout.ipadx = 0;
		layout.ipady = 0;
		layout.gridwidth = 1;
		
		try {
			pause = new JButton(new ImageIcon("resources/pause.png"));
			stop = new JButton(new ImageIcon("resources/stop.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		pause.setContentAreaFilled(false); stop.setContentAreaFilled(false);
		pause.setBorderPainted(false); stop.setBorderPainted(false);
		
		pause.addMouseListener(new MouseListener() { // 일시정지/재생 버튼을 누르면 isPlaying 값에 not을 취한다.
			public void mouseClicked(MouseEvent me) {}
			public void mouseEntered(MouseEvent me) { // 마우스가 버튼 영역에 들어왔을 때 손 모양으로 변하도록 한다.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			public void mouseExited(MouseEvent me) { // 마우스가 버튼 영역에서 빠져나갔을 때 원래의 화살표 모양으로 변하도록 한다.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				setCursor(cursor);
			}
			public void mousePressed(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {
				if(isPlaying) { // 게임을 플레이하고 있는 경우 일시정지를 한다.
					Tetris.mainScreen.stop();
				} else if(!Tetris.mainScreen.isTopReached()) { // 게임을 일시정지했고 패배하지 않은 경우 계속하기(resume)를 한다.
					Tetris.mainScreen.resume();
				} else { // 게임을 일시정지했고 패배한 경우 게임을 새로 시작한다.
					Tetris.mainScreen.terminate();
					Tetris.mainScreen.resume();
				}
				
				isPlaying = !isPlaying; // 재생/일시정지 상태에 not을 취한다.
			}
		}); // pause.addMouseListener
		
		stop.addMouseListener(new MouseListener() { // 정지 버튼을 누르면 게임이 종료되도록 한다.
			public void mouseClicked(MouseEvent me) {}
			public void mouseEntered(MouseEvent me) { // 마우스가 버튼 영역에 들어왔을 때 손 모양으로 변하도록 한다.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			public void mouseExited(MouseEvent me) { // 마우스가 버튼 영역에서 빠져나갔을 때 원래의 화살표 모양으로 변하도록 한다.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				setCursor(cursor);
			}
			public void mousePressed(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {
				isPlaying = false;
				Tetris.mainScreen.terminate();
			}
		}); // stop.addMouseListener
		
		layout.gridx = 0;
		layout.gridy = 6;
		add(pause, layout);
		
		layout.gridx = 1;
		layout.gridy = 6;
		add(stop, layout);
		
		///////////////////////////////// 레이블 6개, 버튼 2개 삽입 완료 /////////////////////////////////
	}
}

class MiniLabel extends JLabel { // 이번 블록과 다음 블록을 보여 주는 미니패널 역할
	
	private static final long serialVersionUID = 1L;
	
	Block.Type type;
	
	void update() { // 현재 표시되는 블록을 바꿈
		setIcon(new ImageIcon("resources/" + type + ".png"));
	}
}