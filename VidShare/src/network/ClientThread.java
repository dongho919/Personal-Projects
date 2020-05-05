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
	
	
	// incomingData = 파일을 수신하기 위한 스트림
	// incomingMsg = MessagePacket 객체를 수신하기 위한 스트림
	// fileInStream/fileOutStream = 파일을 컴퓨터에 쓰고 읽기 위한 스트림
	// buf = 파일을 수신 및 읽기/쓰기 할 때 사용할 8MB 버퍼
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
			main.console.append("[" + getName() + "] 메시지 스트림 생성 완료: " + incomingMsg + ", " + outgoingMsg + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while(isRunning && sock.isConnected()) {
			MessagePacket mp = IoUtil.receiveMessagePacket(incomingMsg);
			main.console.append("[" + getName() + "] 메시지 수신: " + mp + "\n");
			// 만약 로그인 MessagePacket이면 정보를 확인하고, 일치할 경우 스레드의 이름을 바꾼다.
			if(mp.msgType.equals("login")) {
				if(UserDataUtils.isLoginMatch(main, mp)) {
					setName(mp.senderId);
					ArrayList<NotificationData> notifData = UserDataUtils.readNotifList();
					main.console.append("[" + getName() + "] 로그인\n");
					
					// 저장된 알림을 하나씩 불러와 해당 사용자에게 보낼 내용들만 추출해 보낸다
					boolean isSuspended = false;
					for(NotificationData data : notifData) {
						for(int i=0; i<data.users.size(); i++) {
							String userId = data.users.get(i);
							// 현재 처리하고 있는 notifData에서 일치하는 이름이 나오면 알림 발신 - notifData에서 사용자 삭제 - 
							if(mp.senderId.equals(userId))	{
								// 영구 정지이거나 정지 기한이 만료되지 않았다면 알림만 보낸다.
								if( data.expiresOn == null || data.expiresOn.after(new Date())) {
									UserDataUtils.sendNotification(this, "suspend", data.msg);
									isSuspended = true;
								// 한 번만 받을 알림이라면 알림을 보내고 data.users에서 사용자 이름을 삭제한다.
								} else if( data.expiresOn.getYear() < 100) {
									UserDataUtils.sendNotification(this, "notif", data.msg);
									data.users.remove(i);
								// 날짜가 지난 알림이라면 바로 삭제한다.
								} else {
									data.users.remove(i);
								}
							}
						}
					}
					if(!isSuspended) {
						UserDataUtils.sendNotification(this, "loginsuccess", "로그인에 성공하였습니다.");
						VideoDataUtils.sendVideoListUpdate(main, VideoDataUtils.readVideoList(VideoDataUtils.VIDEODATA_LOCATION) );
					}
					
					UserDataUtils.writeNotifList(notifData);
				} else {
					UserDataUtils.sendNotification(this, "loginfail", "로그인에 실패하였습니다.");
				}
				
			// 회원탈퇴 요청을 처리한다.
			} else if(mp.msgType.equals("deleteuser")) {
				if(UserDataUtils.isLoginMatch(main, mp)) {
					UserDataUtils.deleteUser(main, mp.senderId);
					UserDataUtils.sendNotification(this, "deleteusersuccess", "성공적으로 탈퇴되었습니다.");
				} else {
					UserDataUtils.sendNotification(this, "deleteuserfail", "비밀번호가 일치하지 않습니다. 다시 시도해 주세요.");
				}
				
			// 회원가입 요청을 처리한다. 아이디 중복확인은 이미 이루어졌다고 가정한다.
			} else if(mp.msgType.equals("signup")) {
				UserDataUtils.addUser(main, mp.senderId, mp.content);
			
			// 아이디 중복확인을 해서 알림을 보낸다.
			} else if(mp.msgType.equals("idcheck")) {
				
				try {
					if(!main.server.serverFactory.getUserManager().doesExist(mp.senderId)) UserDataUtils.sendNotification(this, "idchecksuccess", "사용 가능한 아이디입니다.");
					else UserDataUtils.sendNotification(this, "idcheckfail", "중복되는 아이디입니다.");
				} catch (FtpException e) {
					e.printStackTrace();
				}
			} else if(mp.msgType.equals("uploadcomplete")) {
				Video video = (Video)mp.attachment;
				main.console.append(String.format("[%s] 파일 업로드 완료: %s\n", getName(), video.fileName));
				try {
					VideoDataUtils.addVideo(main, video, main.server.serverFactory.getUserManager().getUserByName(mp.senderId));
				} catch (FtpException e) {
					e.printStackTrace();
				}
			} else if(mp.msgType.equals("downloadcomplete")) {
				Video video = (Video)mp.attachment;
				main.console.append(String.format("[%s] 파일 다운로드 완료: %s\n", getName(), video.fileName));
				
			} else if(mp.msgType.equals("deletevideo")) {
				ArrayList<Video> videosToDelete = (ArrayList<Video>)mp.attachment;
				int deleteCount = VideoDataUtils.deleteServerVideo(main, videosToDelete, mp.senderId);
				UserDataUtils.sendNotification(this, "deletefail", "삭제 실패: " + (videosToDelete.size() - deleteCount) + "개");
				
			// 영상 검색 요청을 받고 회신을 한다.
			} else if(mp.msgType.equals("searchvideo")) {
				main.console.append("ClientThread 비디오 검색 요청 수신: " + getName() + "\n");
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