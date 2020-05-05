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
	
	// ������ �ִ� ����ڵ鿡�� ���ŵ� userList�� ÷�ε� MessagePacket�� ������.
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
	
	// ����ڸ� �߰��ϰ� ���Ͽ� �߰��� �� MainFrame�� userList�� ������Ʈ�Ѵ�.
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
	
	// suspendDialog�� ����
	public static void openSuspendDialog(MainFrame main) {
		new SuspendDialog(main);
	}
	
	// ������ �ִ� ����ڵ鿡�Դ� MessagePacket�� �����ϰ� SuspendData�� NOTIFDATA_LOCATION�� �״�� �����Ѵ�.
	public static void suspendUser(MainFrame main, SuspendData data) {
		CopyOnWriteArrayList<ClientThread> usersOnline = main.server.msgThread.usersOnline;
		
		// ����� ������ ��� �о�鿩 ��ġ�ϴ� �̸��� ã�� ���� �������� �ٲ� ����� ��� ������ �����.
		ArrayList<User> users = readUserList(main);
		ArrayList<String> usersToNotify = new ArrayList<>();
		for(User user : users) {
			if(data.users.contains(user.getName()))
				usersToNotify.add(user.getName());
		}
		
		// �������� ����ڵ鿡�� �˸��� ������.
		NotificationData notifData = new NotificationData();
		notifData.msg = "���� ���� �����Ͻ�:" + (data.suspendedUntil==null ? "���� ����":dateFormat.format(data.suspendedUntil) );
		notifData.users = usersToNotify;
		notifData.expiresOn = data.suspendedUntil;
		sendNotification(usersOnline, notifData);
	}
	
	// �˸� �߽� â�� ����
	public static void openNotificationDialog(MainFrame main) {
		new NotificationDialog(main);
	}
	
	// ClientThread�� �Ѱܹ޾� �ش� �������� notification�� ��� MessagePacket�� �����Ѵ�.
	public static void sendNotification(ClientThread thr, String type, String notification) {
		ObjectOutputStream outgoingMsg = thr.getOutgoingMsg();
		MessagePacket msg = new MessagePacket();
		msg.msgType = type;
		msg.content = notification;
		msg.sendDate = new Date();
		IoUtil.sendMessagePacket(msg, outgoingMsg);
	}
	
	// NotificationData�� ����� ��Ͽ� NotificationData�� �˸� �޽����� �߽��Ѵ�. ���Ŀ� �������ؾ� �� NotificationData�� NOTIFDATA_LOCATION�� �����Ѵ�.
	public static void sendNotification(CopyOnWriteArrayList<ClientThread> usersOnline, NotificationData data) {
		// ���� �˸��� ��쿡�� ����� ������ ����
		// ������ �˸��� ��쿡�� �� �� ������ ����������
		
		ArrayList<NotificationData> notifData = readNotifList();
		// null�� �ƴ϶�� ���� ���� ������ �ƴ϶�� ��
		if(data.expiresOn != null) {
			// ���� ����ڰ� �����ϸ� �ٷ� �������� ������ٸ� �¶����� ����ڵ鸸 ���� �����Ѵ�.
			if(data.expiresOn.getYear() < 100) {
				for(ClientThread thr : usersOnline) {
					if(data.users.contains(thr.getName())) {
						sendNotification(thr, "notif", data.msg);
						data.users.remove(thr.getName());
					}
				}
				notifData.add(data);
			// ���� ����ڰ� �����ص� �ٷ� �������� ���� �޼������� ����ƴٸ� (������ Ǯ�ȴٸ�) ������ �ʰ� notifdata.txt���� ����
			} else if(data.expiresOn.before(new Date())) {
				notifData.remove(data); // FIXME: ���߿� ���۷����� ���̷� ���� ���� ���� ���� ����, �׶��� �̸� ���ؼ� ������ ��
			// ���� ���� ������� ���� �޼������ (�Ͻ� ���� �޼������) �¶��� ����ڵ鿡�� �����⸸ �Ѵ�.
			} else {
				for(ClientThread thr : usersOnline) {
					if(data.users.contains(thr.getName()))
						sendNotification(thr, "suspend", data.msg);
				}
				notifData.add(data);
			}
			
		// ���� ������� ���� ������� ������ ������
		} else {
			for(ClientThread thr : usersOnline)
				sendNotification(thr, "suspend", data.msg);
		}
		writeNotifList(notifData);
	}
	
	// ����� �˸��� �ҷ��´�.
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
	
	// notifdata.txt�� notifArr�� �����Ѵ�.
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
	
	// ����� ������ �о���δ�.
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
	
	// MessagePacket�� ��� �ִ� �α��� ������ ������ ������ ��ġ�ϴ��� Ȯ���Ѵ�.
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
	
	// MainFrame�� userList ������Ʈ�� ����� ������ ��� ArrayList�� �ѱ�� ������Ʈ�� ������Ʈ�Ѵ�.
	public static void updateUserList(MainFrame main) {
		ArrayList<User> users = readUserList(main);
		DefaultListModel<String> newList = new DefaultListModel<String>();
		for(User user : users) 
			newList.addElement(user.getName());
		main.userList.setModel(newList);
		main.userList.repaint();
	}
	
	// ����ڸ� userdata.txt���� �����ϰ� MainFrame�� userList�� ������Ʈ�Ѵ�.
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
