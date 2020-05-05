package ui;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import client.ui.ClientMainFrame;
import io.UserDataUtils;
import io.Video;
import io.VideoDataUtils;
import network.Server;

public class MainFrame extends JFrame {

	public JPanel contentPane;
	public JPanel mainPanel;
	public UploadPanel uploadPanel;
	public JComboBox<String> searchCombo;
	public JList<VideoPanel> videoList;
	public JList<String> userList;
	public Server server;
	
	public final static String ADMIN_ID = "ADMIN";
	public static final String ADMIN_PASSWORD = "password";
	public static final String VERSION = "VidShare 0.1.4";
	private JTextField searchField;
	public JTextArea console;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		UserDataUtils.mkdirs();
		VideoDataUtils.mkdirs();
		boolean isTest = false;
		
		if(isTest) {
			ArrayList<Video> videos = new ArrayList<>();
			
			for(int i=0; i<10; i++) {
				Video u = new Video();
				if(i%2 == 0) {
					u.thumbnailFile = "a.jpg";
					u.uploaderId = "dongho" + i/2;
					u.title = "980919";
					u.fileName = "dongh" + i + ".avi";
					u.uploadTime = new Date();
				} else {
					u.thumbnailFile = "c.png";
					u.uploaderId = "sukho" + i/2;
					u.title = "000204";
					u.fileName = "sukh" + i + ".avi";
					u.uploadTime = new Date();
				}
				videos.add(u);
				
			}
			System.out.println(videos);
			
			VideoDataUtils.writeVideoList(videos, VideoDataUtils.VIDEODATA_LOCATION, VideoDataUtils.TMP_VIDEODATA_LOCATION);
			
			UserDataUtils.writeNotifList(new ArrayList<NotificationData>());
		}
		
		
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the frame.
	 */
	
