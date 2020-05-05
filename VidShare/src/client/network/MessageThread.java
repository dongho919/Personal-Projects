package client.network;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import client.ui.ClientMainFrame;
import io.IoUtil;
import io.Video;
import io.VideoDataUtils;
import network.MessagePacket;

public class MessageThread extends Thread {
	ClientMainFrame main;
	Socket sock;
	
	public ObjectInputStream incomingMsg;
	public ObjectOutputStream outgoingMsg;
	private boolean isRunning;
	
	public MessageThread(ClientMainFrame main, Socket sock) {
		this.main = main;
		this.sock = sock;
		isRunning = true;
		
		try {
			incomingMsg = new ObjectInputStream(sock.getInputStream());
			outgoingMsg = new ObjectOutputStream(sock.getOutputStream());
			System.out.println("MsgThread ObjectStreams Created: " + incomingMsg + ", " + outgoingMsg + " on port " + sock.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public void run() {
		while(isRunning && !sock.isClosed()) {
			System.out.println(".");
			// TODO: 메시지를 수신하면 notif, suspend, loginsuccess, loginfail, idchecksuccess, idcheckfail, deleteusersuccess, deleteuserfail,
			// deletevideosuccess, deletevideofail, userlistupdate, videolistupdate의 경우를 나눠서 생각한다.
			
			MessagePacket mp = IoUtil.receiveMessagePacket(incomingMsg);
			System.out.println(mp);
			
			// 로그인, 영상 삭제, 회원탈퇴 실패, 일반 알림, 정지 알림의 경우에는 그대로 표시한다.
			
			if((mp.msgType.contains("fail") && !mp.msgType.equals("idcheckfail")) ||
			   mp.msgType.equals("notif") ||
			   mp.msgType.equals("suspend")) {
				System.out.println("알림이 도착했습니다.");
				
				{
					JDialog notifDialog = new JDialog();
					notifDialog.setBounds(200, 200, 250, 200);
					notifDialog.setResizable(false);
					notifDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					notifDialog.setLayout(new BorderLayout());
					JLabel notifLabel = new JLabel(mp.content, SwingConstants.CENTER);
					notifLabel.setMaximumSize(new Dimension(250, 200));
					notifLabel.setPreferredSize(new Dimension(250, 150));
					notifLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
					notifDialog.add(notifLabel, BorderLayout.CENTER);
					JButton okButton = new JButton("확인");
					okButton.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent arg0) {
							notifDialog.dispose();
							if(mp.msgType.equals("suspend"))
								((CardLayout)main.contentPane.getLayout()).show(main.contentPane, "loginPanel");
						}
					});
					notifDialog.add(okButton, BorderLayout.SOUTH);
					notifDialog.setVisible(true);
				}
				
			// 로그인에 성공했을 경우 id를 설정하고 mainPanel로 이동한다
			} else if(mp.msgType.equals("loginsuccess")) {
				main.id = main.idField_login.getText();
				main.password = new String(main.passwordField_login.getPassword());
				System.out.println("다음 아이디로 로그인했습니다: " + main.id);
				main.idField_login.setText("");
				main.passwordField_login.setText("");
				
				((CardLayout)main.contentPane.getLayout()).show(main.contentPane, "mainPanel");
				
			// 아이디가 중복되지 않을 경우 아이디 입력창 배경을 시안으로 바꾸고 idChecked를 true로 바꾼다.
			} else if(mp.msgType.equals("idchecksuccess")) {
				main.idField_signup.setBackground(Color.GREEN);
				main.idChecked = true;
				
			// 아이디가 중복될 경우 아이디 입력창 초기화 후 배경을 마젠타로 바꾸고 idChecked를 false로 바꾼다.
			} else if(mp.msgType.equals("idcheckfail")) {
				//main.idField_signup.setText("");
				main.idField_signup.setBackground(Color.PINK);
				main.idChecked = false;
				
			// 회원탈퇴에 성공했을 경우 로그인 화면으로 이동한다.
			} else if(mp.msgType.equals("deleteusersuccess")) {
				main.id = "";
				main.idField_login.setText("");
				main.passwordField_login.setText("");
				((CardLayout)main.contentPane.getLayout()).show(main.contentPane, "loginPanel");
				
			// TODO: 유저 목록 구현
			} else if(mp.msgType.equals("userlistupdate")) {
			
			// videoList 업데이트 정보가 도착하면 그에 따라 업데이트한다.
			} else if(mp.msgType.equals("videolistupdate")) {
				ArrayList<Video> videos = (ArrayList<Video>)mp.attachment;
				VideoDataUtils.updateVideoList(main.videoList, videos);
			}
		}
	}
	
	public void close() {
		isRunning = false;
	}
}
