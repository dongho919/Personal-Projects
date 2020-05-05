package network;

import java.io.File;
import java.net.ServerSocket;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.UserDataHandler;

import client.ui.ClientMainFrame;
import io.UserDataUtils;
import io.VideoDataUtils;
import ui.MainFrame;

public class Server {
	
	public static final int MESSAGE_PORT = 6756;
	public static final int FTP_PORT = 6757;
	
	public AcceptorThread msgThread;
	public FtpServerFactory serverFactory;
	public ListenerFactory factory;
	public FtpServer server;
	
	public Server(MainFrame main) {
		
		// 각 기능에 대한 서버소켓을 생성하여 스레드를 할당한다.
		ServerSocket mp = null;
		try {
			mp = new ServerSocket(MESSAGE_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		msgThread = new AcceptorThread(mp, main);
		msgThread.setPriority(Thread.MIN_PRIORITY);
		msgThread.start();
		
		BasicConfigurator.configure();
		
		serverFactory = new FtpServerFactory();
		factory = new ListenerFactory();
		
		factory.setPort(FTP_PORT);
		
		DataConnectionConfigurationFactory configFactory = new DataConnectionConfigurationFactory();
		configFactory.setPassivePorts("6758-10000");
		configFactory.setPassiveExternalAddress(ClientMainFrame.HOST_ADDRESS);
		factory.setDataConnectionConfiguration(configFactory.createDataConnectionConfiguration());
		
		//SslConfigurationFactory ssl = new SslConfigurationFactory();
		//ssl.setKeystoreFile(new File(VideoDataUtils.VIDEODATA_LOCATION));
		//ssl.setKeystorePassword("980919");
		
		//factory.setSslConfiguration(ssl.createSslConfiguration());
		factory.setImplicitSsl(false);
		
		serverFactory.addListener("default", factory.createListener());
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setFile(new File(UserDataUtils.USERDATA_LOCATION));
		serverFactory.setUserManager(userManagerFactory.createUserManager());
		
		FtpServer server = serverFactory.createServer();
		try {
			server.start();
		} catch (FtpException e) {
			e.printStackTrace();
		}
	}
	
	// msgThread의 usersOnline 목록을 확인하여 일치하는 ID가 있으면 참값을 반환한다.
	public boolean isOnline(String id) {
		for(ClientThread thr : msgThread.usersOnline) {
			if(thr.getName().equals(id)) return true;
		}
		return false;
	}
}