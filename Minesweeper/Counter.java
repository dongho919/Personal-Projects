import java.awt.*;
import javax.swing.*;

public class Counter extends JPanel {
	int count;
	
	public Counter(int init, int x, int y) { // Counter 생성자
		super();
		setSize(45, 25);
		setLocation(x, y);
		setLayout(new GridLayout(1, 3));
		setBackground(Color.black);
		count=init;
		
		update();
	} // 생성자
	public Counter(int init) { // 생성자
		super();
		setSize(45, 25);
		setLayout(new GridLayout(1, 3));
		setBackground(Color.black);
		count=init;
		
		update();
	} // 생성자
	public void update() { // 숫자가 바뀐 것을 반영하는 메서드
		removeAll();
		if(count>=0) { // count가 0보다 작을 경우
			for(int i=0; i<3; i++) { // 세 자리 숫자를 넣음
				add(new JLabel(new ImageIcon(Counter.class.getResource("src/img/count"+(count/((int)Math.pow(10, 2-i))%10)+".png"))));
			} // for
			invalidate();
			validate();
		} else {
			for(int i=0; i<3; i++) { // 세 자리 숫자를 넣음
				add(new JLabel(new ImageIcon(Counter.class.getResource("src/img/count0.png"))));
			} // for
		} // if
	} // 메서드
} // 클래스