package ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


import java.util.ArrayList;

public class NotificationData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<String> users;
	public String msg;
	public Date expiresOn;
	
	@Override
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return "users: " + users +
				", msg: " + msg +
				", expiresOn: " + expiresOn==null? null:format.format(expiresOn);
	}
}
