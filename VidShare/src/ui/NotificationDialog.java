package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import io.UserDataUtils;

public class NotificationDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 * @param users 
	 */
	
	JTextArea textArea;
	String[] users;
	
	public NotificationDialog(MainFrame main) {
		int[] indices = main.userList.getSelectedIndices();
		users = new String[indices.length];
		for(int i=0; i<users.length; i++) {
			users[i] = main.userList.getModel().getElementAt(indices[i]);
		}
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWeights = new double[]{1.0,1.0};
		gbl_contentPanel.rowWeights = new double[]{1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JList<String> userList = new JList<>(users);
			userList.setBorder(new LineBorder(new Color(0, 0, 0)));
			GridBagConstraints gbc_userList = new GridBagConstraints();
			gbc_userList.fill = GridBagConstraints.BOTH;
			gbc_userList.insets = new Insets(0, 0, 5, 5);
			gbc_userList.gridx = 0;
			gbc_userList.gridy = 0;
			contentPanel.add(userList, gbc_userList);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 0;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				textArea = new JTextArea();
				scrollPane.setViewportView(textArea);
				textArea.setWrapStyleWord(true);
				textArea.setLineWrap(true);
				textArea.setAutoscrolls(false);
				textArea.setRows(5);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("확인");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if(!textArea.getText().trim().isEmpty()) {
							UserDataUtils.sendNotification(main.server.msgThread.usersOnline, getValue());
							dispose();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("취소");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setVisible(true);
	}
	
	private NotificationData getValue() {
		NotificationData data = new NotificationData();
		data.users = new ArrayList<>( Arrays.asList(users) );
		data.msg = textArea.getText().trim();
		data.expiresOn = new Date(0L);
		return data;
	}

}
