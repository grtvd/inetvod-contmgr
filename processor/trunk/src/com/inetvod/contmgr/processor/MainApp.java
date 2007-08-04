/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import com.inetvod.common.core.Logger;
import com.inetvod.common.core.StrUtil;
import com.inetvod.common.core.StreamUtil;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.contmgr.data.ContentItemStatus;
import com.inetvod.contmgr.data.VideoCodec;
import com.inetvod.contmgr.dbdata.ContentItem;
import com.inetvod.contmgr.dbdata.ContentItemList;
import com.inetvod.contmgr.processor.mediainfo.MediaInfoItem;
import com.inetvod.contmgr.processor.mediainfo.MediaInfoManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class MainApp
{
	/* Constants */
	private static final long BYTES_PER_GIGABYTE = 1073741824;
	private static final int KILOS = 1000;
	private static final int MILLIS_PER_SECOND = 1000;

	/* Fields */
	private static MainApp fMainApp = new MainApp();
	private File fContentDir;
	private long fMaxLocalStorage;
	private short fRetryCount;
	private int fDownloadTimeoutSecs;

	/* Construction */
	private MainApp()
	{
	}

	/* Implementation */
	public static void main(String[] args)
	{
		try
		{
			fMainApp.init();
			if(processArgs(args))
				fMainApp.doWork();
			else
				printUsage();
		}
		catch(Exception e)
		{
			Logger.logErr(fMainApp, "main", e);
			e.printStackTrace();
		}
	}

	private void init() throws Exception
	{
		//noinspection MismatchedQueryAndUpdateOfCollection
		Properties properties = new Properties();
		FileInputStream propertiesFile = new FileInputStream(new File("processor.xml"));
		try
		{
			properties.loadFromXML(propertiesFile);
		}
		finally
		{
			propertiesFile.close();
		}

		Logger.initialize(properties.getProperty("log4j"), properties.getProperty("logdir"));

		DatabaseAdaptor.setDBConnectFile(properties.getProperty("dbconnect"));

		// Preload DatabaseAdaptors
		ContentItem.getDatabaseAdaptor();

		fContentDir = new File(properties.getProperty("contentDir"));
		fMaxLocalStorage = Long.parseLong(properties.getProperty("maxLocalStorageGigs")) * BYTES_PER_GIGABYTE;
		fRetryCount = Short.parseShort(properties.getProperty("retryCount"));
		fDownloadTimeoutSecs = Integer.parseInt(properties.getProperty("downloadTimeoutSecs")) * MILLIS_PER_SECOND;

		HashMap<VideoCodec, String> transcodeCommands = new HashMap<VideoCodec, String>();
		for(VideoCodec videoCodec : VideoCodec.values())
		{
			String transcodecommand = properties.getProperty(String.format("transcodecommand_%s", videoCodec.toString()));
			if(StrUtil.hasLen(transcodecommand))
				transcodeCommands.put(videoCodec, transcodecommand);
		}

		VCLManager.initialize(properties.getProperty("vlcapp"), transcodeCommands,
			Boolean.parseBoolean(properties.getProperty("vlcapp_logoutput")));

		MediaInfoManager.initialize(properties.getProperty("mediainfodll"));
	}

	private static boolean processArgs(String[] args)
	{
		try
		{
			//noinspection RedundantIfStatement
			if((args == null) || (args.length == 0))
				return true;

			return false;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private static void printUsage()
	{
		System.out.println("usage: process");
	}

	private void doWork() throws Exception
	{
		while(processNextItem())
		{
			checkAndFreeLocalSpace();
		}
	}

	private boolean processNextItem() throws Exception
	{
		ContentItem contentItem;

		ContentItemList contentItemList = ContentItemList.findByStatusToDownloadOrTranscode();
		if(contentItemList.size() != 0)
		{
			contentItem = contentItemList.get(0);
			if(ContentItemStatus.ToDownload.equals(contentItem.getStatus()))
			{
				downloadContent(contentItemList.get(0));
				determineContentInfo(contentItem);
			}
			else
			{
				ContentItem sourceContentItem = ContentItem.getCreate(contentItem.getSourceURL());
				if(ContentItemStatus.Error.equals(sourceContentItem.getStatus()))
				{
					contentItem.setStatus(ContentItemStatus.Error);
					contentItem.update();
				}
				else if(!ContentItemStatus.Local.equals(sourceContentItem.getStatus()))
				{
					downloadContent(sourceContentItem);
					determineContentInfo(sourceContentItem);
				}
				else
				{
					transcodeContent(sourceContentItem, contentItem);
					determineContentInfo(contentItem);
				}
			}

			return true;
		}

		return false;
	}

	private void downloadContent(ContentItem contentItem) throws Exception
	{
		long fileSize = downloadFile(contentItem.getSourceURL(), contentItem.getLocalFilePath());
		if(fileSize > 0)
		{
			contentItem.setFileSize(fileSize);
			contentItem.setStatus(ContentItemStatus.Local);
			contentItem.update();
		}
		//TODO for now a temporary fix, settting the RequestedAt date will move to bottom of queue, giving time to other itmes to be serviced
		else
		{
			contentItem.incRetryCount();
			if(contentItem.getRetryCount() >= fRetryCount)
				contentItem.setStatus(ContentItemStatus.Error);
			contentItem.setRequestedAt();
			contentItem.update();
		}
	}

	private long downloadFile(String sourceURL, String fileName) throws Exception
	{
		try
		{
			File file = new File(fContentDir, fileName);

			Logger.logInfo(this, "downloadFile", String.format("Downloading '%s' to '%s'", sourceURL, file.getAbsolutePath()));

			// Send HTTP request to server
			HttpClient httpClient = new HttpClient();
			if(fDownloadTimeoutSecs > 0)
				httpClient.getParams().setParameter("http.socket.timeout", fDownloadTimeoutSecs);
			GetMethod getMethod = new GetMethod(sourceURL);
			getMethod.setFollowRedirects(true);

			try
			{
				int rc = httpClient.executeMethod(getMethod);
				if(rc != HttpStatus.SC_OK)
				{
					Logger.logWarn(this, "downloadFile", String.format("Bad result(%d) from url(%s)", rc, sourceURL));
					return 0;
				}

				file.getParentFile().mkdirs();
				if(file.exists())
					file.delete();

				InputStream responseStream = getMethod.getResponseBodyAsStream();
				StreamUtil.streamToFile(responseStream, file.getAbsolutePath());

				if(!file.exists() || (file.length() == 0))
					Logger.logWarn(this, "downloadFile", String.format("File(%s) is 0 length or doesn't exist", file.getAbsolutePath()));
				return file.length();
			}
			finally
			{
				getMethod.releaseConnection();
			}
		}
		catch(Exception e)
		{
			Logger.logWarn(this, "downloadFile", e);
			return 0;
		}
	}

	private void transcodeContent(ContentItem srcContentItem, ContentItem dstContentItem) throws Exception
	{
		long fileSize = transcodeFile(srcContentItem.getLocalFilePath(), dstContentItem.getNeedVideoCodec(),
			dstContentItem.getLocalFilePath());
		if(fileSize > 0)
		{
			dstContentItem.setFileSize(fileSize);
			dstContentItem.setStatus(ContentItemStatus.Local);
			dstContentItem.update();
		}
		//TODO for now a temporary fix, settting the RequestedAt date will move to bottom of queue, giving time to other itmes to be serviced
		else
		{
			dstContentItem.incRetryCount();
			if(dstContentItem.getRetryCount() >= fRetryCount)
				dstContentItem.setStatus(ContentItemStatus.Error);
			dstContentItem.setRequestedAt();
			dstContentItem.update();
		}
	}

	private long transcodeFile(String srcFileName, VideoCodec dstVideoCodec, String dstFileName) throws Exception
	{
		File srcFile = new File(fContentDir, srcFileName);
		File dstFile = new File(fContentDir, dstFileName);

		dstFile.getParentFile().mkdirs();
		if(dstFile.exists())
			dstFile.delete();

		Logger.logInfo(this, "transcodeFile", String.format("Transcoding '%s' to '%s'", srcFile.getAbsolutePath(),
			dstFile.getAbsolutePath()));

		return VCLManager.transcodeMedia(srcFile, dstVideoCodec, dstFile);
	}

	private void determineContentInfo(ContentItem contentItem) throws Exception
	{
		String filename = (new File(fContentDir, contentItem.getLocalFilePath())).getAbsolutePath();
		MediaInfoItem mediaInfoItem = MediaInfoManager.getFileInfo(filename);
		if(mediaInfoItem != null)
		{
			contentItem.setVideoCodec(mediaInfoItem.getVideoCodec());
			contentItem.setAudioCodec(mediaInfoItem.getAudioCodec());
			contentItem.setHorzResolution(convertIntegerToShort(mediaInfoItem.getWidth()));
			contentItem.setVertResolution(convertIntegerToShort(mediaInfoItem.getHeight()));
			contentItem.setFramesPerSecond(convertFrameRate(mediaInfoItem.getFrameRate()));
			contentItem.setBitRate(convertBitRate(mediaInfoItem.getBitRate()));
			contentItem.setRunningTimeSecs(convertPlayTime(mediaInfoItem.getPlayTime()));
			contentItem.update();
		}
	}

	private static Short convertIntegerToShort(Integer value)
	{
		if(value == null)
			return null;

		return (short)(int)value;
	}

	private static Short convertFrameRate(Float frameRate)
	{
		if(frameRate == null)
			return null;

		return (short)Math.ceil(frameRate);
	}

	private static Short convertBitRate(Integer bitRate)
	{
		if(bitRate == null)
			return null;

		return convertIntegerToShort((int)Math.round((double)bitRate / (double)KILOS));
	}

	private static Integer convertPlayTime(Integer playTime)
	{
		if(playTime == null)
			return null;
		if(playTime == 0)
			return 0;

		return (int)(Math.ceil(playTime) / (double)MILLIS_PER_SECOND);
	}

	private void checkAndFreeLocalSpace() throws Exception
	{
		long totalFileSize = ContentItemList.countTotalFileSizeForLocal();

		if(totalFileSize > fMaxLocalStorage)
			totalFileSize = deleteContentForListUntilMaxLocalStorage(ContentItemList.findBySoloLocal(), totalFileSize);

		if(totalFileSize > fMaxLocalStorage)
			totalFileSize = deleteContentForListUntilMaxLocalStorage(ContentItemList.findBySoloLocalNoToTranscode(),
				totalFileSize);

		if(totalFileSize > fMaxLocalStorage)
			totalFileSize = deleteContentForListUntilMaxLocalStorage(ContentItemList.findByLocalWasTranscoded(),
				totalFileSize);

		if(totalFileSize > fMaxLocalStorage)
			Logger.logWarn(this, "checkAndFreeLocalSpace", String.format("TotalFileSize(%d) still greater than MaxLocalStorage(%d)",
				totalFileSize, fMaxLocalStorage));
	}

	private long deleteContentForListUntilMaxLocalStorage(ContentItemList contentItemList, long totalFileSize)
		throws Exception
	{
		for(ContentItem contentItem : contentItemList)
		{
			if(totalFileSize <= fMaxLocalStorage)
				break;

			contentItem.setStatus(ContentItemStatus.NotLocal);
			contentItem.update();

			File file = new File(fContentDir, contentItem.getLocalFilePath());
			if(!file.delete())
				Logger.logErr(this, "deleteContentForListUntilMaxLocalStorage", String.format(
					"Failed to delete file '%s'", file.getAbsolutePath()));
			if(file.getParentFile().listFiles().length == 0)
				file.getParentFile().delete();

			totalFileSize -= contentItem.getFileSize();
		}

		return totalFileSize;
	}
}
