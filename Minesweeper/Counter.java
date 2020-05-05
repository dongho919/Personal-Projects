import java.awt.*;
import javax.swing.*;

public class Counter extends JPanel {
	int count;
	
	public Counter(int init, int x, int y) { // Counter ������
		super();
		setSize(45, 25);
		setLocation(x, y);
		setLayout(new GridLayout(1, 3));
		setBackground(Color.black);
		count=init;
		
		update();
	} // ������
	public Counter(int init) { // ������
		super();
		setSize(45, 25);
		setLayout(new GridLayout(1, 3));
		setBackground(Color.black);
		count=init;
		
		update();
	} // ������
	public void update() { // ���ڰ� �ٲ� ���� �ݿ��ϴ� �޼���
		removeAll();
		if(count>=0) { // count�� 0���� ���� ���
			for(int i=0; i<3; i++) { // �� �ڸ� ���ڸ� ����
				add(new JLabel(new ImageIcon(Counter.class.getResource("src/img/count"+(count/((int)Math.pow(10, 2-i))%10)+".png"))));
			} // for
			invalidate();
			validate();
		} else {
			for(int i=0; i<3; i++) { // �� �ڸ� ���ڸ� ����
				add(new JLabel(new ImageIcon(Counter.class.getResource("src/img/count0.png"))));
			} // for
		} // if
	} // �޼���
} // Ŭ����