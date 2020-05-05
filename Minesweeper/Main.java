import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main extends JFrame {
	Toolkit tk=Toolkit.getDefaultToolkit();
	Dimension screen=tk.getScreenSize(); // 화면의 크기를 구함
	JButton restart;
	static Main mineSweeper;
	static Field field;
	static JLabel indicator, highScore;
	static Counter mineCounter, timeCounter;
	static Timer timer;
	static String name;
	static int score;
	
	public Main(String title, int width, int height) { // Main 생성자
		super(title);
		setSize(width, height);
		setLocation((screen.width-width)/2, (screen.height-height)/2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		try { // 파일에서 이름과 최고점수를 불러온다
			FileReader fr=new FileReader("data.ser");
			BufferedReader br=new BufferedReader(fr);
			name=br.readLine();
			score=Integer.parseInt(br.readLine());
			br.close();
		} catch(FileNotFoundException fnfe) { // 파일이 없을 경우 최고기록을 Anonymous의 999초로 정한다.
			name="Anonymous";
			score=999;
		} catch(IOException ie) {
			ie.printStackTrace();
		}
		
		field=new Field(300, 300);
		
		restart=new JButton("재시작");
		restart.setSize(100, 25);
		restart.setLocation(5, 2);
		restart.addMouseListener(new MouseAdapter() { // mouseAdapter 추가
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
				
				timer.cancel(); // Timer 종료
			} // 메서드
		}); // addMouseListener 종료
		
		indicator=new JLabel("", JLabel.RIGHT);
		indicator.setSize(60, 25);
		indicator.setLocation(235, 2);
		
		highScore=new JLabel("최고점수: "+score+" by "+name, JLabel.CENTER);
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
		
	} // 생성자
	public static void main(String[] args) { // main
		mineSweeper=new Main("지뢰찾기", 306, 395);
	} // 메서드
} // 클래스