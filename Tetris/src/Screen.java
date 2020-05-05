import javax.swing.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Screen extends JPanel { // 블록이 움직일 화면 영역
	
	enum PixColor { none, gray, orange, skyblue, pink, red, blue, green } // 픽셀의 색깔을 나타내는 열거형
	
	private static final long serialVersionUID = 1L;
	
	byte[][] colorMap = new byte[Tetris.MAP_HEIGHT][Tetris.MAP_WIDTH];
	static ImageIcon[] pixels;
	Block block; // 현재 떨어지고 있는 블록을 참조하는 변수
	Timer updateTimer;
	
	Screen(int width, int height) { // 생성자
		setSize(width, height);
		setBackground(Color.BLACK);
		setLayout(new GridLayout(Tetris.MAP_HEIGHT, Tetris.MAP_WIDTH));
		
		pixels = new ImageIcon[8];
		int i = 0;
		for(PixColor color : PixColor.values()) { // Block 클래스에 속한 PixColor 열거형의 각 원소에 대응하는 이미지를 로딩한다.
			pixels[i] = new ImageIcon("resources/" + color + ".png");
			i++;
		} // for
	} // 생성자
	
	void update() { // colorMap에 맞게 패널을 새로고침한다. FIXME: Screen이 업데이트될 때 간혹 remove가 제대로 작동하지 않아 찌그러진 모습의 블록이 나타난다.
		
		while(getComponentCount() > 0) {
			remove(0);
		} // while
		
		byte color;
		JLabel tmpLabel;
		for(int row = 0; row < Tetris.MAP_HEIGHT; row++) {
			for(int col = 0; col < Tetris.MAP_WIDTH; col++) {
				color = colorMap[row][col];
				tmpLabel = new JLabel(pixels[color]);
				add(tmpLabel);
			} // inner for loop
		} // outer for loop
		
		revalidate();
		repaint();
		
	} // update 메서드
	
	boolean isRowFilled(int row) { // 해당하는 줄이 꽉 채워져 있는지 확인하는 메서드
		for(int col = 0; col < Tetris.MAP_WIDTH; col++) { // 해당 행에서 각 열을 검사하며, 0을 만나는 경우 바로 false(줄이 채워져 있지 않음)을 반환한다.
			if(colorMap[row][col] == 0) return false;
		}
		return true; // 0을 한 번도 만나지 않은 경우 줄이 채워져 있는 것이므로 true를 반환한다.	
	} // isRowFilled 메서드
	
	boolean isTopReached() { // 화면의 맨 윗부분까지 굳은 블럭이 있는지 확인하는 메서드
		for(int col = 0; col < Tetris.MAP_WIDTH; col++) {
			if(colorMap[0][col] != 0) return true; // 가장 윗부분까지 픽셀이 차 있고 그 픽셀이 block에 속한 것이 아니라면 isTopReached는 참을 반환한다.
		}
		
		return false;
	}
	void clear() { // 꽉 채워져 있는 줄들을 클리어하는 메서드
		for(int row = 0; row < Tetris.MAP_HEIGHT; row++) { // 각 행을 확인하는 루프
			
			if(isRowFilled(row)) { // 행이 꽉 차 있을 경우 행을 지우고 위의 행들을 한 칸씩 아래로 내린다.
				Tetris.status.score += 10;
				Tetris.status.scoreDisp.setText(Tetris.status.score + "");
				for(int tmpRow = row; tmpRow > 0; tmpRow--) {
					for(int tmpCol = 0; tmpCol < Tetris.MAP_WIDTH; tmpCol++) {
						
						if(!block.isMyPixel(tmpRow - 1, tmpCol))
							colorMap[tmpRow][tmpCol] = colorMap[tmpRow - 1][tmpCol];
					} // innermost for
				} // inner for
			} // if
			
		} // outer for
	} // clear 메서드
	
	void stop() { // 일지 정지 혹은 게임이 끝났을 때
		updateTimer.cancel();
	} // stop 메서드
	
	void resume() {
		if(Tetris.status.comingDisp.type == Block.Type.none) { // 만일 게임이 아직 시작되지 않았다면 새로 내려올 블록, 이번 블록, 다음 블록을 모두 랜덤으로 뽑는다.
			Random r = new Random();
			Tetris.status.comingDisp.type = Block.Type.values()[r.nextInt(7) + 1];
			Tetris.status.comingDisp.update();
			Tetris.status.nextDisp.type = Block.Type.values()[r.nextInt(7) + 1];
			Tetris.status.nextDisp.update();
			block = new Block(Block.Type.values()[r.nextInt(7) + 1]);
			block.updateColorMap();
			update();
		}
		
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTask(), 1000, 1000);
	} // resume 메서드
	
	void terminate() { // Screen과 Status를 완전히 백지 상태로 만든다.
		updateTimer.cancel();
		Tetris.status.score = 0;
		Tetris.status.scoreDisp.setText(Tetris.status.score + "");
		Tetris.status.comingDisp.type = Block.Type.none;
		Tetris.status.nextDisp.type = Block.Type.none;
		Tetris.status.comingDisp.update();
		Tetris.status.nextDisp.update();
		for(int row = 0; row < Tetris.MAP_HEIGHT; row++) {
			for(int col = 0; col < Tetris.MAP_WIDTH; col++) {
				colorMap[row][col] = 0;
			}
		}
		update();
	} // terminate 메서드
}

class UpdateTask extends TimerTask { // Screen을 주기적으로 업데이트하기 위한 TimerTask
	
	Random r;
	
	UpdateTask() {
		r = new Random();
	}
	
	public void run() {
		if(Tetris.mainScreen.block.hitTest()) { // hitTest가 참일 경우 블록을 굳히고 꽉 찬 줄을 지워야 한다.
			if(Tetris.mainScreen.isTopReached()) {
				Tetris.status.isPlaying = false;
				Tetris.mainScreen.stop();
			}
			Tetris.mainScreen.block = new Block(Tetris.status.comingDisp.type);
			Tetris.mainScreen.clear();
			Tetris.mainScreen.update();
			Tetris.status.comingDisp.type = Tetris.status.nextDisp.type;
			Tetris.status.comingDisp.update();
			Tetris.status.nextDisp.type = Block.Type.values()[r.nextInt(7) + 1];
			Tetris.status.nextDisp.update(); // 다음에 올 블록을 랜덤으로 고른다.
			
		} else { // hitTest가 거짓일 경우 블록을 한 단계 떨어뜨려야 한다.
			Tetris.mainScreen.block.fallOneStep();
			Tetris.mainScreen.update();
		} // if-else
	} // run 메서드
} // UpdateTask 클래스