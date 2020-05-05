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
		GridBagConstraints layout = new GridBagConstraints(); // ���̾ƿ� �Ŵ��� ����
		
		///////////////////////////////// ���̺� ���� /////////////////////////////////
		
		layout.gridwidth = 2;
		layout.anchor = GridBagConstraints.PAGE_END;
		layout.weightx = 1.0;
		layout.weighty = 1.0;
		
		comingText = new JLabel("�̹� ���");
		layout.gridx = 0;
		layout.gridy = 0;
		add(comingText, layout);
		
		nextText = new JLabel("���� ���");
		layout.gridx = 0;
		layout.gridy = 2;
		add(nextText, layout);
		
		scoreText = new JLabel("���� ����");
		layout.gridx = 0;
		layout.gridy = 4;
		add(scoreText, layout);
		
		///////////////////////////////// comingDisp, nextDisp ���� /////////////////////////////////
		
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
		
		///////////////////////////////// ���� ���̺� ���� /////////////////////////////////
		
		layout.ipadx = 50;
		layout.ipady = 0;
		
		scoreDisp = new JLabel(score + ""); // score�� String���� ����� ���̺� ǥ���ϱ� ����
		scoreDisp.setHorizontalAlignment(JLabel.CENTER);
		scoreDisp.setFont(new Font("Serif", Font.PLAIN, 20));
		layout.gridx = 0;
		layout.gridy = 5;
		add(scoreDisp, layout);
		
		///////////////////////////////// ��ư ���� /////////////////////////////////
		
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
		
		pause.addMouseListener(new MouseListener() { // �Ͻ�����/��� ��ư�� ������ isPlaying ���� not�� ���Ѵ�.
			public void mouseClicked(MouseEvent me) {}
			public void mouseEntered(MouseEvent me) { // ���콺�� ��ư ������ ������ �� �� ������� ���ϵ��� �Ѵ�.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			public void mouseExited(MouseEvent me) { // ���콺�� ��ư �������� ���������� �� ������ ȭ��ǥ ������� ���ϵ��� �Ѵ�.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				setCursor(cursor);
			}
			public void mousePressed(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {
				if(isPlaying) { // ������ �÷����ϰ� �ִ� ��� �Ͻ������� �Ѵ�.
					Tetris.mainScreen.stop();
				} else if(!Tetris.mainScreen.isTopReached()) { // ������ �Ͻ������߰� �й����� ���� ��� ����ϱ�(resume)�� �Ѵ�.
					Tetris.mainScreen.resume();
				} else { // ������ �Ͻ������߰� �й��� ��� ������ ���� �����Ѵ�.
					Tetris.mainScreen.terminate();
					Tetris.mainScreen.resume();
				}
				
				isPlaying = !isPlaying; // ���/�Ͻ����� ���¿� not�� ���Ѵ�.
			}
		}); // pause.addMouseListener
		
		stop.addMouseListener(new MouseListener() { // ���� ��ư�� ������ ������ ����ǵ��� �Ѵ�.
			public void mouseClicked(MouseEvent me) {}
			public void mouseEntered(MouseEvent me) { // ���콺�� ��ư ������ ������ �� �� ������� ���ϵ��� �Ѵ�.
				Cursor cursor = Cursor.getDefaultCursor();
				cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			public void mouseExited(MouseEvent me) { // ���콺�� ��ư �������� ���������� �� ������ ȭ��ǥ ������� ���ϵ��� �Ѵ�.
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
		
		///////////////////////////////// ���̺� 6��, ��ư 2�� ���� �Ϸ� /////////////////////////////////
	}
}

class MiniLabel extends JLabel { // �̹� ��ϰ� ���� ����� ���� �ִ� �̴��г� ����
	
	private static final long serialVersionUID = 1L;
	
	Block.Type type;
	
	void update() { // ���� ǥ�õǴ� ����� �ٲ�
		setIcon(new ImageIcon("resources/" + type + ".png"));
	}
}