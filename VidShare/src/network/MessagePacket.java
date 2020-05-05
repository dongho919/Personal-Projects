package network;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessagePacket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String msgType;
	public String content;
	public String senderId;
	public Object attachment;
	public Date sendDate;
	
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		return "msgType: " + msgType +
				", content: " + content +
				", senderId: " + senderId +
				", sendDate: " + format.format(sendDate);
	}

}
