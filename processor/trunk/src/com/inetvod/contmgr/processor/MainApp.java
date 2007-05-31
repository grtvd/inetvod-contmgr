/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.inetvod.common.core.Logger;
import com.inetvod.common.core.StreamUtil;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.contmgr.data.ContentItemStatus;
import com.inetvod.contmgr.data.VideoCodec;
import com.inetvod.contmgr.dbdata.ContentItem;
import com.inetvod.contmgr.dbdata.ContentItemList;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class MainApp
{
	/* Constants */
	private static final long BYTES_PER_GIGABYTE = 1073741824;

	/* Fields */
	private static MainApp fMainApp = new MainApp();
	private long fMaxLocalStorage;
	private File fContentDir;

	/* Getters and Setters */
	public static MainApp getThe() { return fMainApp; }

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

		VCLManager.initialize(properties.getProperty("vlcapp"), properties.getProperty("transcodecommand"),
			Boolean.parseBoolean(properties.getProperty("vlcapp_logoutput")));
	}

	private static boolean processArgs(String[] args)
	{
		try
		{
			//noinspection RedundantIfStatement
			if((args == null) || (args.length == 0))
				return true;

			return false;

//			if(args.length != 2)
//				return false;
//
//			for(int i = 0; i < args.length; i++)
//			{
//				if("-p".equals(args[i]))
//				{
//					if(i < args.length - 1)
//					{
//						i++;
//						fProviderID = new ProviderID(args[i]);
//					}
//					else
//						return false;
//				}
//				else if("-pc".equals(args[i]))
//				{
//					if(i < args.length - 1)
//					{
//						i++;
//						fProviderConnectionID = new ProviderConnectionID(args[i]);
//					}
//					else
//						return false;
//				}
//				else
//					return false;
//			}

//			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private static void printUsage()
	{
		System.out.println("usage: process");
//		System.out.println("usage: process [options] [args]");
//		System.out.println("   -p <ProviderID>");
//		System.out.println("   -pc <ProviderConnectionID>");
	}

	private void doWork() throws Exception
	{
//		processRequest("http://media.libsyn.com/media/tooncast/27_-_Private_SNAFU_-_Home_Front.m4v", VideoCodec.convertFromString("WMV1"));
//		processRequest("http://media.libsyn.com/media/tooncast/13_-_Stupidstitious.m4v", VideoCodec.convertFromString(""));
//
//	String[] sourceURLs = new String[] {
//"http://www.podtrac.com/pts/redirect.mp4?http://media.g4tv.com/videoDB/016/101/video16101/as7085ps3elite_pod.mp4",
//"http://media.libsyn.com/media/tooncast/10_-_Caught.m4v",
//"http://media.libsyn.com/media/tooncast/35_-_Superman_-_Secret_Agent.m4v",
//"http://media.libsyn.com/media/tooncast/27_-_Private_SNAFU_-_Home_Front.m4v",
//"http://media.libsyn.com/media/tooncast/45_-_Peg_Leg_Pedro.m4v",
//"http://media.libsyn.com/media/tooncast/09_-_Felix.m4v"
//		};
//
//		for(String sourceURL : sourceURLs)
//			processRequest(sourceURL, VideoCodec.WMV2);

		while(processNextItem())
		{
			checkAndFreeLocalSpace();
		}
	}

	private static void processRequest(String sourceURL, VideoCodec needVideoCodec) throws Exception
	{
		ContentItem contentItem = ContentItem.getCreate(sourceURL, needVideoCodec);

		if(contentItem.getNeedVideoCodec() == null)
		{
			if(ContentItemStatus.NotLocal.equals(contentItem.getStatus()))
				contentItem.setStatus(ContentItemStatus.ToDownload);
		}
		else
		{
			ContentItem sourceContentItem = ContentItem.getCreate(sourceURL, null);
			if(ContentItemStatus.NotLocal.equals(sourceContentItem.getStatus()))
				contentItem.setStatus(ContentItemStatus.ToDownload);
			sourceContentItem.setRequestedAt();
			sourceContentItem.update();

			if(ContentItemStatus.NotLocal.equals(contentItem.getStatus()))
				contentItem.setStatus(ContentItemStatus.ToTranscode);
		}

		contentItem.setRequestedAt();
		contentItem.update();
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
			}
			else
			{
				ContentItem sourceContentItem = ContentItem.getCreate(contentItem.getSourceURL());
				if(!ContentItemStatus.Local.equals(sourceContentItem.getStatus()))
				{
					downloadContent(sourceContentItem);
				}
				else
				{
					transcodeContent(sourceContentItem, contentItem);
				}
			}

			return true;
		}

		return false;
	}

	private void downloadContent(ContentItem contentItem) throws Exception
	{
		contentItem.setFileSize(downloadFile(contentItem.getSourceURL(), contentItem.getLocalFilePath()));
		contentItem.setStatus(ContentItemStatus.Local);
		contentItem.update();
	}

	private long downloadFile(String sourceURL, String fileName) throws Exception
	{
		try
		{
			File file = new File(fContentDir, fileName);
			file.getParentFile().mkdirs();
			if(file.exists())
				file.delete();

			Logger.logInfo(this, "downloadFile", String.format("Downloading '%s' to '%s'", sourceURL, file.getAbsolutePath()));

			// Send HTTP request to server
			HttpClient httpClient = new HttpClient();
			//TODO httpClient.getParams().setParameter("http.socket.timeout", TimeoutMillis);
			GetMethod getMethod = new GetMethod(sourceURL);
			getMethod.setFollowRedirects(true);

			try
			{
				httpClient.executeMethod(getMethod);
				InputStream responseStream = getMethod.getResponseBodyAsStream();

				StreamUtil.streamToFile(responseStream, file.getAbsolutePath());

				long fileLen = file.length();
				if(fileLen == 0)
					throw new Exception(String.format("File(%s) is 0 length", file.getAbsolutePath()));
				return file.length();
			}
			finally
			{
				getMethod.releaseConnection();
			}
		}
		catch(Exception e)
		{
			Logger.logErr(this, "downloadFile", e);
			throw e;
		}
	}

	private void transcodeContent(ContentItem srcContentItem, ContentItem dstContentItem) throws Exception
	{
		dstContentItem.setFileSize(transcodeFile(srcContentItem.getLocalFilePath(), dstContentItem.getLocalFilePath()));
		dstContentItem.setStatus(ContentItemStatus.Local);
		dstContentItem.update();
	}

	private long transcodeFile(String srcFileName, String dstFileName) throws Exception
	{
		File srcFile = new File(fContentDir, srcFileName);
		File dstFile = new File(fContentDir, dstFileName);

		dstFile.getParentFile().mkdirs();
		if(dstFile.exists())
			dstFile.delete();

		Logger.logInfo(this, "transcodeFile", String.format("Transcoding '%s' to '%s'", srcFile.getAbsolutePath(),
			dstFile.getAbsolutePath()));

		return VCLManager.transcodeMedia(srcFile, dstFile);
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

			totalFileSize -= contentItem.getFileSize();
		}

		return totalFileSize;
	}
}
