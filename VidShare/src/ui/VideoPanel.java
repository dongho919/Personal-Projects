package ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import io.Video;
import io.VideoDataUtils;

public class VideoPanel extends JPanel {
	
	public final int THUMBNAIL_WIDTH = 80;
	public final int THUMBNAIL_HEIGHT = 80;
	
	/**
	 * Create the panel.
	 */
	
	public Video video;
	public JProgressBar downloadBar;
	
	public VideoPanel(Video video) {
		setBackground(Color.WHITE);
		this.video = video;
		ImageIcon thumbnail = new ImageIcon(VideoDataUtils.THUMBNAIL_LOCATION + video.thumbnailFile );
		thumbnail = new ImageIcon( thumbnail.getImage().getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_DEFAULT) );
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0};
		setLayout(gridBagLayout);
		
		JLabel titleLabel = new JLabel(String.format("%s (%.2f %sB)", video.title, video.fileSize/1024.0/1024.0/(video.fileSize>1024*1024*1024?1024.0:1.0), video.fileSize>1024*1024*1024?"G":"M"));
		GridBagConstraints gbc_titleLabel = new GridBagConstraints();
		gbc_titleLabel.fill = GridBagConstraints.BOTH;
		gbc_titleLabel.insets = new Insets(0, 0, 5, 0);
		gbc_titleLabel.gridx = 1;
		gbc_titleLabel.gridy = 0;
		add(titleLabel, gbc_titleLabel);
		
		JLabel thumbnailLabel = new JLabel(thumbnail);
		GridBagConstraints gbc_thumbnailLabel = new GridBagConstraints();
		gbc_thumbnailLabel.gridheight = 3;
		gbc_thumbnailLabel.insets = new Insets(0, 0, 5, 5);
		gbc_thumbnailLabel.gridx = 0;
		gbc_thumbnailLabel.gridy = 0;
		add(thumbnailLabel, gbc_thumbnailLabel);
		
		JLabel uploaderLabel = new JLabel(video.uploaderId);
		GridBagConstraints gbc_uploaderLabel = new GridBagConstraints();
		gbc_uploaderLabel.fill = GridBagConstraints.BOTH;
		gbc_uploaderLabel.insets = new Insets(0, 0, 5, 0);
		gbc_uploaderLabel.gridx = 1;
		gbc_uploaderLabel.gridy = 1;
		add(uploaderLabel, gbc_uploaderLabel);
		
		JLabel tagsLabel = new JLabel(Arrays.toString(video.tags));
		GridBagConstraints gbc_tagsLabel = new GridBagConstraints();
		gbc_tagsLabel.fill = GridBagConstraints.BOTH;
		gbc_tagsLabel.gridx = 1;
		gbc_tagsLabel.gridy = 2;
		add(tagsLabel, gbc_tagsLabel);
		
		downloadBar = new JProgressBar();
		GridBagConstraints gbc_downloadBar = new GridBagConstraints();
		gbc_downloadBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadBar.insets = new Insets(0, 5, 0, 5);
		gbc_downloadBar.gridx = 1;
		gbc_downloadBar.gridy = 3;
		downloadBar.setVisible(false);
		add(downloadBar, gbc_downloadBar);
	}

}
