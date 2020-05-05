package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import io.IoUtil;
import ui.MainFrame;

//AcceptorThread = 포트 하나를 책임지는 스레드. ServerSocket에 연결요청이 들어올 때마다 ServerThread를 생성하여 sockThreads에 추가한다.
public class AcceptorThread extends Thread {
	
	public ServerSocket ss;
	MainFrame main;
	public CopyOnWriteArrayList<ClientThread> usersOnline;
	volatile boolean isRunning;
	
	// 생성자
	public AcceptorThread(ServerSocket ss, MainFrame main) {
		this.ss = ss;
		this.main = main;
		isRunning = true;
		usersOnline = new CopyOnWriteArrayList<>();
		
		// 사용자가 연결을 끊는 것을 감지하면 ArrayList에서 뺀다
		Thread connectionMonitor = new Thread() {
			@Override
			public void run() {
				while(true) {
					for(ClientThread thr : usersOnline) {
						if(thr.getState() == Thread.State.TERMINATED) {
							main.console.append("[AcceptorThread] " + thr.getName() + "(이)가 접속을 해제했습니다.\n");
							usersOnline.remove(thr);
						}
					}
				}
			}
		};
		connectionMonitor.setPriority(MIN_PRIORITY);
		connectionMonitor.start();
	}
	
	@Override
	public void run() {
		// 클라이언트를 accept()하고 접속 메시지를 표시한 후 스레드를 생성한다.
		while(isRunning && !ss.isClosed()) {
			try {
				Socket sock = ss.accept();
				main.console.append("[AcceptorThread] " + getName() + ": " + ss.getLocalPort() + "번 포트에 클라이언트 접속\n");
				
				ClientThread thr = new ClientThread(sock, main);
				sock.setKeepAlive(true);
				thr.setPriority(MIN_PRIORITY);
				usersOnline.add(thr);
				thr.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void halt() {
		isRunning = false;
		main.console.append("[AcceptorThread] " + getName() + ":" + ss.getLocalPort() + " - 스레드 중단됨\n");
		System.out.print("[AcceptorThread] " + getName() + ":" + ss.getLocalPort() + " - 스레드 중단됨\n");
	}
}