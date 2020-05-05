package client.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import client.ui.ClientMainFrame;
import io.IoUtil;
import io.Video;
import io.VideoDataUtils;
import network.MessagePacket;
import network.Server;

public class FileTransfer {
	
	ClientMainFrame main;
	
	Thread uploadThread;
	Thread downloadThread;
	CopyOnWriteArrayList<TransferThread> uploads;
	CopyOnWriteArrayList<TransferThread> downloads;
	
	public FileTransfer(ClientMainFrame main) {
		this.main = main;
		uploads = new CopyOnWriteArrayList<>();
		downloads = new CopyOnWriteArrayList<>();
		
		uploadThread = new Thread() {
			public void run() {
				while(true) {
					for(TransferThread thr : uploads) {
						if(!thr.client.isConnected()) uploads.remove(thr);
					}
				}
			}
		};
		downloadThread = new Thread() {
			public void run() {
				while(true) {
					for(TransferThread thr : downloads) {
						if(!thr.client.isConnected()) downloads.remove(thr);
					}
				}
			}
		};
		
		uploadThread.setPriority(Thread.MIN_PRIORITY);
		downloadThread.setPriority(Thread.MIN_PRIORITY);
		uploadThread.start();
		downloadThread.start();
	}
	
	public void upload(Video video) {
		TransferThread transferThread = new TransferThread(video, true, main.id, main.password);
		transferThread.client.addProtocolCommandListener(new ProtocolCommandListener() {
			@Override
			public void protocolReplyReceived(ProtocolCommandEvent arg0) {
				System.out.println(arg0.getMessage());
				if(arg0.getReplyCode() == 226 && arg0.getMessage().contains("Transfer complete.")) {
					
					MessagePacket uploadComplete = new MessagePacket();
					uploadComplete.msgType = "uploadcomplete";
					uploadComplete.attachment = transferThread.video;
					uploadComplete.sendDate = new Date();
					uploadComplete.senderId = main.id;
					IoUtil.sendMessagePacket(uploadComplete, main.msgThread.outgoingMsg);
				}
			}
			
			@Override
			public void protocolCommandSent(ProtocolCommandEvent arg0) {}
		});
		transferThread.start();
		
	}
	public void download(Video video) {
		
		TransferThread transferThread = new TransferThread(video, false, main.id, main.password);
		transferThread.client.addProtocolCommandListener(new ProtocolCommandListener() {
			@Override
			public void protocolReplyReceived(ProtocolCommandEvent arg0) {
				System.out.println(arg0.getMessage());
				if(arg0.getReplyCode() == 226) {
					ArrayList<Video> videos = VideoDataUtils.readVideoList(VideoDataUtils.LOCAL_VIDEODATA_LOCATION);
					videos.add(0, transferThread.video);
					VideoDataUtils.updateVideoList(main.localVideoList, videos);
					VideoDataUtils.writeVideoList(videos, VideoDataUtils.LOCAL_VIDEODATA_LOCATION, VideoDataUtils.LOCAL_TMP_VIDEODATA_LOCATION);
					
					MessagePacket downloadComplete = new MessagePacket();
					downloadComplete.msgType = "downloadcomplete";
					downloadComplete.attachment = transferThread.video;
					downloadComplete.sendDate = new Date();
					downloadComplete.senderId = main.id;
					IoUtil.sendMessagePacket(downloadComplete, main.msgThread.outgoingMsg);
				}
			}
			
			@Override
			public void protocolCommandSent(ProtocolCommandEvent arg0) {}
		});
		transferThread.start();
	}
		
	// videoList에서 선택된 값들에 대해 각각의 파일에 대해 소켓을 생성하여 다운로드를 진행한다.
	public void downloadVideo(ClientMainFrame main) {
		for(int i : main.videoList.getSelectedIndices()) {
			Video downloadVideo = main.videoList.getModel().getElementAt(i).video;
			download(downloadVideo);
		}
		
	}

	public void uploadVideo(Video video) {
		upload(video);
	}
}

class TransferThread extends Thread {

	public FTPClient client;
	public volatile Video video;
	private boolean isUpload;
	private String id;
	private String password;
	
	public TransferThread(Video video, boolean isUpload, String id, String password) {
		client = new FTPClient();
		this.video = video;
		this.isUpload = isUpload;
		this.id = id;
		this.password = password;
	}
	
	@Override
	public void run() {
		try {
	    	int reply;
	    	client.setBufferSize(IoUtil.FTP_BUFFER_SIZE);
	    	client.setControlEncoding("UTF-8");
	    	System.out.println(client.getReceiveDataSocketBufferSize() + "\n" + client.getSendDataSocketBufferSize() + "\n" + client.getBufferSize());
	    	
	    	client.connect(ClientMainFrame.HOST_ADDRESS, Server.FTP_PORT);
	    	
	    	reply = client.getReplyCode();
		    if(!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
		    	System.out.println("FTP 연결에 실패했습니다.");
		    } else {
		    	client.login(id, password);
		    	client.setFileType(FTP.BINARY_FILE_TYPE);
		    	client.enterLocalPassiveMode();
		    	if(isUpload) {
			    	File inFile = new File(video.fileName);
			    	BufferedInputStream in = new BufferedInputStream(new FileInputStream(inFile));
			    	
			    	String numberedFileName = inFile.getName();
			    	String[] fileList = client.listNames();
			    	String noExtensionName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
			    	String extension = inFile.getName().substring(inFile.getName().lastIndexOf('.'));
			    	int count = 1;
			    	for(int i=0; i<fileList.length; i++)
			    		fileList[i] = fileList[i].toUpperCase();
			    	System.out.println("fileList: " + Arrays.toString(fileList));
			    	while(Arrays.asList(fileList).contains(numberedFileName.toUpperCase())) {
			    		System.out.println(numberedFileName);
			    		numberedFileName = String.format("%s (%d)%s", noExtensionName, count, extension);
			    		count++;
			    	}
			    	
			    	video = ((Video)video.clone());
			    	video.fileName = numberedFileName;
			    	
			    	client.storeFile(numberedFileName, in);
			    	in.close();
		    	} else {
		    		File outFile = new File(VideoDataUtils.LOCAL_VIDEO_LOCATION + video.fileName);
		    		String originalFileName = outFile.getName();
		    		String noExtensionPath = outFile.getPath().substring(0, outFile.getPath().lastIndexOf('.'));
		    		String extension = outFile.getPath().substring(outFile.getPath().lastIndexOf('.'));
			    	int count = 1;
			    	while(outFile.exists()) {
			    		outFile = new File(String.format("%s (%d)%s", noExtensionPath, count, extension));
			    		count++;
			    	}
			    	
			    	video = ((Video)video.clone());
			    	video.fileName = outFile.getName();
			    	
		    		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
		    		client.retrieveFile(originalFileName, out);
		    		out.close();
		    	}
		    	client.logout();
		    	client.disconnect();
		    }
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	}
	
}
