/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.inetvod.common.core.Logger;

public class VCLManager
{
	/* Fields */
	private static String fVLCApp;
	private static String fTranscodeCommand; //= "-I dummy --quiet -vvv \"%s\" --sout '#transcode{vcodec=WMV2,vb=1024,scale=1,acodec=mp3,ab=192,channels=2}:standard{access=file,mux=asf,dst=\"%s\"}' vlc:quit";
	private static boolean fLogProcessOutput;

	/* Getters and Setters */

	/* Construction */
	public static void initialize(String vlcApp, String transcodeCommand, boolean logProcessOutput)
	{
		fVLCApp = vlcApp;
		fTranscodeCommand = transcodeCommand;
		fLogProcessOutput = logProcessOutput;
	}

	/* Implementation */
	public static long transcodeMedia(File inputFile, File outputFile) throws Exception
	{
		String commandArgs = String.format(fTranscodeCommand, inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
		String fullCommand = String.format("\"%s\" %s", fVLCApp, commandArgs);

		Logger.logInfo(VCLManager.class, "transcodeMedia", fullCommand);

		StreamEaterThread inputEaterThread = null;
		StreamEaterThread errorEaterThread = null;

		Process process = Runtime.getRuntime().exec(fullCommand, null, inputFile.getParentFile());
		try
		{
			inputEaterThread = new StreamEaterThread(process.getInputStream(), "vcl_input", fLogProcessOutput);
			inputEaterThread.start();

			errorEaterThread = new StreamEaterThread(process.getErrorStream(), "vcl_error", fLogProcessOutput);
			errorEaterThread.start();

			int rc = process.waitFor();
			if(rc != 0)
				throw new Exception(String.format("process failed, rc(%d)", rc));

			long fileLen = outputFile.length();
			if(fileLen == 0)
				throw new Exception(String.format("File(%s) is 0 length", outputFile.getAbsolutePath()));
			return outputFile.length();
		}
		catch(Exception e)
		{
			Logger.logErr(VCLManager.class, "transcodeMedia", e);
			throw e;
		}
		finally
		{
			if(inputEaterThread != null)
				inputEaterThread.setQuit();
			if(errorEaterThread != null)
				errorEaterThread.setQuit();
			process.destroy();
		}
	}

	private static class StreamEaterThread extends Thread
	{
		/* Contants */
		private static final int BUF_SIZE = 1024;

		/* Fields */
		private InputStream fInputStream;
		private InputStreamReader fInputStreamReader;
		private boolean fSaveToLog;
		private boolean fQuit;

		/* Getters and Setters */
		public void setQuit() { fQuit = true; }

		/* Construction */
		public StreamEaterThread(InputStream inputStream, String name, boolean saveToLog)
		{
			super(name);
			fInputStream = inputStream;
			fInputStreamReader = new InputStreamReader(fInputStream);
			fSaveToLog = saveToLog;
		}

		/* Implementation */
		public void run()
		{
			readAll();
			//Logger.logInfo(this, "run", String.format("Thread(%s) exiting", getName()));
		}

		private void readAll()
		{
			char[] chars = new char[BUF_SIZE];
			int length;

			try
			{
				while(!fQuit && (length = fInputStreamReader.read(chars, 0, BUF_SIZE)) != -1)
				{
					if(fSaveToLog)
						Logger.logInfo(this, "readAll", String.format("Thread(%s), Value: '%s'", getName(), String.valueOf(chars, 0, length)));
				}
			}
			catch(Exception e)
			{
				Logger.logErr(this, "readAll", String.format("Thread(%s), Failed while reading from '%s'", getName(), fInputStream), e);
			}
		}
	}
}
