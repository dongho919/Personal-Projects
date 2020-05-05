package ui;

import javax.swing.JPanel;
import java.awt.SystemColor;
import javax.swing.SpringLayout;

import org.apache.ftpserver.ftplet.FtpException;

import client.ui.ClientMainFrame;
import io.IoUtil;
import io.Video;
import io.VideoDataUtils;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.CardLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class UploadPanel extends JPanel {
	private JTextField titleField;
	private JTextField tagsField;
	private JTextField fileField;
	
	JFrame main;
	File videoFile;

	/**
	 * Create the panel.
	 */
	public UploadPanel(JFrame main, boolean isServer) {
		this.main = main;
		videoFile = null;
		
		setBackground(SystemColor.inactiveCaptionBorder);
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel titleLabel = new JLabel("제목");
		springLayout.putConstraint(SpringLayout.NORTH, titleLabel, 69, SpringLayout.NORTH, this);
		add(titleLabel);
		
		JLabel tagsLabel = new JLabel("태그(쉼표로 구분)");
		springLayout.putConstraint(SpringLayout.NORTH, tagsLabel, 21, SpringLayout.SOUTH, titleLabel);
		springLayout.putConstraint(SpringLayout.EAST, tagsLabel, 0, SpringLayout.EAST, titleLabel);
		add(tagsLabel);
		
		titleField = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, titleLabel, -6, SpringLayout.WEST, titleField);
		springLayout.putConstraint(SpringLayout.NORTH, titleField, 66, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, titleField, 137, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, titleField, -180, SpringLayout.EAST, this);
		add(titleField);
		titleField.setColumns(10);
		
		tagsField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, tagsField, 0, SpringLayout.WEST, titleField);
		springLayout.putConstraint(SpringLayout.NORTH, tagsField, 15, SpringLayout.SOUTH, titleField);
		springLayout.putConstraint(SpringLayout.EAST, tagsField, 0, SpringLayout.EAST, titleField);
		add(tagsField);
		tagsField.setColumns(10);
		
		JLabel fileLabel = new JLabel("파일");
		springLayout.putConstraint(SpringLayout.NORTH, fileLabel, 33, SpringLayout.SOUTH, tagsLabel);
		springLayout.putConstraint(SpringLayout.EAST, fileLabel, 0, SpringLayout.EAST, titleLabel);
		add(fileLabel);
		
		fileField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, fileField, 27, SpringLayout.SOUTH, tagsField);
		fileField.setEditable(false);
		springLayout.putConstraint(SpringLayout.WEST, fileField, 0, SpringLayout.WEST, titleField);
		springLayout.putConstraint(SpringLayout.EAST, fileField, 0, SpringLayout.EAST, titleField);
		add(fileField);
		fileField.setColumns(10);
		
		JButton fileButton = new JButton("파일 찾기...");
		fileButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				videoFile = VideoDataUtils.openAddVideoDialog();
				fileField.setText(videoFile==null? "":videoFile.getName());
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, fileButton, 0, SpringLayout.NORTH, fileField);
		springLayout.putConstraint(SpringLayout.WEST, fileButton, 14, SpringLayout.EAST, fileField);
		add(fileButton);
		
		JButton okButton = new JButton("업로드");
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				
				if(!titleField.getText().trim().isEmpty() && videoFile != null) {
					Video video = new Video();
					video.title = titleField.getText();
					video.fileSize = videoFile.length();
					video.tags = tagsField.getText().split(",");
					video.uploadTime = new Date();
					
					if(isServer) {
						JPanel contentPane = ((MainFrame) main).contentPane;
						((CardLayout)contentPane.getLayout()).show(contentPane, "mainPanel");
						
						video.uploaderId = MainFrame.ADMIN_ID;
						video.fileName = videoFile.getName();
						
						try {
							File destination = new File(VideoDataUtils.VIDEO_LOCATION + video.fileName);
							String originalPath = destination.getAbsolutePath();
							int num=1;
							while(destination.exists()) {
								destination = new File( originalPath.substring(0, originalPath.lastIndexOf('.')) +
														" (" + num + ")" +
														originalPath.substring(originalPath.lastIndexOf('.')) );
								video.fileName = destination.getName();
								num++;
							}
							
							BufferedInputStream in = new BufferedInputStream(new FileInputStream(videoFile));
							BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destination));
							byte[] buf = new byte[IoUtil.COPY_BUFFER_SIZE];
							int length;
							while((length=in.read(buf)) > 0)
								out.write(buf, 0, length);
							
							in.close();
							out.close();
							
							VideoDataUtils.addVideo((MainFrame)main, video, ((MainFrame)main).server.serverFactory.getUserManager().getUserByName(MainFrame.ADMIN_ID));
						} catch (IOException | FtpException ex) {
							ex.printStackTrace();
						}
						
					} else {
						JPanel contentPane = ((ClientMainFrame) main).contentPane;
						((CardLayout)contentPane.getLayout()).show(contentPane, "mainPanel");
						video.uploaderId = ((ClientMainFrame) main).id;
						video.fileName = videoFile.getPath();
						((ClientMainFrame) main).fileTransfer.uploadVideo(video);
						
					}
					titleField.setText("");
					tagsField.setText("");
					fileField.setText("");
					videoFile = null;
				}
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, okButton, 130, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, okButton, -33, SpringLayout.SOUTH, this);
		add(okButton);
		
		JButton cancelButton = new JButton("취소");
		springLayout.putConstraint(SpringLayout.EAST, cancelButton, -180, SpringLayout.EAST, this);
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(isServer) {
					JPanel contentPane = ((MainFrame) main).contentPane;
					((CardLayout)contentPane.getLayout()).show(contentPane, "mainPanel");
				} else {
					JPanel contentPane = ((ClientMainFrame) main).contentPane;
					((CardLayout)contentPane.getLayout()).show(contentPane, "mainPanel");
				}
				
				titleField.setText("");
				tagsField.setText("");
				fileField.setText("");
				videoFile = null;
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, cancelButton, 0, SpringLayout.NORTH, okButton);
		add(cancelButton);

	}
}
