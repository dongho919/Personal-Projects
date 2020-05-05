package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import io.UserDataUtils;

public class SuspendDialog extends JDialog {
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	JList<String> userList;
	JComboBox<String> comboBox;
	String[] users;
	
	public SuspendDialog(MainFrame main) {
		int[] indices = main.userList.getSelectedIndices();
		users = new String[indices.length];
		int num = 0;
		for(int i : indices) {
			users[num] = main.userList.getModel().getElementAt(i);
			num++;
		}
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			
			userList = new JList<>(users);
			// TODO: ������ ������� ����Ʈ�� ���úҰ����ϰ� ���� ��. userList.set
			scrollPane.setViewportView(userList);
			
		}
		{
			comboBox = new JComboBox<>();
			comboBox.setModel(new DefaultComboBoxModel<>(new String[] {"7��", "14��", "30��", "���� ����"}));
			contentPanel.add(comboBox, BorderLayout.SOUTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Ȯ��");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						UserDataUtils.suspendUser(main, getValue());
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("���");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) { dispose(); }
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setVisible(true);
	}
	
	public SuspendData getValue() {
		SuspendData data = new SuspendData();
		
		// users �迭�� userList�� �ִ� �̸���� ä���
		data.users = new ArrayList<>( Arrays.asList(users) );
		
		// �޺��ڽ� ���û��׿� ���� ������ �ϼ��� �޸��Ѵ� (-1�� ���� ����)
		int days = 0;
		switch(comboBox.getSelectedIndex()) {
		case 0: days = 7;
			break;
		case 1: days = 14;
			break;
		case 2: days = 30;
			break;
		case 3: days = -1;
			break;
		}
		
		// Calendar�� ����� ��¥�� ����ϰ� data.suspendedUntil �ʵ忡 �����Ѵ�.
		Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);

        if(days>0) {
        	c.add(Calendar.DATE, days);
        	data.suspendedUntil = c.getTime();
        } else {
        	data.suspendedUntil = null;
        }

		return data;
	}

}
