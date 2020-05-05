package io;
import java.io.*;
import network.MessagePacket;

public class IoUtil {
	
	public static final int COPY_BUFFER_SIZE = 1024 * 128;
	public static final int FTP_BUFFER_SIZE = 1024 * 1024;
	//public static int SEND_BUFFER_SIZE = 1024 * 1024 * 8;
	//public static int RECEIVE_BUFFER_SIZE = 1024 * 1024 * 8;
	
	// MessagePacket을 보낸다
	public static void sendMessagePacket(MessagePacket msg, ObjectOutputStream out) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// MessagePacket을 받는다
	public static MessagePacket receiveMessagePacket(ObjectInputStream in) {
		MessagePacket msg = null;
		try {
			msg = (MessagePacket) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.getCause());
		}
		
		return msg;
	}
	
}
