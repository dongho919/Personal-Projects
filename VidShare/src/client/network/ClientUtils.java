package client.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;


import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import client.ui.ClientMainFrame;
import io.IoUtil;
import io.Video;
import io.VideoDataUtils;
import network.MessagePacket;

public class ClientUtils extends VideoDataUtils {
	public static void sendLoginRequest(MessageThread msgThread, String id, char[] password) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "login";
		packet.senderId = id;
		packet.content = new String(password);
		packet.sendDate = new Date();
		IoUtil.sendMessagePacket(packet, msgThread.outgoingMsg);
	}

	public static void sendIdCheck(MessageThread msgThread, String id) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "idcheck";
		packet.senderId = id;
		packet.sendDate = new Date();
		IoUtil.sendMessagePacket(packet, msgThread.outgoingMsg);
	}

	public static void sendSignupRequest(MessageThread msgThread, String id, char[] password) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "signup";
		packet.senderId = id;
		packet.content = new String(password);
		packet.sendDate = new Date();
		IoUtil.sendMessagePacket(packet, msgThread.outgoingMsg);
	}
	
	public static void sendDeleteUserRequest(MessageThread msgThread, String id, char[] password) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "deleteuser";
		packet.senderId = id;
		packet.content = new String(password);
		packet.sendDate = new Date();
		IoUtil.sendMessagePacket(packet, msgThread.outgoingMsg);
	}
	
	public static void sendDeleteVideoRequest(ClientMainFrame main) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "deletevideo";
		packet.senderId = main.id;
		packet.sendDate = new Date();
		
		int[] indices = main.videoList.getSelectedIndices();
		ArrayList<Video> videos = new ArrayList<>();
		for(int index : indices) {
			videos.add( main.videoList.getModel().getElementAt(index).video) ;
		}
		packet.attachment = videos;
		IoUtil.sendMessagePacket(packet, main.msgThread.outgoingMsg);
	}

	public static void sendSearchVideoRequest(ClientMainFrame main, String searchText, int selectedIndex) {
		MessagePacket packet = new MessagePacket();
		packet.msgType = "searchvideo";
		packet.senderId = main.id;
		packet.content = searchText + "\n" + selectedIndex;
		packet.sendDate = new Date();
		IoUtil.sendMessagePacket(packet, main.msgThread.outgoingMsg);
	}
	
	public static void vlcSetup() {
		try {
			File dir = new File(System.getProperty("user.home") + "\\VidShare");
			dir.mkdir();
			
			String regValue = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\VideoLan\\VLC", "InstallDir");
			System.out.println("VLC InstallDir: " + regValue);
		} catch(com.sun.jna.platform.win32.Win32Exception e) {
			if(System.getProperty("sun.arch.data.model") != null) {
				try {
					InputStream is = ClientUtils.class.getResource("/vlc-3.0.8-win" +
				System.getProperty("sun.arch.data.model") + ".exe").
							openStream();
					OutputStream os = new FileOutputStream(System.getProperty("user.home") + "\\VidShare\\vlc-installer.exe");
					byte[] b = new byte[2048];
					int length;
					while((length = is.read(b)) > 0)
						os.write(b, 0, length);
					
					os.close();
					
					InputStream elevateIs = ClientUtils.class.getResourceAsStream("/Elevate.exe");
					OutputStream elevateOs = new FileOutputStream(System.getProperty("user.home") + "\\VidShare\\Elevate.exe");
					while((length = elevateIs.read(b)) > 0)
						elevateOs.write(b, 0, length);
					
					elevateOs.close();
					
					Process install = Runtime.getRuntime().exec("cmd /c start /wait " + System.getProperty("user.home") + "\\VidShare\\Elevate " + System.getProperty("user.home") + "\\VidShare\\vlc-installer.exe");
					install.waitFor();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
//	public static void openUploadVideoDialog(ClientMainFrame main) {
//		JFileChooser uploadVideoDialog = new JFileChooser();
//		uploadVideoDialog.setMultiSelectionEnabled(false);
//		FileFilter filter = new FileNameExtensionFilter("동영상 파일 (*.avi, *.mp4, *.wmv, *.flv, *mov)", "avi", "mp4", "wmv", "flv", "mov");
//		uploadVideoDialog.setFileFilter(filter);
//		int ret = uploadVideoDialog.showDialog(main, "파일 열기");
//		
//		if(ret == JFileChooser.APPROVE_OPTION) {
//			File uploadFile = uploadVideoDialog.getSelectedFile();
//			//main.uploadFileName.setText(uploadFile.getPath());
//			
//			//////////////////////
//			main.fileTransfer.uploadVideo(uploadFile.getName(), null, uploadFile.getPath());
//		}
//		
//	}
	
}
