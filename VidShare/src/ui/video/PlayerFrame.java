package ui.video;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class PlayerFrame extends JFrame {

	private JPanel contentPane;
	Controls controls;
	EmbeddedMediaPlayerComponent component;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					PlayerFrame frame = new PlayerFrame("C:\\Users\\dongh\\Documents\\카카오톡 받은 파일\\KakaoTalk_Video_20191110_1640_59_985.mp4");
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the frame.
	 */
	public PlayerFrame(String filePath) {
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				component.release();
				dispose();
			}
			
		});
		setBounds(100, 100, 450, 300);
		setTitle(filePath.substring(filePath.lastIndexOf('\\') + 1));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5,5,5,5));
		
		setVisible(true);

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWeights = new double[]{1.0};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0};
		contentPane.setLayout(gbl_contentPane);
		
		
		GridBagConstraints gbc_component = new GridBagConstraints();
		gbc_component.insets = new Insets(0, 0, 5, 5);
		gbc_component.fill = GridBagConstraints.BOTH;
		gbc_component.gridx = 0;
		gbc_component.gridy = 0;
		component = new EmbeddedMediaPlayerComponent();
		contentPane.add(component, gbc_component);
		
		component.mediaPlayer().media().play(filePath);
		controls = new Controls(component.mediaPlayer());
		
		
		GridBagConstraints gbc_controls = new GridBagConstraints();
		gbc_controls.fill = GridBagConstraints.BOTH;
		gbc_controls.gridx = 0;
		gbc_controls.gridy = 1;
		contentPane.add(controls, gbc_controls);
		
		
		
		
	}

}
