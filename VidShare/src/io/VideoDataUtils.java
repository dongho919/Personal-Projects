package io;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.ftpserver.ftplet.User;

import com.sun.jna.NativeLibrary;

import client.ui.ClientMainFrame;
import network.ClientThread;
import network.MessagePacket;
import ui.MainFrame;
import ui.VideoPanel;
import ui.video.PlayerFrame;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.media.MediaParsedStatus;
import uk.co.caprica.vlcj.factory.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class VideoDataUtils {
	
	
	public static final String VIDEODATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\users\\videodata.txt"; // TODO: 확장자 jks로 바꾸고 위치 변경하기
	public static final String TMP_VIDEODATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\users\\videodata-tmp.txt";
	public static final String VIDEO_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\videos\\";
	public static final String THUMBNAIL_LOCATION = System.getProperty("user.home") + "\\VidShare\\server\\videos\\thumbnails\\";
	
	public static final String LOCAL_VIDEODATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\videodata.txt";
	public static final String LOCAL_TMP_VIDEODATA_LOCATION = System.getProperty("user.home") + "\\VidShare\\videodata-tmp.txt";
	public static final String LOCAL_VIDEO_LOCATION = System.getProperty("user.home") + "\\VidShare\\";
	
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public static boolean mkdirs() {
		File videoDir = new File(VIDEO_LOCATION);
		return videoDir.mkdirs();
	}
	
	// 접속해 있는 사용자들에게 갱신된 videoList가 첨부된 MessagePacket을 보낸다.
	public static void sendVideoListUpdate(MainFrame main, ArrayList<Video> videos) {
		MessagePacket videoListUpdate = new MessagePacket();
		videoListUpdate.msgType = "videolistupdate";
		videoListUpdate.sendDate = new Date();
		videoListUpdate.senderId = MainFrame.ADMIN_ID;
		videoListUpdate.attachment = videos;
		for(ClientThread thr : main.server.msgThread.usersOnline) {
			IoUtil.sendMessagePacket(videoListUpdate, thr.getOutgoingMsg());
		}
	}
	// 파일 선택기를 열어 다중선택을 받고 선택된 파일들을 VIDEO_LOCATION에 복사하고 해당 정보들을 main.videoList에 추가한다.
	public static File openAddVideoDialog() {
		JFileChooser addVideoDialog = new JFileChooser();
		addVideoDialog.setMultiSelectionEnabled(false);
		FileFilter filter = new FileNameExtensionFilter("동영상 파일 (*.avi, *.mp4, *.wmv, *.flv, *mov)", "avi", "mp4", "wmv", "flv", "mov");
		addVideoDialog.setFileFilter(filter);
		int ret = addVideoDialog.showDialog(null, "파일 열기");
		
		if(ret == JFileChooser.APPROVE_OPTION) {
			return addVideoDialog.getSelectedFile();
		} else {
			return null;
		}
	}
	
	// 주어진 동영상 파일로부터 썸네일을 추출해 저장한다
	public static void saveThumbnail(String filePath) {
		// TODO: 주어진 동영상 파일로부터 썸네일을 추출해 저장한다. png
		
	    MediaPlayerFactory factory = new MediaPlayerFactory();
	    
		MediaPlayer player = factory.mediaPlayers().newMediaPlayer();
		File thumbnail = new File( THUMBNAIL_LOCATION + filePath.substring(filePath.lastIndexOf('\\') + 1, filePath.lastIndexOf('.')) + ".png" );
		player.media().play(filePath);
		player.media().parsing().parse();
		player.controls().setPosition(0.5f);
		while(player.media().info().duration() == -1);
		
		player.media().events().addMediaEventListener(new MediaEventAdapter() {
			
			@Override
			public void mediaParsedChanged(Media arg0, MediaParsedStatus arg1) {
				if(arg1 == MediaParsedStatus.DONE) {
					
					System.out.println("save(thumbnail):" + player.snapshots().save(thumbnail));
				}
			}
		});
		
		
		player.release();
		
		
	}
	
	// 서버 쪽 파일 시스템에서 서버의 비디오 저장소로 복사하는 기능 (네트워크 x)
	public static void addVideo(MainFrame main, Video video, User user) {
		saveThumbnail(user.getHomeDirectory() + video.fileName);
		ArrayList<Video> videos = readVideoList(VIDEODATA_LOCATION);
		videos.add(0, video);
		writeVideoList(videos, VIDEODATA_LOCATION, TMP_VIDEODATA_LOCATION);
		updateVideoList(main.videoList, videos);

		sendVideoListUpdate(main, videos);
	}
	
	public static int deleteServerVideo(MainFrame main, ArrayList<Video> videosToDelete, String id) {
		ArrayList<Video> videos = readVideoList(VIDEODATA_LOCATION);
		int deleteCount = 0;
		for(Video videoToDelete : videosToDelete) {
			for(int i=videos.size()-1; i>=0; i--) {
				if(videos.get(i).fileName.equals(videoToDelete.fileName) && videos.get(i).uploaderId.equals(id)) {
					File fileToDelete = new File(VIDEO_LOCATION + videoToDelete.fileName);
					fileToDelete.delete();
					videos.remove(i);
					deleteCount++;
					break;
				}
			}
		}
		writeVideoList(videos, VIDEODATA_LOCATION, TMP_VIDEODATA_LOCATION);
		updateVideoList(main.videoList, videos);
		
		sendVideoListUpdate(main, videos);
		
		return deleteCount;
	}
	
	// 넘겨받은 인자들을 이용해 전체 리스트에서 어떤 비디오들의 fileName이 일치하는지 확인 후, 그것들을 지워서 파일 목록에 덮어쓰기
	// 네트워크 x
	public static void deleteLocalVideo(JFrame frame, String videoLocation) {
		if(videoLocation.equals(VIDEO_LOCATION)) {
			MainFrame main = (MainFrame) frame;
			ArrayList<Video> videos = readVideoList(VIDEODATA_LOCATION);
			int[] indices = main.videoList.getSelectedIndices();
			
			for(int i=videos.size()-1; i>=0; i--) {
				for(int j : indices) {
					String fileName1 = videos.get(i).fileName;
					String fileName2 = main.videoList.getModel().getElementAt(j).video.fileName;
					if(fileName1.equals(fileName2)) {
						File videoFile = new File(VIDEO_LOCATION + fileName1);
						videoFile.delete();
						videos.remove(i);
						break;
					}
				}
			}
			writeVideoList(videos, VIDEODATA_LOCATION, TMP_VIDEODATA_LOCATION);
			updateVideoList(main.videoList, videos);
			
			sendVideoListUpdate(main, videos);
		
		} else {
			ClientMainFrame main = (ClientMainFrame) frame;
			ArrayList<Video> videos = readVideoList(LOCAL_VIDEODATA_LOCATION);
			int[] indices = main.localVideoList.getSelectedIndices();
			
			for(int i=videos.size()-1; i>=0; i--) {
				for(int j : indices) {
					String fileName1 = videos.get(i).fileName;
					String fileName2 = main.localVideoList.getModel().getElementAt(j).video.fileName;
					if(fileName1.equals(fileName2)) {
						File videoFile = new File(LOCAL_VIDEO_LOCATION + fileName1);
						videoFile.delete();
						videos.remove(i);
						break;
					}
				}
			}
			writeVideoList(videos, LOCAL_VIDEODATA_LOCATION, LOCAL_TMP_VIDEODATA_LOCATION);
			updateVideoList(main.localVideoList, videos);
		}
	}
	
	// 동영상을 별도의 창에서 재생한다. 만약 videoList에 선택된 항목이 여러 개라면 첫째 항목을 연다.
	public static void playVideo(JList<VideoPanel> videoList, String location, boolean isServer) {
		String fileName = videoList.getModel().getElementAt(videoList.getLeadSelectionIndex()).video.fileName;
		new PlayerFrame(location + fileName);
	}
	
	// VIDEODATA_LOCATION으로부터 비디오 목록을 읽어들여 ArrayList로 반환한다.
	public static ArrayList<Video> readVideoList(String location) {
		File in = null;
		ObjectInputStream reader = null;
		ArrayList<Video> videoArr = null;
		try {
			in = new File(location);
			if(!in.exists()) {
				writeVideoList(new ArrayList<Video>(), location, location.equals(VIDEODATA_LOCATION) ? TMP_VIDEODATA_LOCATION:LOCAL_TMP_VIDEODATA_LOCATION);
				return new ArrayList<Video>();
			}
			reader = new ObjectInputStream( new FileInputStream(in) );
			videoArr = new ArrayList<>();
			while(true) {
				videoArr.add( (Video)reader.readObject() );
			}
		} catch(IOException e) {
			try {
				reader.close();
			} catch(IOException e1) {}
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return videoArr;
	}
	
	// VIDEODATA_LOCATION에 새로운 비디오 목록을 덮어쓴다.
	public static void writeVideoList(ArrayList<Video> videos, String location, String tmpLocation) {
		File out = null, originalOut = null;
		ObjectOutputStream writer = null;
		try {
			out = new File(tmpLocation);
			originalOut = new File(location);
			writer = new ObjectOutputStream( new FileOutputStream(out) );
			for(Video video : videos) {
				writer.writeObject(video);
			}
			writer.close();
			originalOut.delete();
			out.renameTo(originalOut);
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// MainFrame의 videoList와 비디오 정보가 담긴 ArrayList를 넘겨받아 videoList를 업데이트한다.
	public static void updateVideoList(JList<VideoPanel> videoList, ArrayList<Video> videos) {
		DefaultListModel<VideoPanel> newList = new DefaultListModel<>();
		for(Video video : videos)
			newList.addElement( new VideoPanel(video) );
		videoList.setModel(newList);
		videoList.repaint();
	}
	
	public static ArrayList<Video> searchVideoList(String searchText, int selectedIndex) {
			ArrayList<Video> videos = readVideoList(VIDEODATA_LOCATION);
			ArrayList<Video> matchList = new ArrayList<>();
			for(Video video : videos) {
				switch(selectedIndex) {
				
				// 제목으로 검색
				case 0:
					if(containsIgnoreCase(video.title, searchText))
						matchList.add(video);
					break;
					
				// 업로더로 검색
				case 1:
					if(containsIgnoreCase(video.uploaderId, searchText))
						matchList.add(video);
					break;
					
				// 태그로 검색
				case 2:
					if(containsIgnoreCase(video.tags, searchText))
						matchList.add(video);
					break;
				}
			}
			return matchList;
	}
	
	public static void streamVideo(Video video) {
		
		     // your VLC installation path
		     NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");

		     String media = VIDEO_LOCATION + video.fileName; // example = file:///C:/test.mp4
		     // you are gonna use below value on the client 
		     String[] options = {":sout=#duplicate{dst=display,dst=rtp{dst=localhost,port=6758,mux=ts}}"};

		     MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(); 
		     MediaApi mediaPlayer = mediaPlayerFactory.media();
		     mediaPlayer.newMedia(media, options[0], ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep");

		     // Don't exit
		     try {
				Thread.currentThread().join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	private static boolean containsIgnoreCase(String str, String searchText) {
		return str.toUpperCase().contains( searchText.toUpperCase() );
	}
	private static boolean containsIgnoreCase(String[] strArr, String searchText) {
		for(String str : strArr) {
			if(containsIgnoreCase(str, searchText)) return true;
		}
		return false;
	}

}
