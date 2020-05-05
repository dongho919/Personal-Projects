package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import io.IoUtil;
import ui.MainFrame;

//AcceptorThread = ��Ʈ �ϳ��� å������ ������. ServerSocket�� �����û�� ���� ������ ServerThread�� �����Ͽ� sockThreads�� �߰��Ѵ�.
public class AcceptorThread extends Thread {
	
	public ServerSocket ss;
	MainFrame main;
	public CopyOnWriteArrayList<ClientThread> usersOnline;
	volatile boolean isRunning;
	
	// ������
	public AcceptorThread(ServerSocket ss, MainFrame main) {
		this.ss = ss;
		this.main = main;
		isRunning = true;
		usersOnline = new CopyOnWriteArrayList<>();
		
		// ����ڰ� ������ ���� ���� �����ϸ� ArrayList���� ����
		Thread connectionMonitor = new Thread() {
			@Override
			public void run() {
				while(true) {
					for(ClientThread thr : usersOnline) {
						if(thr.getState() == Thread.State.TERMINATED) {
							main.console.append("[AcceptorThread] " + thr.getName() + "(��)�� ������ �����߽��ϴ�.\n");
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
		// Ŭ���̾�Ʈ�� accept()�ϰ� ���� �޽����� ǥ���� �� �����带 �����Ѵ�.
		while(isRunning && !ss.isClosed()) {
			try {
				Socket sock = ss.accept();
				main.console.append("[AcceptorThread] " + getName() + ": " + ss.getLocalPort() + "�� ��Ʈ�� Ŭ���̾�Ʈ ����\n");
				
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
		main.console.append("[AcceptorThread] " + getName() + ":" + ss.getLocalPort() + " - ������ �ߴܵ�\n");
		System.out.print("[AcceptorThread] " + getName() + ":" + ss.getLocalPort() + " - ������ �ߴܵ�\n");
	}
}