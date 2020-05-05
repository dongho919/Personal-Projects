package io;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Video implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String thumbnailFile;
	public String title;
	public String fileName;
	public long fileSize;
	public String uploaderId;
	public Date uploadTime;
	public String[] tags;
	
	
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return	"thumbnailFile: " + thumbnailFile +
				", title: " + title +
				", fileName: " + fileName +
				", fileSize: " + fileSize +
				", uploaderId: " + uploaderId +
				", uploadTime: " + format.format(uploadTime) +
				", tags: " + Arrays.toString(tags);
	}
	
	@Override
	public Object clone() {
		Video video = new Video();
		video.thumbnailFile = thumbnailFile == null? null : new String(thumbnailFile);
		video.title = new String(title);
		video.fileName = new String(fileName);
		video.fileSize = fileSize;
		video.uploaderId = new String(uploaderId);
		video.uploadTime = (Date)uploadTime.clone();
		video.tags = tags.clone();
		
		return video;
	}
}