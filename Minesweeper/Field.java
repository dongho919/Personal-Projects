import java.awt.*;
import javax.swing.*;

class Field extends JPanel {
	static final int HORIZONTAL=15, VERTICAL=15, MINE_NUM=35;
	static boolean isSuccess=true, isStarted=false;
	static Ground[][] buttons=new Ground[VERTICAL+4][HORIZONTAL+4];
	static boolean[] mineField=new boolean[VERTICAL*HORIZONTAL];
	
	public Field(int width, int height) { // Field 생성자
		super();
		setSize(width, height);
		setLocation(0, 30);
		setLayout(new GridLayout(VERTICAL, HORIZONTAL));
		
		shuffle();
		
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // Field에 Ground를 추가함
			add(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2]);
		} // for
		
		setVisible(true);
	} // 생성자
	public static void shuffle() {
		for(int i=0; i<(VERTICAL+4)*(HORIZONTAL+4); i++) { // Ground를 생성함
			buttons[i%(HORIZONTAL+4)][i/(HORIZONTAL+4)]=new Ground(i%(HORIZONTAL+4), i/(HORIZONTAL+4));
		} // for
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // mineField를 초기화함
			mineField[i]=false;
		} // for
		for(int i=0; i<MINE_NUM; i++) { // 지뢰를 넣어 줌
			mineField[i]=true;
		} // for
		for(int i=0; i<10000; i++) { // 지뢰 순서를 섞음
			int rand=(int)(Math.random()*mineField.length);
			boolean tmp=mineField[0];
			mineField[0]=mineField[rand];
			mineField[rand]=tmp;
		} // for
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) { // 땅에 지뢰를 배치함
			buttons[i%HORIZONTAL+2][i/HORIZONTAL+2].hasMine=mineField[i];
		} // for
	}
	
	public static int countMine(Ground ground) { // 주변 지뢰의 개수를 세는 메서드
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
	} // 메서드
	
	public static int countOpen(Ground ground) { // 주변에 몇 개가 열려 있는지 세는 메서드
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
	} // 메서드
	
	public static void open(Ground ground) { // 주변 8개의 블럭을 여는 메서드
		if(!ground.hasMine) {
			for(int i=0; i<4; i++) {
				buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].setIcon(new ImageIcon(Ground.class.getResource((buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].isMarked) ? "src/img/mine.png" : "src/img/open"+countMine(buttons[ground.row+i%2*2-1][ground.col+i/2*2-1])+".png")));
				buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].setIcon(new ImageIcon(Ground.class.getResource((buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].isMarked) ? "src/img/mine.png" : "src/img/open"+countMine(buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2])+".png")));
				buttons[ground.row+i%2*2-1][ground.col+i/2*2-1].isOpen=true;
				buttons[ground.row+(i+1)/2-1][ground.col+i/2-i%2].isOpen=true;
			}
		}
	} // 메서드
	
	public static boolean isAllOpen() { // 버튼이 전부 열렸는지 검사하는 메서드
		boolean result=true;
		Ground ground;
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) {
			ground=buttons[i%HORIZONTAL+2][i/HORIZONTAL+2];
			result=(ground.isOpen || (ground.hasMine && ground.isMarked)) ? result : false;
		}
		return result;
	} // 메서드
	
	public static boolean checkButtons() { // countMine()이 0이고 열린 버튼이 있는지 검사하는 메서드
		boolean result=false;
		
		for(int i=0; i<VERTICAL*HORIZONTAL; i++) {
			if(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2].isOpen && countOpen(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2])<8 && countMine(buttons[i%HORIZONTAL+2][i/HORIZONTAL+2])==0) {
				result=true;
			}
		}
		return result;
	} // 메서드
} // 클래스