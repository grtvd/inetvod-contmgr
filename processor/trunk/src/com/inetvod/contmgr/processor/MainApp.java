/**
 * Copyright © 2006 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;

import com.inetvod.common.core.Logger;
import com.inetvod.common.dbdata.DatabaseAdaptor;

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
		//ContentItem.getDatabaseAdaptor();
	}

	private static boolean processArgs(String[] args)
	{
		try
		{
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

	private void doWork() throws Exception
	{

	}
}
