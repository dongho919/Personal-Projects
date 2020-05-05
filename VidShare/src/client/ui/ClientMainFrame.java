package client.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import client.network.ClientUtils;
import client.network.FileTransfer;
import client.network.MessageThread;
import io.VideoDataUtils;
import network.Server;
import ui.CustomCellRenderer;
import ui.MainFrame;
import ui.UploadPanel;
import ui.VideoPanel;

public class ClientMainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String HOST_ADDRESS = /*"14.37.50.108"*/"10.0.0.206"/*"73.118.204.154"/*"127.0.0.1"*/;
	public final static String[] SEARCH_OPTIONS = new String[] {"제목", "업로더", "태그"};
	
	public JPanel contentPane;
	public JTextField idField_login;
	public JPasswordField passwordField_login;
	public JTextField idField_signup;
	public JPasswordField passwordField_signup;
	public JPasswordField passwordVerifField_signup;
	public MessageThread msgThread;
	public JComboBox<String> searchCombo;
	public boolean idChecked;
	public JLabel downloadLabel;

	public String id;
	public String password;
	
	public JList<VideoPanel> videoList;
	public JList<VideoPanel> localVideoList;
	public FileTransfer fileTransfer;
	
	public ClientMainFrame myself;
	public JTextField uploadFileName;
	private Component uploadPanel;
	private JTextField searchField;
	/**
	 * @wbp.nonvisual location=71,359
	 */
	

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
		
		ClientUtils.vlcSetup();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMainFrame frame = new ClientMainFrame();
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
	public ClientMainFrame() {
		myself = this;
		setTitle(MainFrame.VERSION);
		Socket msgSocket = null;
		fileTransfer = new FileTransfer(myself);
		ui.UIManager.addTrayListener(myself);
		
		try {
			msgSocket = new Socket(HOST_ADDRESS, Server.MESSAGE_PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		msgThread = new MessageThread(this, msgSocket);
		msgThread.setPriority(Thread.MIN_PRIORITY);
		msgThread.start();
		
		idChecked = false;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 520, 300);
		setMinimumSize(new Dimension(400, 200));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		JPanel loginPanel = new JPanel();
		loginPanel.setBackground(SystemColor.inactiveCaptionBorder);
		contentPane.add(loginPanel, "loginPanel");
		
		JLabel titleLabel_login = new JLabel(MainFrame.VERSION);
		titleLabel_login.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel idLabel_login = new JLabel("아이디");
		
		idField_login = new JTextField();
		idField_login.setColumns(10);
		
		JLabel passwordLabel_login = new JLabel("비밀번호");
		
		passwordField_login = new JPasswordField();
		passwordField_login.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					String id = idField_login.getText().trim();
					char[] password = passwordField_login.getPassword();
					if(!idField_login.getText().trim().isEmpty() && passwordField_login.getPassword().length>0)
						ClientUtils.sendLoginRequest(msgThread, id, password);
					Arrays.fill(password, '\u0000');
				}
			}
		});
		
		JButton loginButton_login = new JButton("로그인");
		loginButton_login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				String id = idField_login.getText().trim();
				char[] password = passwordField_login.getPassword();
				if(!idField_login.getText().trim().isEmpty() && passwordField_login.getPassword().length>0)
					ClientUtils.sendLoginRequest(msgThread, id, password);
				Arrays.fill(password, '\u0000');
			}
		});
		
		JButton signupButton_login = new JButton("회원가입");
		signupButton_login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				idField_signup.setText("");
				passwordField_signup.setText("");
				passwordVerifField_signup.setText("");
				((CardLayout)contentPane.getLayout()).show(contentPane, "signupPanel");
			}
		});
		GroupLayout gl_loginPanel = new GroupLayout(loginPanel);
		gl_loginPanel.setHorizontalGroup(
			gl_loginPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_loginPanel.createSequentialGroup()
					.addGap(90)
					.addGroup(gl_loginPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_loginPanel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(passwordLabel_login, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(passwordField_login, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(gl_loginPanel.createSequentialGroup()
							.addGap(12)
							.addComponent(idLabel_login, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_loginPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(titleLabel_login, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
								.addComponent(idField_login, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)))
						.addGroup(gl_loginPanel.createSequentialGroup()
							.addGap(60)
							.addGroup(gl_loginPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(signupButton_login, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
								.addComponent(loginButton_login, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))))
					.addGap(158))
		);
		gl_loginPanel.setVerticalGroup(
			gl_loginPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_loginPanel.createSequentialGroup()
					.addGap(56)
					.addComponent(titleLabel_login)
					.addGap(18)
					.addGroup(gl_loginPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(idField_login, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(idLabel_login))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_loginPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(passwordField_login, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(passwordLabel_login))
					.addGap(24)
					.addComponent(loginButton_login)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(signupButton_login)
					.addContainerGap(44, Short.MAX_VALUE))
		);
		loginPanel.setLayout(gl_loginPanel);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(SystemColor.inactiveCaptionBorder);
		contentPane.add(mainPanel, "mainPanel");
		mainPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel videoListPanel = new JPanel();
		videoListPanel.setBackground(SystemColor.inactiveCaptionBorder);
		mainPanel.add(videoListPanel);
		GridBagLayout gbl_videoListPanel = new GridBagLayout();
		gbl_videoListPanel.columnWeights = new double[]{1.0, 1.0, 1.0 };
		gbl_videoListPanel.rowWeights = new double[]{0.0, 1.0, 0.0};
		videoListPanel.setLayout(gbl_videoListPanel);
		
		searchField = new JTextField();
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
					ClientUtils.sendSearchVideoRequest(myself, searchField.getText(), searchCombo.getSelectedIndex());
			}
		});
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.gridwidth = 2;
		gbc_searchField.insets = new Insets(5, 5, 0, 5);
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.gridx = 0;
		gbc_searchField.gridy = 0;
		videoListPanel.add(searchField, gbc_searchField);
		searchField.setColumns(10);
		
		searchCombo = new JComboBox<>();
		searchCombo.setModel(new DefaultComboBoxModel<String>(SEARCH_OPTIONS));
		GridBagConstraints gbc_searchCombo = new GridBagConstraints();
		gbc_searchCombo.insets = new Insets(5, 0, 0, 0);
		gbc_searchCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchCombo.gridx = 2;
		gbc_searchCombo.gridy = 0;
		videoListPanel.add(searchCombo, gbc_searchCombo);
		
		JScrollPane videoListPane = new JScrollPane();
		videoListPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_videoListPane = new GridBagConstraints();
		gbc_videoListPane.insets = new Insets(5, 5, 5, 0);
		gbc_videoListPane.fill = GridBagConstraints.BOTH;
		gbc_videoListPane.gridwidth = 3;
		gbc_videoListPane.gridx = 0;
		gbc_videoListPane.gridy = 1;
		videoListPanel.add(videoListPane, gbc_videoListPane);
		
		videoList = new JList<>();
		videoList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if( videoList.getSelectedIndex() != -1) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE)
						ClientUtils.sendDeleteVideoRequest(myself);
					else if(e.getKeyCode() == KeyEvent.VK_ENTER)
						fileTransfer.downloadVideo(myself);
				}
			}
		});
		videoList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getClickCount() == 2 && videoList.getSelectedIndex() != -1)
					fileTransfer.downloadVideo(myself);
			}
		});
		videoList.setCellRenderer(new CustomCellRenderer());
		videoListPane.setViewportView(videoList);
		
		JLabel videoListLabel = new JLabel("온라인 저장소");
		videoListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		videoListPane.setColumnHeaderView(videoListLabel);
		
		JButton downloadButton = new JButton("다운로드");
		downloadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(videoList.getSelectedIndex() != -1)
					fileTransfer.downloadVideo(myself);
			}
		});
		GridBagConstraints gbc_downloadButton = new GridBagConstraints();
		gbc_downloadButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadButton.insets = new Insets(0, 5, 10, 5);
		gbc_downloadButton.gridx = 0;
		gbc_downloadButton.gridy = 2;
		videoListPanel.add(downloadButton, gbc_downloadButton);
		
		JButton uploadButton = new JButton("업로드");
		uploadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				((CardLayout)contentPane.getLayout()).show(contentPane, "uploadPanel");
			}
		});
		GridBagConstraints gbc_uploadButton = new GridBagConstraints();
		gbc_uploadButton.insets = new Insets(0, 0, 10, 5);
		gbc_uploadButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_uploadButton.gridx = 1;
		gbc_uploadButton.gridy = 2;
		videoListPanel.add(uploadButton, gbc_uploadButton);
		
		JButton deleteButton = new JButton("삭제");
		deleteButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(videoList.getSelectedIndex() != -1)
					ClientUtils.sendDeleteVideoRequest(myself);
			}
		});
		GridBagConstraints gbc_deleteButton = new GridBagConstraints();
		gbc_deleteButton.insets = new Insets(0, 0, 10, 0);
		gbc_deleteButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_deleteButton.gridx = 2;
		gbc_deleteButton.gridy = 2;
		videoListPanel.add(deleteButton, gbc_deleteButton);
		
		JPanel localVideoListPanel = new JPanel();
		localVideoListPanel.setBackground(SystemColor.inactiveCaptionBorder);
		mainPanel.add(localVideoListPanel);
		GridBagLayout gbl_localVideoListPanel = new GridBagLayout();
		gbl_localVideoListPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_localVideoListPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		localVideoListPanel.setLayout(gbl_localVideoListPanel);
		
		JScrollPane localVideoListPane = new JScrollPane();
		localVideoListPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_localVideoListPane = new GridBagConstraints();
		gbc_localVideoListPane.insets = new Insets(5, 5, 5, 0);
		gbc_localVideoListPane.fill = GridBagConstraints.BOTH;
		gbc_localVideoListPane.gridwidth = 2;
		gbc_localVideoListPane.gridx = 0;
		gbc_localVideoListPane.gridy = 1;
		localVideoListPanel.add(localVideoListPane, gbc_localVideoListPane);
		
		localVideoList = new JList<>();
		localVideoList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(localVideoList.getSelectedIndex() != -1) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE)
						VideoDataUtils.deleteLocalVideo(myself, VideoDataUtils.LOCAL_VIDEO_LOCATION);
					else if(e.getKeyCode() == KeyEvent.VK_ENTER)
						VideoDataUtils.playVideo(localVideoList, VideoDataUtils.LOCAL_VIDEO_LOCATION, false);
				}
			}
		});
		localVideoList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getClickCount() == 2 && localVideoList.getLeadSelectionIndex() != -1 && localVideoList.getModel().getElementAt( localVideoList.getLeadSelectionIndex()).isEnabled())
					VideoDataUtils.playVideo(localVideoList, VideoDataUtils.LOCAL_VIDEO_LOCATION, false);
			}
		});
		localVideoList.setCellRenderer(new CustomCellRenderer());
		VideoDataUtils.updateVideoList( localVideoList, VideoDataUtils.readVideoList(VideoDataUtils.LOCAL_VIDEODATA_LOCATION) );
		localVideoListPane.setViewportView(localVideoList);
		
		JLabel localVideoListLabel = new JLabel("보관함");
		localVideoListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		localVideoListPane.setColumnHeaderView(localVideoListLabel);
		
		JButton playButton = new JButton("재생");
		playButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				VideoPanel panel = localVideoList.getModel().getElementAt( localVideoList.getLeadSelectionIndex() );
				if(localVideoList.getLeadSelectionIndex() != -1 && panel.isEnabled())
					VideoDataUtils.playVideo(localVideoList, VideoDataUtils.LOCAL_VIDEO_LOCATION, false);
			}
		});
		GridBagConstraints gbc_playButton = new GridBagConstraints();
		gbc_playButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_playButton.insets = new Insets(0, 5, 5, 5);
		gbc_playButton.gridx = 0;
		gbc_playButton.gridy = 2;
		localVideoListPanel.add(playButton, gbc_playButton);
		
		JButton deleteLocalButton = new JButton("삭제");
		deleteLocalButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(localVideoList.getSelectedIndex() != -1 && localVideoList.getModel().getElementAt(localVideoList.getSelectedIndex()).isEnabled())
					VideoDataUtils.deleteLocalVideo(myself, VideoDataUtils.LOCAL_VIDEO_LOCATION);
			}
		});
		GridBagConstraints gbc_deleteLocalButton = new GridBagConstraints();
		gbc_deleteLocalButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_deleteLocalButton.insets = new Insets(0, 0, 5, 5);
		gbc_deleteLocalButton.gridx = 1;
		gbc_deleteLocalButton.gridy = 2;
		localVideoListPanel.add(deleteLocalButton, gbc_deleteLocalButton);
		
		downloadLabel = new JLabel("");
		GridBagConstraints gbc_downloadLabel = new GridBagConstraints();
		gbc_downloadLabel.insets = new Insets(5, 0, 0, 0);
		gbc_downloadLabel.gridwidth = 2;
		gbc_downloadLabel.gridx = 0;
		gbc_downloadLabel.gridy = 3;
		localVideoListPanel.add(downloadLabel, gbc_downloadLabel);
		
		JPanel signupPanel = new JPanel();
		signupPanel.setBackground(SystemColor.inactiveCaptionBorder);
		contentPane.add(signupPanel, "signupPanel");
		SpringLayout sl_signupPanel = new SpringLayout();
		signupPanel.setLayout(sl_signupPanel);
		
		JLabel signupLabel_signup = new JLabel("회원가입");
		sl_signupPanel.putConstraint(SpringLayout.WEST, signupLabel_signup, 190, SpringLayout.WEST, signupPanel);
		signupPanel.add(signupLabel_signup);
		
		idField_signup = new JTextField();
		idField_signup.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				idChecked = false;
				idField_signup.setBackground(Color.WHITE);
			}
		});
		sl_signupPanel.putConstraint(SpringLayout.WEST, idField_signup, 155, SpringLayout.WEST, signupPanel);
		sl_signupPanel.putConstraint(SpringLayout.SOUTH, signupLabel_signup, -24, SpringLayout.NORTH, idField_signup);
		sl_signupPanel.putConstraint(SpringLayout.NORTH, idField_signup, 75, SpringLayout.NORTH, signupPanel);
		signupPanel.add(idField_signup);
		idField_signup.setColumns(10);
		
		passwordField_signup = new JPasswordField();
		sl_signupPanel.putConstraint(SpringLayout.NORTH, passwordField_signup, 6, SpringLayout.SOUTH, idField_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, passwordField_signup, 155, SpringLayout.WEST, signupPanel);
		sl_signupPanel.putConstraint(SpringLayout.EAST, passwordField_signup, -127, SpringLayout.EAST, signupPanel);
		signupPanel.add(passwordField_signup);
		
		passwordVerifField_signup = new JPasswordField();
		sl_signupPanel.putConstraint(SpringLayout.NORTH, passwordVerifField_signup, 6, SpringLayout.SOUTH, passwordField_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, passwordVerifField_signup, 155, SpringLayout.WEST, signupPanel);
		sl_signupPanel.putConstraint(SpringLayout.EAST, passwordVerifField_signup, -127, SpringLayout.EAST, signupPanel);
		signupPanel.add(passwordVerifField_signup);
		
		JLabel idLabel_signup = new JLabel("아이디");
		sl_signupPanel.putConstraint(SpringLayout.NORTH, idLabel_signup, 78, SpringLayout.NORTH, signupPanel);
		sl_signupPanel.putConstraint(SpringLayout.WEST, idLabel_signup, 113, SpringLayout.WEST, signupPanel);
		signupPanel.add(idLabel_signup);
		
		JLabel passwordLabel_signup = new JLabel("비밀번호");
		sl_signupPanel.putConstraint(SpringLayout.NORTH, passwordLabel_signup, 12, SpringLayout.SOUTH, idLabel_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, passwordLabel_signup, 99, SpringLayout.WEST, signupPanel);
		signupPanel.add(passwordLabel_signup);
		
		JLabel passwordVerifLabel_signup = new JLabel("비밀번호 확인");
		sl_signupPanel.putConstraint(SpringLayout.NORTH, passwordVerifLabel_signup, 12, SpringLayout.SOUTH, passwordLabel_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, passwordVerifLabel_signup, 72, SpringLayout.WEST, signupPanel);
		signupPanel.add(passwordVerifLabel_signup);
		
		JButton idCheckButton_signup = new JButton("중복확인");
		sl_signupPanel.putConstraint(SpringLayout.EAST, idCheckButton_signup, -40, SpringLayout.EAST, signupPanel);
		sl_signupPanel.putConstraint(SpringLayout.EAST, idField_signup, -6, SpringLayout.WEST, idCheckButton_signup);
		idCheckButton_signup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				String id = idField_signup.getText();
				ClientUtils.sendIdCheck(msgThread, id);
			}
		});
		sl_signupPanel.putConstraint(SpringLayout.NORTH, idCheckButton_signup, 74, SpringLayout.NORTH, signupPanel);
		signupPanel.add(idCheckButton_signup);
		
		JButton signupButton_signup = new JButton("가입");
		sl_signupPanel.putConstraint(SpringLayout.NORTH, signupButton_signup, 26, SpringLayout.SOUTH, passwordVerifField_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, signupButton_signup, 0, SpringLayout.WEST, idField_signup);
		sl_signupPanel.putConstraint(SpringLayout.EAST, signupButton_signup, 0, SpringLayout.EAST, idField_signup);
		signupButton_signup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String id = idField_signup.getText().trim();
				char[] password = passwordField_signup.getPassword();
				// 중복확인, 비밀번호 길이 확인, 비밀번호 무결성 확인
				if(idChecked &&
				   !idField_signup.getText().trim().isEmpty() &&
				   passwordField_signup.getPassword().length>=4 &&
				   Arrays.equals( passwordField_signup.getPassword(), passwordVerifField_signup.getPassword() ) ) {
					ClientUtils.sendSignupRequest(msgThread, id, password);
					idField_signup.setText("");
					passwordField_signup.setText("");
					passwordVerifField_signup.setText("");
					((CardLayout)contentPane.getLayout()).show(contentPane, "loginPanel");
				} else {
					String warning = null;
					if(!idChecked) {
						warning = "중복확인을 해주세요.";
					} else if(idField_signup.getText().trim().isEmpty()) {
						warning = "아이디를 입력해 주세요.";
					} else if(passwordField_signup.getPassword().length<4) {
						warning = "비밀번호는 4글자 이상이어야 합니다.";
						passwordField_signup.setText("");
						passwordVerifField_signup.setText("");
					} else if( !Arrays.equals( passwordField_signup.getPassword(), passwordVerifField_signup.getPassword() )) {
						warning = "비밀번호 확인 값이 일치하지 않습니다.";
						passwordField_signup.setText("");
						passwordVerifField_signup.setText("");
					}
					JDialog notifDialog = new JDialog();
					notifDialog.setBounds(200, 200, 250, 200);
					notifDialog.setResizable(false);
					notifDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					notifDialog.getContentPane().setLayout(new BorderLayout());
					JLabel notifLabel = new JLabel(warning, SwingConstants.CENTER);
					notifLabel.setMaximumSize(new Dimension(250, 200));
					notifLabel.setPreferredSize(new Dimension(250, 150));
					notifLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
					notifDialog.getContentPane().add(notifLabel, BorderLayout.CENTER);
					JButton okButton = new JButton("확인");
					okButton.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent arg0) {
							notifDialog.dispose();
						}
					});
					notifDialog.getContentPane().add(okButton, BorderLayout.SOUTH);
					notifDialog.setVisible(true);
				}
			}
		});
		signupPanel.add(signupButton_signup);
		
		JButton returnButton_signup = new JButton("뒤로");
		returnButton_signup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				idField_login.setText("");
				passwordField_login.setText("");
				((CardLayout)contentPane.getLayout()).show(contentPane, "loginPanel");
			}
		});
		sl_signupPanel.putConstraint(SpringLayout.NORTH, returnButton_signup, 6, SpringLayout.SOUTH, signupButton_signup);
		sl_signupPanel.putConstraint(SpringLayout.WEST, returnButton_signup, 0, SpringLayout.WEST, idField_signup);
		sl_signupPanel.putConstraint(SpringLayout.EAST, returnButton_signup, 0, SpringLayout.EAST, idField_signup);
		signupPanel.add(returnButton_signup);
		
		uploadPanel = new UploadPanel(myself, false);
		contentPane.add(uploadPanel, "uploadPanel");
		
	}
}
