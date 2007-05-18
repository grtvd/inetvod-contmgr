/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import com.inetvod.common.core.Logger;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.contmgr.dbdata.ContentItem;

public class MainApp
{
	/* Fields */
	private static MainApp fMainApp = new MainApp();

	/* Getters and Setters */
	public static MainApp getThe() { return fMainApp; }

	/* Construction */
	private MainApp()
	{
	}

	/* Implementation */
	@SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
	public static void main(String[] args)
	{
		try
		{
			fMainApp.init();
			if(fMainApp.processArgs(args))
				fMainApp.doWork();
			else
				fMainApp.printUsage();
		}
		catch(Exception e)
		{
			Logger.logErr(fMainApp, "main", e);
			e.printStackTrace();
		}
	}

	private static void init() throws Exception
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
		System.out.println("usage: apiservice [options] [args]");
		System.out.println("   -p <ProviderID>");
		System.out.println("   -pc <ProviderConnectionID>");
	}

	private static void doWork() throws Exception
	{
		ContentItem contentItem = ContentItem.getCreate("a");
		contentItem.setRequestedAt(new Date());
		contentItem.setStatus("ToDownload");
		contentItem.update();
	}
}
