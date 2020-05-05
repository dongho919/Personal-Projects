import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main extends JFrame {
	Toolkit tk=Toolkit.getDefaultToolkit();
	Dimension screen=tk.getScreenSize(); // ȭ���� ũ�⸦ ����
	JButton restart;
	static Main mineSweeper;
	static Field field;
	static JLabel indicator, highScore;
	static Counter mineCounter, timeCounter;
	static Timer timer;
	static String name;
	static int score;
	
	public Main(String title, int width, int height) { // Main ������
		super(title);
		setSize(width, height);
		setLocation((screen.width-width)/2, (screen.height-height)/2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		try { // ���Ͽ��� �̸��� �ְ������� �ҷ��´�
			FileReader fr=new FileReader("data.ser");
			BufferedReader br=new BufferedReader(fr);
			name=br.readLine();
			score=Integer.parseInt(br.readLine());
			br.close();
		} catch(FileNotFoundException fnfe) { // ������ ���� ��� �ְ����� Anonymous�� 999�ʷ� ���Ѵ�.
			name="Anonymous";
			score=999;
		} catch(IOException ie) {
			ie.printStackTrace();
		}
		
		field=new Field(300, 300);
		
		restart=new JButton("�����");
		restart.setSize(100, 25);
		restart.setLocation(5, 2);
		restart.addMouseListener(new MouseAdapter() { // mouseAdapter �߰�
			public void mouseClicked(MouseEvent me) { // override
				Main.indicator.setText("");
				Field.isSuccess=true;
				Field.isStarted=false;
				
				mineSweeper.getContentPane().remove(field);
				field=new Field(300, 300);
				mineSweeper.getContentPane().add(field);
				mineSweeper.getContentPane().invalidate();
				mineSweeper.getContentPane().validate();
				
				mineCounter.count=Field.MINE_NUM;
				timeCounter.count=0;
				mineCounter.update();
				timeCounter.update();
				
				timer.cancel(); // Timer ����
			} // �޼���
		}); // addMouseListener ����
		
		indicator=new JLabel("", JLabel.RIGHT);
		indicator.setSize(60, 25);
		indicator.setLocation(235, 2);
		
		highScore=new JLabel("�ְ�����: "+score+" by "+name, JLabel.CENTER);
		highScore.setSize(300, 25);
		highScore.setLocation(0, 330);
		
		mineCounter=new Counter(Field.MINE_NUM, 110, 2);
		timeCounter=new Counter(0, 160, 2);
		
		add(mineCounter);
		add(timeCounter);
		add(highScore);
		add(field);
		add(restart);
		add(indicator);
		setVisible(true);
		
	} // ������
	public static void main(String[] args) { // main
		mineSweeper=new Main("����ã��", 306, 395);
	} // �޼���
} // Ŭ����