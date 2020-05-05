package ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SuspendData {
	public ArrayList<String> users;
	public Date suspendedUntil;
	
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return "users: " + users +
				", suspendedUntil: " + format.format(suspendedUntil);
	}
}