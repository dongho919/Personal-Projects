import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;

public class HighScore extends JDialog {
	Toolkit tk=Toolkit.getDefaultToolkit();
	Dimension screen=tk.getScreenSize();
	static int width=200, height=100;
	static JLabel label;
	static TextField tf;
	static JButton confirm;
	
	public HighScore(JFrame parent, String title) { // HighScore ������
		super(parent, title, true);
		setSize(width, height);
		setLocation((screen.width-width)/2, (screen.height-height)/2);
		setLayout(null);
		
		label=new JLabel(Main.timeCounter.count+"", JLabel.CENTER);
		label.setSize(60, 20);
		label.setLocation(140, 5);
		tf=new TextField();
		tf.setSize(130, 20);
		tf.setLocation(5, 5);
		confirm=new JButton("OK");
		confirm.setSize(80, 25);
		confirm.setLocation(56, 35);
		confirm.addMouseListener(new MouseAdapter() { // MouseAdapter �߰�
			public void mouseClicked(MouseEvent me) { // override
				if(!tf.getText().equals("")) { // �̸� �Է¶��� �� ĭ�� �ƴ� ��
					try { // ���Ͽ� �ְ������� �����ϰ� ���� ȭ�鿡 ���̴� �ְ������� �����Ѵ�
						FileWriter fw=new FileWriter("data.ser");
						BufferedWriter bw=new BufferedWriter(fw);
						bw.write(tf.getText()+"\n"+Main.timeCounter.count);
						bw.close();
						
						Main.score=Main.timeCounter.count;
						Main.name=tf.getText();
						Main.highScore.setText("�ְ�����: "+Main.score+" by "+Main.name);
						
						dispose();
					} catch(IOException e) {
						e.printStackTrace();
					} // try-catch
				} // if
			} // �޼���
		}); // MouseAdpater �߰� ����
		
		add(tf);
		add(label);
		add(confirm);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	} // ������
} // Ŭ����