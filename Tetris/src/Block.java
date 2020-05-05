public class Block {
	
	enum Type { none(0), straight(1), fuckyou(2), snake_left(3), snake_right(4), standing_left(5), standing_right(6), square(7);
		int num;
		
		Type(int num) {
			this.num = num;
		}
		
		byte toByte() {
			return (byte)num;
		}
	} // ����� ����� ��Ÿ���� ������
	
	private Type type; // ����� � ��������� ��Ÿ���� ����
	int[][] oldCoord = new int[4][2]; // ����� ���� ��ġ�� ��Ÿ���� �迭
	int[][] coord = new int[4][2]; // ����� �����ϴ� ���簢������ ��ǥ�� �����ϴ� �迭, coord[0]�� ȸ���߽��� ��ǥ 
	
	Block(Type type) {
		
		this.type = type;
		
		switch(type) {
		case straight:
			coord[0][0] = 1; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2;
			coord[2][0] = 2; coord[2][1] = Tetris.MAP_WIDTH / 2;
			coord[3][0] = 3; coord[3][1] = Tetris.MAP_WIDTH / 2;
			break;
			
		case fuckyou:
			coord[0][0] = 1; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2 - 1;
			coord[3][0] = 1; coord[3][1] = Tetris.MAP_WIDTH / 2 + 1;
			break;
			
		case snake_left:
			coord[0][0] = 0; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2 - 1;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2;
			coord[3][0] = 1; coord[3][1] = Tetris.MAP_WIDTH / 2 + 1;
			break;
			
		case snake_right:
			coord[0][0] = 0; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2 + 1;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2;
			coord[3][0] = 1; coord[3][1] = Tetris.MAP_WIDTH / 2 - 1;
			break;
			
		case standing_left:
			coord[0][0] = 0; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2 - 1;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2;
			coord[3][0] = 2; coord[3][1] = Tetris.MAP_WIDTH / 2;
			break;
			
		case standing_right:
			coord[0][0] = 0; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2 + 1;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2;
			coord[3][0] = 2; coord[3][1] = Tetris.MAP_WIDTH / 2;
			break;
			
		case square:
			coord[0][0] = 0; coord[0][1] = Tetris.MAP_WIDTH / 2;
			coord[1][0] = 0; coord[1][1] = Tetris.MAP_WIDTH / 2 - 1;
			coord[2][0] = 1; coord[2][1] = Tetris.MAP_WIDTH / 2 - 1;
			coord[3][0] = 1; coord[3][1] = Tetris.MAP_WIDTH / 2;
			break;
		default:
			break;
		}
		
		updateColorMap();
	} // ������
	
	void turnCW() { // ����� �ð�������� ������ �޼���
		if(type != Type.square) {
			int[][] newCoord = new int[4][2];
			
			for(int i = 0; i < 4; i++) {
				newCoord[i][0] = (coord[i][1] - coord[0][1]) + coord[0][0];
				newCoord[i][1] = -(coord[i][0] - coord[0][0]) + coord[0][1]; // ���� �� �κ��� ���߿� ���� ���ϰ��� ����
			}
		
			if(!overlapTest(newCoord)) {
				for(int i = 0; i < 4; i++) {
					oldCoord[i][0] = coord[i][0];
					oldCoord[i][1] = coord[i][1];
					coord[i][0] = newCoord[i][0];
					coord[i][1] = newCoord[i][1];	
				} // for
				updateColorMap();
			} // if
		} // if
	} // turnCW �޼���
	
	void turnCCW() { // ����� �ݽð�������� ������ �޼���
		if(type != Type.square) {
			int[][] newCoord = new int[4][2];
			
			for(int i = 0; i < 4; i++) {
				newCoord[i][0] = -(coord[i][1] - coord[0][1]) + coord[0][0];
				newCoord[i][1] = (coord[i][0] - coord[0][0]) + coord[0][1]; // ���� �� �κ��� ���߿� ���� ���ϰ��� ����
			}
			
			if(!overlapTest(newCoord)) {
				for(int i = 0; i < 4; i++) {
					oldCoord[i][0] = coord[i][0];
					oldCoord[i][1] = coord[i][1];
					coord[i][0] = newCoord[i][0];
					coord[i][1] = newCoord[i][1];	
				} // for
				updateColorMap();
			} // if
		} // if
	} // turnCCW �޼���
	
	void moveLeft() {
		int[][] newCoord = new int[4][2];
		
		for(int i = 0; i < 4; i++) {
			newCoord[i][0] = coord[i][0];
			newCoord[i][1] = coord[i][1] - 1;
		}
		
		if(!overlapTest(newCoord)) {
			for(int i = 0; i < 4; i++) {
				oldCoord[i][0] = coord[i][0];
				oldCoord[i][1] = coord[i][1];
				coord[i][1]--;
			} // for
			updateColorMap();
		} // if
		
	} // moveLeft �޼���
	
	void moveRight() {
		
		int[][] newCoord = new int[4][2];
		
		for(int i = 0; i < 4; i++) {
			newCoord[i][0] = coord[i][0];
			newCoord[i][1] = coord[i][1] + 1;
		}
		
		if(!overlapTest(newCoord)) {
			for(int i = 0; i < 4; i++) {
				oldCoord[i][0] = coord[i][0];
				oldCoord[i][1] = coord[i][1];
				coord[i][1]++;
			} // for
			updateColorMap();
		} // if
	} // moveRight �޼���
	
	void fallOneStep() { // ����� �� �ܰ� ���������� �ϴ� �޼���
		if(!hitTest()) {
			for(int i = 0; i < 4; i++) {
				oldCoord[i][0] = coord[i][0];
				oldCoord[i][1] = coord[i][1];
				coord[i][0]++;
			} // for
		} // if
		updateColorMap();
	} // fallOneStep �޼���
	
	void fallToBottom() {
		while(!hitTest()) {
			fallOneStep();
		} // while
	} // fallToBottom �޼���
	
	boolean overlapTest(int[][] coord) { // ��ȿ�� ������ ����ų� �ٸ� ��ϰ� ��ø�Ǵ��� Ȯ���ϴ� �޼���
		for(int i = 0; i < 4; i++) {
			
			int row = coord[i][0];
			int col = coord[i][1];
			
			if( row < 0 || row >= Tetris.MAP_HEIGHT || // y��ǥ�� ��ȿ�� ������ ����ų�
				col < 0 || col >= Tetris.MAP_WIDTH || // x��ǥ�� ��ȿ�� ������ ����ų�
				(Tetris.mainScreen.colorMap[row][col] != 0 && !isMyPixel(row, col))) { // ����� �����ϴ� �ȼ��� �̹� �����ϴ� �ȼ��� ��ø�Ǵ� ���
				
				return true;
				
			} // if
		} // for
		
		return false; // �� ��� �ȼ��� ���� ���ǿ� �ش���� �ʴ� ��� hitTest�� ������ ��ȯ�Ѵ�.
	} // overlapTest �޼���
	
	boolean hitTest() { // ����� ���� ������ �����ϴ��� Ȯ���ϴ� �޼���
		for(int i = 0; i < 4; i++) {
			
			int row = coord[i][0] + 1;
			int col = coord[i][1];
			
			if( coord[i][0] < 0 || coord[i][0] >= Tetris.MAP_HEIGHT - 1 || // y��ǥ�� ��ȿ�� ������ ����ų�
				coord[i][1] < 0 || coord[i][1] >= Tetris.MAP_WIDTH ||  // x��ǥ�� ��ȿ�� ������ ����ų�
				(Tetris.mainScreen.colorMap[row][col] != 0 && !isMyPixel(row, col))) { // ����� �����ϴ� �ȼ��� �̹� �����ϴ� �ٸ� �ȼ� ��(on top of)�� �ִ� ���
				
				return true;
				
			} // if
		} // for
		
		return false; // �� ��� �ȼ��� ���� ���ǿ� �ش���� �ʴ� ��� hitTest�� ������ ��ȯ�Ѵ�.
	} // hitTest �޼���
	
	boolean isMyPixel(int row, int col) { // �־��� ��ǥ�� �ȼ��� ������ ���� ������ Ȯ���ϴ� �޼���
		 for(int i = 0; i < 4; i++) {
			 if(row == coord[i][0] && col == coord[i][1]) return true; // �ȼ� ��ǥ �����Ϳ� ���� ��ǩ���� ������ true
		 }
		 return false; // ���� ��ǩ���� ã�� ���� ��� false
	}
	
	void updateColorMap() {
		for(int i = 0; i < 4; i++) { // ������ ��ġ���� �ȼ����� �����.
			Tetris.mainScreen.colorMap[oldCoord[i][0]][oldCoord[i][1]] = 0;
		} // for
		for(int i = 0; i < 4; i++) { // �� ��ġ�� �ȼ����� �����Ѵ�.
			Tetris.mainScreen.colorMap[coord[i][0]][coord[i][1]] = type.toByte();
		} // for
	} // updateColorMap �޼���
} // Ŭ����