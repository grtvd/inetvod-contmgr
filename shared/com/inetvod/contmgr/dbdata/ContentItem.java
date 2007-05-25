/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.dbdata;

import java.util.Date;

import com.inetvod.common.core.DataReader;
import com.inetvod.common.core.DataWriter;
import com.inetvod.common.core.FileExtension;
import com.inetvod.common.core.FileUtil;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.common.dbdata.DatabaseObject;
import com.inetvod.contmgr.data.ContentItemID;
import com.inetvod.contmgr.data.ContentItemStatus;
import com.inetvod.contmgr.data.VideoCodec;

public class ContentItem extends DatabaseObject
{
	/* Constants */
	private static final int SourceURLMaxLength = 892;
	private static final int NeedVideoCodecMaxLength = 8;
	private static final int LocalFilePathMaxLength = 64;
	private static final int VideoCodecMaxLength = 8;
	private static final int AudioCodecMaxLength = 8;

	/* Fields */
	private ContentItemID fContentItemID;
	private String fSourceURL;
	private VideoCodec fNeedVideoCodec;
	private Date fRequestedAt;
	private ContentItemStatus fStatus;
	private String fLocalFilePath;
	private Long fFileSize;
	private VideoCodec fVideoCodec;
	private String fAudioCodec;
	private boolean fCanRelease;

	private static DatabaseAdaptor<ContentItem, ContentItemList> fDatabaseAdaptor =
		new DatabaseAdaptor<ContentItem, ContentItemList>(ContentItem.class, ContentItemList.class);
	public static DatabaseAdaptor<ContentItem, ContentItemList> getDatabaseAdaptor() { return fDatabaseAdaptor; }

	/* Getters and Setters */
	public ContentItemID getContentItemID() { return fContentItemID; }
	public String getSourceURL() { return fSourceURL; }
	public VideoCodec getNeedVideoCodec() { return fNeedVideoCodec; }

	public Date getRequestedAt() { return fRequestedAt; }
	public void setRequestedAt(Date requestedAt) { fRequestedAt = requestedAt; }

	public ContentItemStatus getStatus() { return fStatus; }
	public void setStatus(ContentItemStatus status)
	{
		if(ContentItemStatus.ToTranscode.equals(status) && (fVideoCodec == null))
			throw new IllegalArgumentException("Can't set to ToTranscode when fVideoCodec == null");
		if(ContentItemStatus.ToDownload.equals(status) && (fVideoCodec != null))
			throw new IllegalArgumentException("Can't set to ToDownload when fVideoCodec != null");
		fStatus = status;
	}

	public String getLocalFilePath() { return fLocalFilePath; }
	public void setLocalFilePath(String localFilePath) { fLocalFilePath = localFilePath; }

	public Long getFileSize() { return fFileSize; }
	public void setFileSize(Long fileSize) { fFileSize = fileSize; }

	public VideoCodec getVideoCodec() { return fVideoCodec; }
	public void setVideoCodec(VideoCodec videoCodec) { fVideoCodec = videoCodec; }

	public String getAudioCodec() { return fAudioCodec; }
	public void setAudioCodec(String audioCodec) { fAudioCodec = audioCodec; }

	public boolean isCanRelease() { return fCanRelease; }
	public void setCanRelease(boolean canRelease) { fCanRelease = canRelease; }

	/* Construction */
	private ContentItem(String sourceURL, VideoCodec needVideoCodec)
	{
		super(true);
		fContentItemID = ContentItemID.newInstance();
		fRequestedAt = new Date();
		fSourceURL = sourceURL;
		fNeedVideoCodec = needVideoCodec;

		if(fNeedVideoCodec == null)
			fStatus = ContentItemStatus.ToDownload;
		else
			fStatus = ContentItemStatus.ToTranscode;

		String localFileName = fContentItemID.toString();
		FileExtension fileExtension;
		if(fNeedVideoCodec == null)
			fileExtension = FileUtil.determineFileExtFromURL(sourceURL);
		else
			fileExtension = fNeedVideoCodec.getDefaultFileExtension();
		fLocalFilePath = String.format("%s/%s", localFileName.substring(0,3),
			FileUtil.buildFileName(localFileName, fileExtension));
	}

	public ContentItem(DataReader reader) throws Exception
	{
		super(reader);
		readFrom(reader);
	}

	public static ContentItem newInstance(String sourceURL, VideoCodec needVideoCodec)
	{
		return new ContentItem(sourceURL, needVideoCodec);
	}

	public static ContentItem newInstance(String sourceURL)
	{
		return newInstance(sourceURL, null);
	}

	public static ContentItem getCreate(String sourceURL, VideoCodec needVideoCodec) throws Exception
	{
		ContentItemList contentItemList = ContentItemList.findBySourceURLNeedVideoCodec(sourceURL, needVideoCodec);

		if(contentItemList.size() == 0)
			return newInstance(sourceURL, needVideoCodec);
		if(contentItemList.size() == 1)
			return contentItemList.get(0);

		throw new Exception(String.format("Too many ContentItems found(%d) for sourceURL(%s), needVideoCodec(%s)",
			contentItemList.size(), sourceURL, needVideoCodec));
	}

	public static ContentItem getCreate(String sourceURL) throws Exception
	{
		return getCreate(sourceURL, null);
	}

	/* Implementation */
	public void readFrom(DataReader reader) throws Exception
	{
		fContentItemID = reader.readDataID("ContentItemID", ContentItemID.MaxLength, ContentItemID.CtorString);
		fSourceURL = reader.readString("SourceURL", SourceURLMaxLength);
		fNeedVideoCodec = VideoCodec.convertFromString(reader.readString("NeedVideoCodec", NeedVideoCodecMaxLength));
		fRequestedAt = reader.readDateTime("RequestedAt");
		fStatus = ContentItemStatus.convertFromString(reader.readString("Status", ContentItemStatus.MaxLength));
		fLocalFilePath = reader.readString("LocalFilePath", LocalFilePathMaxLength);
		fFileSize = reader.readLong("FileSize");
		fVideoCodec = VideoCodec.convertFromString(reader.readString("VideoCodec", VideoCodecMaxLength));
		fAudioCodec = reader.readString("AudioCodec", AudioCodecMaxLength);
		fCanRelease = reader.readBooleanValue("CanRelease");
	}

	public void writeTo(DataWriter writer) throws Exception
	{
		writer.writeDataID("ContentItemID", fContentItemID, ContentItemID.MaxLength);
		writer.writeString("SourceURL", fSourceURL, SourceURLMaxLength);
		writer.writeString("NeedVideoCodec", VideoCodec.convertToString(fNeedVideoCodec), NeedVideoCodecMaxLength);
		writer.writeDateTime("RequestedAt", fRequestedAt);
		writer.writeString("Status", ContentItemStatus.convertToString(fStatus), ContentItemStatus.MaxLength);
		writer.writeString("LocalFilePath", fLocalFilePath, LocalFilePathMaxLength);
		writer.writeLong("FileSize", fFileSize);
		writer.writeString("VideoCodec", VideoCodec.convertToString(fVideoCodec), VideoCodecMaxLength);
		writer.writeString("AudioCodec", fAudioCodec, AudioCodecMaxLength);
		writer.writeBooleanValue("CanRelease", fCanRelease);
	}

	public void update() throws Exception
	{
		fDatabaseAdaptor.update(this);
	}

	public void delete() throws Exception
	{
		fDatabaseAdaptor.delete(fContentItemID);
	}
}
