/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.dbdata;

import java.util.Date;

import com.inetvod.common.core.DataReader;
import com.inetvod.common.core.DataWriter;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.common.dbdata.DatabaseObject;
import com.inetvod.contmgr.data.ContentItemID;

public class ContentItem extends DatabaseObject
{
	/* Constants */
	private static final int SourceURLMaxLength = 892;
	private static final int NeedVideoCodecMaxLength = 8;
	private static final int StatusMaxLength = 16;
	private static final int LocalFilePathMaxLength = 64;
	private static final int VideoCodecMaxLength = 8;
	private static final int AudioCodecMaxLength = 8;

	/* Fields */
	private ContentItemID fContentItemID;
	private String fSourceURL;
	private String fNeedVideoCodec;
	private Date fRequestedAt;
	private String fStatus;
	private String fLocalFilePath;
	private Integer fFileSize;
	private String fVideoCodec;
	private String fAudioCodec;
	private boolean fCanRelease;

	private static DatabaseAdaptor<ContentItem, ContentItemList> fDatabaseAdaptor =
		new DatabaseAdaptor<ContentItem, ContentItemList>(ContentItem.class, ContentItemList.class);
	public static DatabaseAdaptor<ContentItem, ContentItemList> getDatabaseAdaptor() { return fDatabaseAdaptor; }

	/* Getters and Setters */
	public ContentItemID getContentItemID() { return fContentItemID; }
	public String getSourceURL() { return fSourceURL; }
	public String getNeedVideoCodec() { return fNeedVideoCodec; }

	public Date getRequestedAt() { return fRequestedAt; }
	public void setRequestedAt(Date requestedAt) { fRequestedAt = requestedAt; }

	public String getStatus() { return fStatus; }
	public void setStatus(String status) { fStatus = status; }

	public String getLocalFilePath() { return fLocalFilePath; }
	public void setLocalFilePath(String localFilePath) { fLocalFilePath = localFilePath; }

	public Integer getFileSize() { return fFileSize; }
	public void setFileSize(Integer fileSize) { fFileSize = fileSize; }

	public String getVideoCodec() { return fVideoCodec; }
	public void setVideoCodec(String videoCodec) { fVideoCodec = videoCodec; }

	public String getAudioCodec() { return fAudioCodec; }
	public void setAudioCodec(String audioCodec) { fAudioCodec = audioCodec; }

	public boolean isCanRelease() { return fCanRelease; }
	public void setCanRelease(boolean canRelease) { fCanRelease = canRelease; }

	/* Construction */
	private ContentItem(String sourceURL, String needVideoCodec)
	{
		super(true);
		fContentItemID = ContentItemID.newInstance();
		fSourceURL = sourceURL;
		fNeedVideoCodec = needVideoCodec;
	}

	public ContentItem(DataReader reader) throws Exception
	{
		super(reader);
		readFrom(reader);
	}

	public static ContentItem newInstance(String sourceURL, String needVideoCodec)
	{
		return new ContentItem(sourceURL, needVideoCodec);
	}

	public static ContentItem newInstance(String sourceURL)
	{
		return newInstance(sourceURL, null);
	}

	public static ContentItem getCreate(String sourceURL, String needVideoCodec) throws Exception
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
		fNeedVideoCodec = reader.readString("NeedVideoCodec", NeedVideoCodecMaxLength);
		fRequestedAt = reader.readDateTime("RequestedAt");
		fStatus = reader.readString("Status", StatusMaxLength);
		fLocalFilePath = reader.readString("LocalFilePath", LocalFilePathMaxLength);
		fFileSize = reader.readInt("FileSize");
		fVideoCodec = reader.readString("VideoCodec", VideoCodecMaxLength);
		fAudioCodec = reader.readString("AudioCodec", AudioCodecMaxLength);
		fCanRelease = reader.readBooleanValue("CanRelease");
	}

	public void writeTo(DataWriter writer) throws Exception
	{
		writer.writeDataID("ContentItemID", fContentItemID, ContentItemID.MaxLength);
		writer.writeString("SourceURL", fSourceURL, SourceURLMaxLength);
		writer.writeString("NeedVideoCodec", fNeedVideoCodec, NeedVideoCodecMaxLength);
		writer.writeDateTime("RequestedAt", fRequestedAt);
		writer.writeString("Status", fStatus, StatusMaxLength);
		writer.writeString("LocalFilePath", fLocalFilePath, LocalFilePathMaxLength);
		writer.writeInt("FileSize", fFileSize);
		writer.writeString("VideoCodec", fVideoCodec, VideoCodecMaxLength);
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
