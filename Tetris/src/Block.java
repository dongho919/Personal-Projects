public class Block {
	
	enum Type { none(0), straight(1), fuckyou(2), snake_left(3), snake_right(4), standing_left(5), standing_right(6), square(7);
		int num;
		
		Type(int num) {
			this.num = num;
		}
		
		byte toByte() {
			return (byte)num;
		}
	} // 블록의 모양을 나타내는 열거형
	
	private Type type; // 블록이 어떤 모양인지를 나타내는 변수
	int[][] oldCoord = new int[4][2]; // 블록의 이전 위치를 나타내는 배열
	int[][] coord = new int[4][2]; // 블록을 구성하는 정사각형들의 좌표를 저장하는 배열, coord[0]은 회전중심의 좌표 
	
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
	} // 생성자
	
	void turnCW() { // 블록을 시계방향으로 돌리는 메서드
		if(type != Type.square) {
			int[][] newCoord = new int[4][2];
			
			for(int i = 0; i < 4; i++) {
				newCoord[i][0] = (coord[i][1] - coord[0][1]) + coord[0][0];
				newCoord[i][1] = -(coord[i][0] - coord[0][0]) + coord[0][1]; // 나도 이 부분을 나중에 이해 못하겠지 껄껄
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
	} // turnCW 메서드
	
	void turnCCW() { // 블록을 반시계방향으로 돌리는 메서드
		if(type != Type.square) {
			int[][] newCoord = new int[4][2];
			
			for(int i = 0; i < 4; i++) {
				newCoord[i][0] = -(coord[i][1] - coord[0][1]) + coord[0][0];
				newCoord[i][1] = (coord[i][0] - coord[0][0]) + coord[0][1]; // 나도 이 부분을 나중에 이해 못하겠지 껄껄
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
	} // turnCCW 메서드
	
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
		
	} // moveLeft 메서드
	
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
	} // moveRight 메서드
	
	void fallOneStep() { // 블록이 한 단계 떨어지도록 하는 메서드
		if(!hitTest()) {
			for(int i = 0; i < 4; i++) {
				oldCoord[i][0] = coord[i][0];
				oldCoord[i][1] = coord[i][1];
				coord[i][0]++;
			} // for
		} // if
		updateColorMap();
	} // fallOneStep 메서드
	
	void fallToBottom() {
		while(!hitTest()) {
			fallOneStep();
		} // while
	} // fallToBottom 메서드
	
	boolean overlapTest(int[][] coord) { // 유효한 범위를 벗어나거나 다른 블록과 중첩되는지 확인하는 메서드
		for(int i = 0; i < 4; i++) {
			
			int row = coord[i][0];
			int col = coord[i][1];
			
			if( row < 0 || row >= Tetris.MAP_HEIGHT || // y좌표가 유효한 범위를 벗어났거나
				col < 0 || col >= Tetris.MAP_WIDTH || // x좌표가 유효한 범위를 벗어났거나
				(Tetris.mainScreen.colorMap[row][col] != 0 && !isMyPixel(row, col))) { // 블록이 차지하는 픽셀이 이미 존재하는 픽셀과 중첩되는 경우
				
				return true;
				
			} // if
		} // for
		
		return false; // 그 어느 픽셀도 위의 조건에 해당되지 않는 경우 hitTest는 거짓을 반환한다.
	} // overlapTest 메서드
	
	boolean hitTest() { // 블록이 굳을 조건을 충족하는지 확인하는 메서드
		for(int i = 0; i < 4; i++) {
			
			int row = coord[i][0] + 1;
			int col = coord[i][1];
			
			if( coord[i][0] < 0 || coord[i][0] >= Tetris.MAP_HEIGHT - 1 || // y좌표가 유효한 범위를 벗어났거나
				coord[i][1] < 0 || coord[i][1] >= Tetris.MAP_WIDTH ||  // x좌표가 유효한 범위를 벗어났거나
				(Tetris.mainScreen.colorMap[row][col] != 0 && !isMyPixel(row, col))) { // 블록이 차지하는 픽셀이 이미 존재하는 다른 픽셀 위(on top of)에 있는 경우
				
				return true;
				
			} // if
		} // for
		
		return false; // 그 어느 픽셀도 위의 조건에 해당되지 않는 경우 hitTest는 거짓을 반환한다.
	} // hitTest 메서드
	
	boolean isMyPixel(int row, int col) { // 주어진 좌표의 픽셀이 나에게 속한 것인지 확인하는 메서드
		 for(int i = 0; i < 4; i++) {
			 if(row == coord[i][0] && col == coord[i][1]) return true; // 픽셀 좌표 데이터에 같은 좌푯값이 있으면 true
		 }
		 return false; // 같은 좌푯값을 찾지 못한 경우 false
	}
	
	void updateColorMap() {
		for(int i = 0; i < 4; i++) { // 원래의 위치에서 픽셀들을 지운다.
			Tetris.mainScreen.colorMap[oldCoord[i][0]][oldCoord[i][1]] = 0;
		} // for
		for(int i = 0; i < 4; i++) { // 새 위치에 픽셀들을 지정한다.
			Tetris.mainScreen.colorMap[coord[i][0]][coord[i][1]] = type.toByte();
		} // for
	} // updateColorMap 메서드
} // 클래스