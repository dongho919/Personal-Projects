import javax.swing.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Screen extends JPanel { // ����� ������ ȭ�� ����
	
	enum PixColor { none, gray, orange, skyblue, pink, red, blue, green } // �ȼ��� ������ ��Ÿ���� ������
	
	private static final long serialVersionUID = 1L;
	
	byte[][] colorMap = new byte[Tetris.MAP_HEIGHT][Tetris.MAP_WIDTH];
	static ImageIcon[] pixels;
	Block block; // ���� �������� �ִ� ����� �����ϴ� ����
	Timer updateTimer;
	
	Screen(int width, int height) { // ������
		setSize(width, height);
		setBackground(Color.BLACK);
		setLayout(new GridLayout(Tetris.MAP_HEIGHT, Tetris.MAP_WIDTH));
		
		pixels = new ImageIcon[8];
		int i = 0;
		for(PixColor color : PixColor.values()) { // Block Ŭ������ ���� PixColor �������� �� ���ҿ� �����ϴ� �̹����� �ε��Ѵ�.
			pixels[i] = new ImageIcon("resources/" + color + ".png");
			i++;
		} // for
	} // ������
	
	void update() { // colorMap�� �°� �г��� ���ΰ�ħ�Ѵ�. FIXME: Screen�� ������Ʈ�� �� ��Ȥ remove�� ����� �۵����� �ʾ� ��׷��� ����� ����� ��Ÿ����.
		
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
		
	} // update �޼���
	
	boolean isRowFilled(int row) { // �ش��ϴ� ���� �� ä���� �ִ��� Ȯ���ϴ� �޼���
		for(int col = 0; col < Tetris.MAP_WIDTH; col++) { // �ش� �࿡�� �� ���� �˻��ϸ�, 0�� ������ ��� �ٷ� false(���� ä���� ���� ����)�� ��ȯ�Ѵ�.
			if(colorMap[row][col] == 0) return false;
		}
		return true; // 0�� �� ���� ������ ���� ��� ���� ä���� �ִ� ���̹Ƿ� true�� ��ȯ�Ѵ�.	
	} // isRowFilled �޼���
	
	boolean isTopReached() { // ȭ���� �� ���κб��� ���� ���� �ִ��� Ȯ���ϴ� �޼���
		for(int col = 0; col < Tetris.MAP_WIDTH; col++) {
			if(colorMap[0][col] != 0) return true; // ���� ���κб��� �ȼ��� �� �ְ� �� �ȼ��� block�� ���� ���� �ƴ϶�� isTopReached�� ���� ��ȯ�Ѵ�.
		}
		
		return false;
	}
	void clear() { // �� ä���� �ִ� �ٵ��� Ŭ�����ϴ� �޼���
		for(int row = 0; row < Tetris.MAP_HEIGHT; row++) { // �� ���� Ȯ���ϴ� ����
			
			if(isRowFilled(row)) { // ���� �� �� ���� ��� ���� ����� ���� ����� �� ĭ�� �Ʒ��� ������.
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
	} // clear �޼���
	
	void stop() { // ���� ���� Ȥ�� ������ ������ ��
		updateTimer.cancel();
	} // stop �޼���
	
	void resume() {
		if(Tetris.status.comingDisp.type == Block.Type.none) { // ���� ������ ���� ���۵��� �ʾҴٸ� ���� ������ ���, �̹� ���, ���� ����� ��� �������� �̴´�.
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
	} // resume �޼���
	
	void terminate() { // Screen�� Status�� ������ ���� ���·� �����.
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
	} // terminate �޼���
}

class UpdateTask extends TimerTask { // Screen�� �ֱ������� ������Ʈ�ϱ� ���� TimerTask
	
	Random r;
	
	UpdateTask() {
		r = new Random();
	}
	
	public void run() {
		if(Tetris.mainScreen.block.hitTest()) { // hitTest�� ���� ��� ����� ������ �� �� ���� ������ �Ѵ�.
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
			Tetris.status.nextDisp.update(); // ������ �� ����� �������� ����.
			
		} else { // hitTest�� ������ ��� ����� �� �ܰ� ����߷��� �Ѵ�.
			Tetris.mainScreen.block.fallOneStep();
			Tetris.mainScreen.update();
		} // if-else
	} // run �޼���
} // UpdateTask Ŭ����