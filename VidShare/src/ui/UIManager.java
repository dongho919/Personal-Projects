package ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class UIManager {
	
	
	final static SystemTray tray = SystemTray.getSystemTray();
	static TrayIcon trayIcon;
	final static String ICON_LOCATION = "/vidshare-icon2.png";
	
	public static void addTrayListener(JFrame frame) {
		
		
		
        if(SystemTray.isSupported()){

            //Image image= new ImageIcon(UIManager.class.getResource(ICON_LOCATION)).getImage();
        	Image image = new ImageIcon(ICON_LOCATION).getImage();
            ActionListener exitListener=new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
            PopupMenu popup=new PopupMenu();
            MenuItem defaultItem=new MenuItem("종료");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            defaultItem=new MenuItem("열기");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(true);
                    frame.setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
            trayIcon = new TrayIcon(image, MainFrame.VERSION, popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
            	@Override
            	public void mouseReleased(MouseEvent e) {
            		if(e.getClickCount() == 2) {
            			frame.setVisible(true);
                        frame.setExtendedState(JFrame.NORMAL);
            		}
            	}
            });
        }
        frame.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
        if(e.getNewState()==JFrame.ICONIFIED){
          try{
            tray.add(trayIcon);
            frame.setVisible(false);
            System.out.println("added to SystemTray");
            }catch(AWTException ex){
            System.out.println("unable to add to system tray");
        }
            }
        if(e.getNewState()==JFrame.MAXIMIZED_BOTH){
                    tray.remove(trayIcon);
                    frame.setVisible(true);
                    System.out.println("Tray icon removed");
                }
                if(e.getNewState()==JFrame.NORMAL){
                    tray.remove(trayIcon);
                    frame.setVisible(true);
                }
            }
        });
    }
}
