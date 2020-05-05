import java.awt.*;
import javax.swing.*;

class Field extends JPanel {
	static final int HORIZONTAL=15, VERTICAL=15, MINE_NUM=35;
	static boolean isSuccess=true, isStarted=false;
	static Ground[][] buttons=new Ground[VERTICAL+4][HORIZONTAL+4];
	static boolean[] mineField=new boolean[VERTICAL*HORIZONTAL];
	
	public Field(int width, int height) { // Field ������
		super();
		setSize(width, height);
		setLocation(0, 30);
		setLayout(new GridLayout(VERTICAL, HORIZONTAL));
		
		shuffle();
		
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // Field�� Ground�� �߰���
			add(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2]);
		} // for
		
		setVisible(true);
	} // ������
	public static void shuffle() {
		for(int i=0; i<(VERTICAL+4)*(HORIZONTAL+4); i++) { // Ground�� ������
			buttons[i%(HORIZONTAL+4)][i/(HORIZONTAL+4)]=new Ground(i%(HORIZONTAL+4), i/(HORIZONTAL+4));
		} // for
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // mineField�� �ʱ�ȭ��
			mineField[i]=false;
		} // for
		for(int i=0; i<MINE_NUM; i++) { // ���ڸ� �־� ��
			mineField[i]=true;
		} // for
		for(int i=0; i<10000; i++) { // ���� ������ ����
			int rand=(int)(Math.random()*mineField.length);
			boolean tmp=mineField[0];
			mineField[0]=mineField[rand];
			mineField[rand]=tmp;
		} // for
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // ���� ���ڸ� ��ġ��
			buttons[i%HORIZONTAL+2][i/HORIZONTAL+2].hasMine=mineField[i];
		} // for
	}
	
	public static int countMine(Ground ground) { // �ֺ� ������ ������ ���� �޼���
		int result=0;
		
		for(int i=0; i<4; i++) {
			if(buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].hasMine) {
				result++;
			}
			if(buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].hasMine) {
				result++;
			}
		}
		
		return result;
	} // �޼���
	
	public static int countOpen(Ground ground) { // �ֺ��� �� ���� ���� �ִ��� ���� �޼���
		int result=0;
		for(int i=0; i<4; i++) {
			if(buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].isOpen) {
				result++;
			}
			if(buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].isOpen) {
				result++;
			}
		}
		return result;
	} // �޼���
	
	public static void open(Ground ground) { // �ֺ� 8���� ���� ���� �޼���
		if(!ground.hasMine) {
			for(int i=0; i<4; i++) {
				buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].setIcon(new ImageIcon(Ground.class.getResource((buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].isMarked) ? "src/img/mine.png" : "src/img/open"+countMine(buttons[ground.row+i%2*2-1][ground.col+i/2*2-1])+".png")));
				buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].setIcon(new ImageIcon(Ground.class.getResource((buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].isMarked) ? "src/img/mine.png" : "src/img/open"+countMine(buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2])+".png")));
				buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].isOpen=true;
				buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].isOpen=true;
			}
		}
	} // �޼���
	
	public static boolean isAllOpen() { // ��ư�� ���� ���ȴ��� �˻��ϴ� �޼���
		boolean result=true;
		Ground ground;
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) {
			ground=buttons[i%HORIZONTAL+2][i/HORIZONTAL+2];
			result=(ground.isOpen || (ground.hasMine && ground.isMarked)) ? result : false;
		}
		return result;
	} // �޼���
	
	public static boolean checkButtons() { // countMine()�� 0�̰� ���� ��ư�� �ִ��� �˻��ϴ� �޼���
		boolean result=false;
		
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) {
			if(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2].isOpen && countOpen(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2])<8 && countMine(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2])==0) {
				result=true;
			}
		}
		return result;
	} // �޼���
} // Ŭ����