package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.ftpserver.ftplet.FtpException;

import io.IoUtil;
import io.UserDataUtils;
import io.Video;
import io.VideoDataUtils;
import ui.MainFrame;
import ui.NotificationData;

public class ClientThread extends Thread {
	
	volatile boolean isRunning;
	public Socket sock;
	
	
	// incomingData = ������ �����ϱ� ���� ��Ʈ��
	// incomingMsg = MessagePacket ��ü�� �����ϱ� ���� ��Ʈ��
	// fileInStream/fileOutStream = ������ ��ǻ�Ϳ� ���� �б� ���� ��Ʈ��
	// buf = ������ ���� �� �б�/���� �� �� ����� 8MB ����
	ObjectOutputStream outgoingMsg;
	ObjectInputStream incomingMsg;
	
	MainFrame main;
	
	public ObjectOutputStream getOutgoingMsg() { return outgoingMsg; }
	public ObjectInputStream getIncomingMsg() { return incomingMsg; }
	
	
	public ClientThread(Socket sock, MainFrame main) {
		this.main = main;
		
		isRunning = true;
		this.sock = sock;
		
		try {
			outgoingMsg = new ObjectOutputStream( sock.getOutputStream() );
			outgoingMsg.flush();
			incomingMsg = new ObjectInputStream( sock.getInputStream() );
			main.console.append("[" + getName() + "] �޽��� ��Ʈ�� ���� �Ϸ�: " + incomingMsg + ", " + outgoingMsg + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while(isRunning && sock.isConnected()) {
			MessagePacket mp = IoUtil.receiveMessagePacket(incomingMsg);
			main.console.append("[" + getName() + "] �޽��� ����: " + mp + "\n");
			// ���� �α��� MessagePacket�̸� ������ Ȯ���ϰ�, ��ġ�� ��� �������� �̸��� �ٲ۴�.
			if(mp.msgType.equals("login")) {
				if(UserDataUtils.isLoginMatch(main, mp)) {
					setName(mp.senderId);
					ArrayList<NotificationData> notifData = UserDataUtils.readNotifList();
					main.console.append("[" + getName() + "] �α���\n");
					
					// ����� �˸��� �ϳ��� �ҷ��� �ش� ����ڿ��� ���� ����鸸 ������ ������
					boolean isSuspended = false;
					for(NotificationData data : notifData) {
						for(int i=0; i<data.users.size(); i++) {
							String userId = data.users.get(i);
							// ���� ó���ϰ� �ִ� notifData���� ��ġ�ϴ� �̸��� ������ �˸� �߽� - notifData���� ����� ���� - 
							if(mp.senderId.equals(userId))	{
								// ���� �����̰ų� ���� ������ ������� �ʾҴٸ� �˸��� ������.
								if( data.expiresOn == null || data.expiresOn.after(new Date())) {
									UserDataUtils.sendNotification(this, "suspend", data.msg);
									isSuspended = true;
								// �� ���� ���� �˸��̶�� �˸��� ������ data.users���� ����� �̸��� �����Ѵ�.
								} else if( data.expiresOn.getYear() < 100) {
									UserDataUtils.sendNotification(this, "notif", data.msg);
									data.users.remove(i);
								// ��¥�� ���� �˸��̶�� �ٷ� �����Ѵ�.
								} else {
									data.users.remove(i);
								}
							}
						}
					}
					if(!isSuspended) {
						UserDataUtils.sendNotification(this, "loginsuccess", "�α��ο� �����Ͽ����ϴ�.");
						VideoDataUtils.sendVideoListUpdate(main, VideoDataUtils.readVideoList(VideoDataUtils.VIDEODATA_LOCATION) );
					}
					
					UserDataUtils.writeNotifList(notifData);
				} else {
					UserDataUtils.sendNotification(this, "loginfail", "�α��ο� �����Ͽ����ϴ�.");
				}
				
			// ȸ��Ż�� ��û�� ó���Ѵ�.
			} else if(mp.msgType.equals("deleteuser")) {
				if(UserDataUtils.isLoginMatch(main, mp)) {
					UserDataUtils.deleteUser(main, mp.senderId);
					UserDataUtils.sendNotification(this, "deleteusersuccess", "���������� Ż��Ǿ����ϴ�.");
				} else {
					UserDataUtils.sendNotification(this, "deleteuserfail", "��й�ȣ�� ��ġ���� �ʽ��ϴ�. �ٽ� �õ��� �ּ���.");
				}
				
			// ȸ������ ��û�� ó���Ѵ�. ���̵� �ߺ�Ȯ���� �̹� �̷�����ٰ� �����Ѵ�.
			} else if(mp.msgType.equals("signup")) {
				UserDataUtils.addUser(main, mp.senderId, mp.content);
			
			// ���̵� �ߺ�Ȯ���� �ؼ� �˸��� ������.
			} else if(mp.msgType.equals("idcheck")) {
				
				try {
					if(!main.server.serverFactory.getUserManager().doesExist(mp.senderId)) UserDataUtils.sendNotification(this, "idchecksuccess", "��� ������ ���̵��Դϴ�.");
					else UserDataUtils.sendNotification(this, "idcheckfail", "�ߺ��Ǵ� ���̵��Դϴ�.");
				} catch (FtpException e) {
					e.printStackTrace();
				}
			} else if(mp.msgType.equals("uploadcomplete")) {
				Video video = (Video)mp.attachment;
				main.console.append(String.format("[%s] ���� ���ε� �Ϸ�: %s\n", getName(), video.fileName));
				try {
					VideoDataUtils.addVideo(main, video, main.server.serverFactory.getUserManager().getUserByName(mp.senderId));
				} catch (FtpException e) {
					e.printStackTrace();
				}
			} else if(mp.msgType.equals("downloadcomplete")) {
				Video video = (Video)mp.attachment;
				main.console.append(String.format("[%s] ���� �ٿ�ε� �Ϸ�: %s\n", getName(), video.fileName));
				
			} else if(mp.msgType.equals("deletevideo")) {
				ArrayList<Video> videosToDelete = (ArrayList<Video>)mp.attachment;
				int deleteCount = VideoDataUtils.deleteServerVideo(main, videosToDelete, mp.senderId);
				UserDataUtils.sendNotification(this, "deletefail", "���� ����: " + (videosToDelete.size() - deleteCount) + "��");
				
			// ���� �˻� ��û�� �ް� ȸ���� �Ѵ�.
			} else if(mp.msgType.equals("searchvideo")) {
				main.console.append("ClientThread ���� �˻� ��û ����: " + getName() + "\n");
				Scanner s = new Scanner(mp.content);
				String searchText = s.nextLine();
				int selectedIndex = Integer.parseInt(s.nextLine());
				s.close();
				VideoDataUtils.sendVideoListUpdate(main, VideoDataUtils.searchVideoList(searchText, selectedIndex));
			}
		}
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}