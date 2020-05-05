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
			// TODO: �޽����� �����ϸ� notif, suspend, loginsuccess, loginfail, idchecksuccess, idcheckfail, deleteusersuccess, deleteuserfail,
			// deletevideosuccess, deletevideofail, userlistupdate, videolistupdate�� ��츦 ������ �����Ѵ�.
			
			MessagePacket mp = IoUtil.receiveMessagePacket(incomingMsg);
			System.out.println(mp);
			
			// �α���, ���� ����, ȸ��Ż�� ����, �Ϲ� �˸�, ���� �˸��� ��쿡�� �״�� ǥ���Ѵ�.
			
			if((mp.msgType.contains("fail") && !mp.msgType.equals("idcheckfail")) ||
			   mp.msgType.equals("notif") ||
			   mp.msgType.equals("suspend")) {
				System.out.println("�˸��� �����߽��ϴ�.");
				
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
					JButton okButton = new JButton("Ȯ��");
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
				
			// �α��ο� �������� ��� id�� �����ϰ� mainPanel�� �̵��Ѵ�
			} else if(mp.msgType.equals("loginsuccess")) {
				main.id = main.idField_login.getText();
				main.password = new String(main.passwordField_login.getPassword());
				System.out.println("���� ���̵�� �α����߽��ϴ�: " + main.id);
				main.idField_login.setText("");
				main.passwordField_login.setText("");
				
				((CardLayout)main.contentPane.getLayout()).show(main.contentPane, "mainPanel");
				
			// ���̵� �ߺ����� ���� ��� ���̵� �Է�â ����� �þ����� �ٲٰ� idChecked�� true�� �ٲ۴�.
			} else if(mp.msgType.equals("idchecksuccess")) {
				main.idField_signup.setBackground(Color.GREEN);
				main.idChecked = true;
				
			// ���̵� �ߺ��� ��� ���̵� �Է�â �ʱ�ȭ �� ����� ����Ÿ�� �ٲٰ� idChecked�� false�� �ٲ۴�.
			} else if(mp.msgType.equals("idcheckfail")) {
				//main.idField_signup.setText("");
				main.idField_signup.setBackground(Color.PINK);
				main.idChecked = false;
				
			// ȸ��Ż�� �������� ��� �α��� ȭ������ �̵��Ѵ�.
			} else if(mp.msgType.equals("deleteusersuccess")) {
				main.id = "";
				main.idField_login.setText("");
				main.passwordField_login.setText("");
				((CardLayout)main.contentPane.getLayout()).show(main.contentPane, "loginPanel");
				
			// TODO: ���� ��� ����
			} else if(mp.msgType.equals("userlistupdate")) {
			
			// videoList ������Ʈ ������ �����ϸ� �׿� ���� ������Ʈ�Ѵ�.
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
