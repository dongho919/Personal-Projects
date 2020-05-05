package ui.video;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import uk.co.caprica.vlcj.media.MediaParsedStatus;
import uk.co.caprica.vlcj.player.base.AudioApi;
import uk.co.caprica.vlcj.player.base.ControlsApi;
import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Controls extends JPanel {

	private MouseEvent mouseEvent;

	/**
	 * Create the panel.
	 */
	
	JButton pauseButton;
	JSlider videoTimeSlider;
	JLabel videoTimeLabel;
	JSlider videoVolumeSlider;
	
	ControlsApi controls;
	AudioApi audio;
	
	static final long SKIP_TIME = 10000;
	static final int VOLUME_TICK = 5;
	static final int VOLUME_MAX_SIZE = 10;
	
	public Controls(EmbeddedMediaPlayer mediaPlayer) {
		MediaApi media = mediaPlayer.media();
		controls = mediaPlayer.controls();
		audio = mediaPlayer.audio();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		
		pauseButton = new JButton("\u23f8");
		pauseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if( pauseButton.getText().equals("\u23f8") ) {
					pauseButton.setText("▶");
					controls.pause();
				} else {
					pauseButton.setText("\u23f8");
					controls.play();
				}
			}
		});
		GridBagConstraints gbc_pauseButton = new GridBagConstraints();
		gbc_pauseButton.insets = new Insets(0, 0, 0, 5);
		gbc_pauseButton.gridx = 0;
		gbc_pauseButton.gridy = 0;
		add(pauseButton, gbc_pauseButton);
		
		// videoTimeSlider의 범위를 초 단위로 0에서 영상 길이까지로 정한다.
		videoTimeSlider = new JSlider();
		videoTimeSlider.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				switch(arg0.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
					controls.skipTime(SKIP_TIME);
					break;
				case KeyEvent.VK_LEFT:
					controls.skipTime(-SKIP_TIME);
					break;
				}
			}
		});
		videoTimeSlider.setValue(0);
		mediaPlayer.media().parsing().parse(/*5000*/);
		while(mediaPlayer.media().parsing().status() != MediaParsedStatus.DONE); // 이 부분에서는 파싱이 완료될 때까지 기다린다.
		videoTimeSlider.setMaximum((int) (media.info().duration() / 1000));
		
		// 마우스를 드래그하면 해당 시간으로 동영상 시간을 변경
		videoTimeSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent arg0) {
				videoTimeLabel.setText( millisToSec(videoTimeSlider.getValue()* 1000) );
				controls.setTime(videoTimeSlider.getValue() * 1000);
			}
		});
		
		// videoTimeSlider 상에서 클릭을 하면 클릭한 위치로 이동
		videoTimeSlider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent arg0) {
				double sliderWidth = videoTimeSlider.getWidth();
				double mousePos = arg0.getX();
				videoTimeSlider.setValue( (int) (videoTimeSlider.getMaximum() * (mousePos/sliderWidth)) );
				controls.setTime(videoTimeSlider.getValue() * 1000);
			}
		});
		GridBagConstraints gbc_videoTimeSlider = new GridBagConstraints();
		gbc_videoTimeSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_videoTimeSlider.insets = new Insets(0, 0, 0, 5);
		gbc_videoTimeSlider.gridx = 1;
		gbc_videoTimeSlider.gridy = 0;
		add(videoTimeSlider, gbc_videoTimeSlider);
		
		videoTimeLabel = new JLabel("0:00:00");
		GridBagConstraints gbc_videoTimeLabel = new GridBagConstraints();
		gbc_videoTimeLabel.insets = new Insets(0, 0, 0, 5);
		gbc_videoTimeLabel.gridx = 2;
		gbc_videoTimeLabel.gridy = 0;
		add(videoTimeLabel, gbc_videoTimeLabel);
		
		videoVolumeSlider = new JSlider();
		videoVolumeSlider.setValue(mediaPlayer.audio().volume());
		videoVolumeSlider.setMaximumSize(new Dimension(VOLUME_MAX_SIZE, 20));
		videoVolumeSlider.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int currentVolume = videoVolumeSlider.getValue();
				switch(arg0.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
					videoVolumeSlider.setValue( currentVolume + VOLUME_TICK );
					break;
				case KeyEvent.VK_LEFT:
					videoVolumeSlider.setValue( currentVolume - VOLUME_TICK );
					break;
				}
				System.out.println(videoVolumeSlider.getValue());
				audio.setVolume(videoVolumeSlider.getValue());
			}
		});
		videoVolumeSlider.setMinorTickSpacing(5);
		videoVolumeSlider.setValue(100);
		videoVolumeSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent arg0) {
				videoVolumeSlider.setToolTipText(videoVolumeSlider.getValue()+"");
				audio.setVolume(videoVolumeSlider.getValue());
			}
		});
		
		// videoTimeSlider 상에서 클릭을 하면 클릭한 위치로 이동
		videoVolumeSlider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent arg0) {
				double sliderWidth = videoVolumeSlider.getWidth();
				double mousePos = arg0.getX();
				videoVolumeSlider.setValue( (int) (videoVolumeSlider.getMaximum() * (mousePos/sliderWidth)) );
				audio.setVolume(videoVolumeSlider.getValue());
			}
		});
		GridBagConstraints gbc_videoVolumeSlider = new GridBagConstraints();
		gbc_videoVolumeSlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_videoVolumeSlider.gridx = 3;
		gbc_videoVolumeSlider.gridy = 0;
		add(videoVolumeSlider, gbc_videoVolumeSlider);
		
		mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			
			@Override
			public void timeChanged(MediaPlayer arg0, long arg1) {
				videoTimeSlider.setValue((int)arg1/1000);
				videoTimeLabel.setText( millisToSec(arg1));
			}
		});
		

	}
	
	public String millisToSec(long millis) {
		return String.format("%d:%02d:%02d", millis/3600000, millis/60000%60, millis/1000%60);
	}

}
