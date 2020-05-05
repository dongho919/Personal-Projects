package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultListModel;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import network.ClientThread;
import network.MessagePacket;
import ui.MainFrame;
import ui.NotificationData;
import ui.NotificationDialog;
import ui.SuspendData;
import ui.SuspendDialog;

public class UserDataUtils {
	public static final String 	 USERDATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\users\\userdata.properties";
	public static final String     NOTIFDATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\users\\notifdata.txt";
	public static final String TMP_NOTIFDATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\users\\notifdata-tmp.txt";
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	public static boolean mkdirs() {
		File userDir = new File(USERDATA_LOCATION.substring(0, USERDATA_LOCATION.lastIndexOf('\\')));
		userDir.mkdirs();
		File userdataFile = new File(USERDATA_LOCATION);
		boolean result = false;
		if(!userdataFile.exists()) {
			try {
				result = userdataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	// 접속해 있는 사용자들에게 갱신된 userList가 첨부된 MessagePacket을 보낸다.
	public static void sendUserListUpdate(MainFrame main, ArrayList<User> users) {
		MessagePacket userListUpdate = new MessagePacket();
		userListUpdate.msgType = "userlistupdate";
		userListUpdate.sendDate = new Date();
		userListUpdate.senderId = MainFrame.ADMIN_ID;
		userListUpdate.attachment = users;
		for(ClientThread thr : main.server.msgThread.usersOnline) {
			IoUtil.sendMessagePacket(userListUpdate, thr.getOutgoingMsg());
		}
	}
	
	// 사용자를 추가하고 파일에 추가한 후 MainFrame의 userList를 업데이트한다.
	public static void addUser(MainFrame main, String senderId, String content) {
		UserManager manager = main.server.serverFactory.getUserManager();
		BaseUser user = new BaseUser();
		user.setName(senderId);
		user.setPassword(content);
		List<Authority> auth = new ArrayList<>();
		auth.add(new WritePermission());
		user.setAuthorities(auth);
		user.setHomeDirectory(VideoDataUtils.VIDEO_LOCATION);
		try {
			manager.save(user);
		} catch (FtpException e) {
			e.printStackTrace();
		}
		updateUserList(main);
		
	}
	
	// suspendDialog을 연다
	public static void openSuspendDialog(MainFrame main) {
		new SuspendDialog(main);
	}
	
	// 접속해 있는 사용자들에게는 MessagePacket을 전송하고 SuspendData를 NOTIFDATA_LOCATION에 그대로 저장한다.
	public static void suspendUser(MainFrame main, SuspendData data) {
		CopyOnWriteArrayList<ClientThread> usersOnline = main.server.msgThread.usersOnline;
		
		// 사용자 정보를 모두 읽어들여 일치하는 이름을 찾고 정지 만료일을 바꿔 사용자 목록 파일을 덮어쓴다.
		ArrayList<User> users = readUserList(main);
		ArrayList<String> usersToNotify = new ArrayList<>();
		for(User user : users) {
			if(data.users.contains(user.getName()))
				usersToNotify.add(user.getName());
		}
		
		// 정지당한 사용자들에게 알림을 보낸다.
		NotificationData notifData = new NotificationData();
		notifData.msg = "계정 정지 만료일시:" + (data.suspendedUntil==null ? "영구 정지":dateFormat.format(data.suspendedUntil) );
		notifData.users = usersToNotify;
		notifData.expiresOn = data.suspendedUntil;
		sendNotification(usersOnline, notifData);
	}
	
	// 알림 발신 창을 띄운다
	public static void openNotificationDialog(MainFrame main) {
		new NotificationDialog(main);
	}
	
	// ClientThread를 넘겨받아 해당 소켓으로 notification이 담긴 MessagePacket을 전송한다.
	public static void sendNotification(ClientThread thr, String type, String notification) {
		ObjectOutputStream outgoingMsg = thr.getOutgoingMsg();
		MessagePacket msg = new MessagePacket();
		msg.msgType = type;
		msg.content = notification;
		msg.sendDate = new Date();
		IoUtil.sendMessagePacket(msg, outgoingMsg);
	}
	
	// NotificationData의 사용자 목록에 NotificationData의 알림 메시지를 발신한다. 추후에 재전송해야 할 NotificationData는 NOTIFDATA_LOCATION에 저장한다.
	public static void sendNotification(CopyOnWriteArrayList<ClientThread> usersOnline, NotificationData data) {
		// 정지 알림의 경우에는 만료될 때까지 남고
		// 나머지 알림의 경우에는 한 번 보내면 없어지도록
		
		ArrayList<NotificationData> notifData = readNotifList();
		// null이 아니라는 말은 영구 정지가 아니라는 뜻
		if(data.expiresOn != null) {
			// 만약 사용자가 수신하면 바로 없어지게 만들었다면 온라인인 사용자들만 빼고 저장한다.
			if(data.expiresOn.getYear() < 100) {
				for(ClientThread thr : usersOnline) {
					if(data.users.contains(thr.getName())) {
						sendNotification(thr, "notif", data.msg);
						data.users.remove(thr.getName());
					}
				}
				notifData.add(data);
			// 만약 사용자가 수신해도 바로 없어지지 않을 메세지지만 만료됐다면 (정지가 풀렸다면) 보내지 않고 notifdata.txt에서 뺀다
			} else if(data.expiresOn.before(new Date())) {
				notifData.remove(data); // FIXME: 나중에 레퍼런스값 차이로 인해 문제 생길 수도 있음, 그때는 이름 비교해서 삭제할 것
			// 만약 아직 만료되지 않은 메세지라면 (일시 정지 메세지라면) 온라인 사용자들에게 보내기만 한다.
			} else {
				for(ClientThread thr : usersOnline) {
					if(data.users.contains(thr.getName()))
						sendNotification(thr, "suspend", data.msg);
				}
				notifData.add(data);
			}
			
		// 영구 정지라면 기한 상관없이 무조건 보낸다
		} else {
			for(ClientThread thr : usersOnline)
				sendNotification(thr, "suspend", data.msg);
		}
		writeNotifList(notifData);
	}
	
	// 저장된 알림을 불러온다.
	public static ArrayList<NotificationData> readNotifList() {
		File in = null;
		ObjectInputStream reader = null;
		ArrayList<NotificationData> notifArr = new ArrayList<>();
		try {
			in = new File(NOTIFDATA_LOCATION);
			if(!in.exists()) {
				writeNotifList(new ArrayList<NotificationData>());
				return new ArrayList<NotificationData>();
			}
			reader = new ObjectInputStream( new FileInputStream(in) );
			notifArr = new ArrayList<>();
			while(true) {
				notifArr.add( (NotificationData) reader.readObject() );
			}
		} catch(IOException e) {
			try {
				reader.close();
			} catch(IOException e1) {
				e1.printStackTrace();
			} catch(NullPointerException e2) {
				e2.printStackTrace();
			}
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return notifArr;
	}
	
	// notifdata.txt에 notifArr를 저장한다.
	public static void writeNotifList(ArrayList<NotificationData> notifArr) {
		for(int i=notifArr.size()-1; i>=0; i--) {
			if(notifArr.get(i).users.isEmpty())
				notifArr.remove(i);
		}
		
		File out = null, originalOut = null;
		ObjectOutputStream writer = null;
		try {
			out = new File(TMP_NOTIFDATA_LOCATION);
			originalOut = new File(NOTIFDATA_LOCATION);
			writer = new ObjectOutputStream( new FileOutputStream(out) );
			for(NotificationData notif : notifArr) {
				writer.writeObject(notif);
			}
			writer.close();
			originalOut.delete();
			out.renameTo(originalOut);
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// 사용자 정보를 읽어들인다.
	public static ArrayList<User> readUserList(MainFrame main) {
		File userdataFile = new File(USERDATA_LOCATION);
		if(!userdataFile.exists()) {
			try {
				userdataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ArrayList<User> userList = new ArrayList<>();
		try {
			UserManager manager = main.server.serverFactory.getUserManager();
			BaseUser user = new BaseUser();
			user.setName(MainFrame.ADMIN_ID);
			user.setPassword(MainFrame.ADMIN_PASSWORD);
			List<Authority> auth = new ArrayList<>();
			auth.add(new WritePermission());
			user.setAuthorities(auth);
			user.setHomeDirectory(VideoDataUtils.VIDEO_LOCATION);
			
			manager.save(user);
		
			String[] userNames = new String[0];
		
			userNames = manager.getAllUserNames();
			for(String userName : userNames) {
				userList.add( manager.getUserByName(userName) );
			}
		} catch (FtpException e) {
			e.printStackTrace();
		}
		return userList;
	}
	
	// MessagePacket에 들어 있는 로그인 정보가 서버의 정보와 일치하는지 확인한다.
	public static boolean isLoginMatch(MainFrame main, MessagePacket loginInfo) {
		ArrayList<User> users = UserDataUtils.readUserList(main);
		for(User user : users) {
			String passwordHash = DigestUtils.md5Hex(loginInfo.content).toUpperCase();
			System.out.println(passwordHash);
			if(user.getName().equals(loginInfo.senderId)) {
				String hash = "";
				try {
					Scanner s = new Scanner(new File(USERDATA_LOCATION));
					while(!hash.contains(user.getName()) || !hash.contains("password")) hash = s.nextLine();
					s.close();
					hash = hash.substring(hash.indexOf('=') + 1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if(passwordHash.equals( hash ))
					return true;
			}
		}
		return false;
	}
	
	// MainFrame의 userList 컴포넌트와 사용자 정보가 담긴 ArrayList를 넘기면 컴포넌트를 업데이트한다.
	public static void updateUserList(MainFrame main) {
		ArrayList<User> users = readUserList(main);
		DefaultListModel<String> newList = new DefaultListModel<String>();
		for(User user : users) 
			newList.addElement(user.getName());
		main.userList.setModel(newList);
		main.userList.repaint();
	}
	
	// 사용자를 userdata.txt에서 삭제하고 MainFrame의 userList를 업데이트한다.
	public static void deleteUser(MainFrame main, String senderId) {
		UserManager manager = main.server.serverFactory.getUserManager();
		try {
			manager.delete(senderId);
		} catch (FtpException e) {
			e.printStackTrace();
		}
		
		ArrayList<User> users = readUserList(main);
		sendUserListUpdate(main, users);
	}
	
}