	public MainFrame() {
		MainFrame myself = this;
		server = new Server(myself);
		ui.UIManager.addTrayListener(myself);
		
		setFont(new Font("Arial", Font.PLAIN, 12));
		setTitle(VERSION + " MainFrame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 700);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		mainPanel = new JPanel();
		contentPane.add(mainPanel, "mainPanel");
		
		uploadPanel = new UploadPanel(myself, true);
		contentPane.add(uploadPanel, "uploadPanel");
		GridBagLayout gbl_mainPanel = new GridBagLayout();
		gbl_mainPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_mainPanel.rowWeights = new double[]{1.0, 1.0};
		mainPanel.setLayout(gbl_mainPanel);
		
		JPanel userView = new JPanel();
		userView.setBackground(SystemColor.inactiveCaptionBorder);
		GridBagConstraints gbc_userView = new GridBagConstraints();
		gbc_userView.fill = GridBagConstraints.BOTH;
		gbc_userView.insets = new Insets(0, 0, 5, 5);
		gbc_userView.gridx = 0;
		gbc_userView.gridy = 0;
		mainPanel.add(userView, gbc_userView);
		GridBagLayout gbl_userView = new GridBagLayout();
		gbl_userView.columnWeights = new double[]{1.0, 1.0};
		gbl_userView.rowWeights = new double[]{0.0, 1.0, 0.0};
		userView.setLayout(gbl_userView);
		
		JScrollPane userViewPane = new JScrollPane();
		userViewPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		userList = new JList<>();
		userList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && userList.getSelectedIndex() != -1)
					io.UserDataUtils.openNotificationDialog(myself);
			}
		});
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getClickCount() == 2 && userList.getSelectedIndex() != -1)
					io.UserDataUtils.openNotificationDialog(myself);
			}
		});
		UserDataUtils.updateUserList( myself );
		
		userViewPane.setViewportView(userList);
		GridBagConstraints gbc_userViewPane = new GridBagConstraints();
		gbc_userViewPane.insets = new Insets(5, 5, 5, 0);
		gbc_userViewPane.fill = GridBagConstraints.BOTH;
		gbc_userViewPane.gridwidth = 2;
		gbc_userViewPane.gridx = 0;
		gbc_userViewPane.gridy = 1;
		userView.add(userViewPane, gbc_userViewPane);
		
		JLabel userListLabel = new JLabel("사용자");
		userListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userViewPane.setColumnHeaderView(userListLabel);
		
		JButton suspendUser = new JButton("계정 정지");
		suspendUser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(userList.getSelectedIndex() != -1)
					io.UserDataUtils.openSuspendDialog(myself);
			}
		});
		GridBagConstraints gbc_suspendUser = new GridBagConstraints();
		gbc_suspendUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_suspendUser.insets = new Insets(0, 5, 0, 5);
		gbc_suspendUser.gridx = 0;
		gbc_suspendUser.gridy = 2;
		userView.add(suspendUser, gbc_suspendUser);
		
		JButton sendNotification = new JButton("알림 발송");
		sendNotification.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(userList.getSelectedIndex() != -1)
					UserDataUtils.openNotificationDialog(myself);
			}
		});
		GridBagConstraints gbc_sendNotification = new GridBagConstraints();
		gbc_sendNotification.fill = GridBagConstraints.HORIZONTAL;
		gbc_sendNotification.gridx = 1;
		gbc_sendNotification.gridy = 2;
		userView.add(sendNotification, gbc_sendNotification);
		
		JPanel videoView = new JPanel();
		videoView.setBackground(SystemColor.inactiveCaptionBorder);
		
		GridBagConstraints gbc_videoView = new GridBagConstraints();
		gbc_videoView.insets = new Insets(0, 0, 5, 0);
		gbc_videoView.fill = GridBagConstraints.BOTH;
		gbc_videoView.gridx = 1;
		gbc_videoView.gridy = 0;
		mainPanel.add(videoView, gbc_videoView);
		GridBagLayout gbl_videoView = new GridBagLayout();
		gbl_videoView.columnWeights = new double[]{1.0, 1.0, 1.0};
		gbl_videoView.rowWeights = new double[]{0.0, 1.0, 0.0};
		videoView.setLayout(gbl_videoView);
		
		searchField = new JTextField();
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
					VideoDataUtils.updateVideoList(videoList, VideoDataUtils.searchVideoList(searchField.getText(), searchCombo.getSelectedIndex()));
			}
		});
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.gridwidth = 2;
		gbc_searchField.insets = new Insets(5, 5, 0, 5);
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.gridx = 0;
		gbc_searchField.gridy = 0;
		videoView.add(searchField, gbc_searchField);
		searchField.setColumns(10);
		
		searchCombo = new JComboBox<>();
		searchCombo.setModel(new DefaultComboBoxModel<>(ClientMainFrame.SEARCH_OPTIONS));
		GridBagConstraints gbc_searchCombo = new GridBagConstraints();
		gbc_searchCombo.insets = new Insets(5, 0, 0, 0);
		gbc_searchCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchCombo.gridx = 2;
		gbc_searchCombo.gridy = 0;
		videoView.add(searchCombo, gbc_searchCombo);
		
		JScrollPane videoViewPane = new JScrollPane();
		videoViewPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_videoViewPane = new GridBagConstraints();
		gbc_videoViewPane.fill = GridBagConstraints.BOTH;
		gbc_videoViewPane.insets = new Insets(5, 5, 5, 0);
		gbc_videoViewPane.gridwidth = 3;
		gbc_videoViewPane.gridx = 0;
		gbc_videoViewPane.gridy = 1;
		videoView.add(videoViewPane, gbc_videoViewPane);
		
		videoList = new JList<>();
		videoList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(videoList.getSelectedIndex() != -1) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE)
						VideoDataUtils.deleteLocalVideo(myself, VideoDataUtils.VIDEO_LOCATION);
					else if(e.getKeyCode() == KeyEvent.VK_ENTER)
						VideoDataUtils.playVideo(videoList, VideoDataUtils.VIDEO_LOCATION, true);
				}
			}
		});
		videoList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(videoList.getSelectedIndex() != -1) {
					VideoPanel panel = videoList.getModel().getElementAt( videoList.getLeadSelectionIndex() );
					if(e.getClickCount() == 2 && panel.isEnabled())
						VideoDataUtils.playVideo(videoList, VideoDataUtils.VIDEO_LOCATION, true);
				}
			}
		});
		VideoDataUtils.updateVideoList( videoList, VideoDataUtils.readVideoList(VideoDataUtils.VIDEODATA_LOCATION) );
		videoList.setCellRenderer(new CustomCellRenderer());
		
		videoViewPane.setViewportView(videoList);
		
		JLabel videoListLabel = new JLabel("동영상");
		videoListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		videoViewPane.setColumnHeaderView(videoListLabel);
		JButton addVideo = new JButton("추가");
		addVideo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				((CardLayout)contentPane.getLayout()).show(contentPane, "uploadPanel");
			}
		});
		GridBagConstraints gbc_addVideo = new GridBagConstraints();
		gbc_addVideo.fill = GridBagConstraints.HORIZONTAL;
		gbc_addVideo.insets = new Insets(0, 5, 0, 5);
		gbc_addVideo.gridx = 0;
		gbc_addVideo.gridy = 2;
		videoView.add(addVideo, gbc_addVideo);
		
		JButton deleteVideo = new JButton("삭제");
		deleteVideo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(videoList.getSelectedIndex() != -1)
					VideoDataUtils.deleteLocalVideo(myself, VideoDataUtils.VIDEO_LOCATION);
			}
		});
		GridBagConstraints gbc_deleteVideo = new GridBagConstraints();
		gbc_deleteVideo.fill = GridBagConstraints.HORIZONTAL;
		gbc_deleteVideo.insets = new Insets(0, 0, 0, 5);
		gbc_deleteVideo.gridx = 1;
		gbc_deleteVideo.gridy = 2;
		videoView.add(deleteVideo, gbc_deleteVideo);
		
		JButton playVideo = new JButton("재생");
		playVideo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(videoList.getLeadSelectionIndex() != -1 && videoList.getLeadSelectionIndex() != -1 && videoList.getModel().getElementAt(videoList.getLeadSelectionIndex()).isEnabled())
					VideoDataUtils.playVideo(videoList, VideoDataUtils.VIDEO_LOCATION, true);
			}
		});
		GridBagConstraints gbc_playVideo = new GridBagConstraints();
		gbc_playVideo.fill = GridBagConstraints.HORIZONTAL;
		gbc_playVideo.gridx = 2;
		gbc_playVideo.gridy = 2;
		videoView.add(playVideo, gbc_playVideo);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		mainPanel.add(scrollPane, gbc_scrollPane);
		
		console = new JTextArea();
		scrollPane.setViewportView(console);
		console.setEditable(false);
		
		DefaultCaret caret = (DefaultCaret) console.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}

}
