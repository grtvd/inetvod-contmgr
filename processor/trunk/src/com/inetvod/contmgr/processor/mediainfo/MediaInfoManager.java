/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor.mediainfo;

import net.sourceforge.mediainfo.MediaInfo;

public class MediaInfoManager
{
	/* Construction */
	public static void initialize(String mediaInfoDLL) throws Exception
	{
		MediaInfo.setLibraryName(mediaInfoDLL);
		MediaInfo.Option_Static("Internet", "No");
	}

	/* Implementation */
	public static MediaInfoItem getFileInfo(String filename) throws Exception
	{
		return MediaInfoItem.newInstance(filename);
	}

//	public static void test() throws Exception
//	{
//		File dir = new File("M:\\test_media");
//
//
//		MediaInfoItem mediaInfoItem;
////		mediaInfoItem = getFileInfo("m:\\test_media\\Episode 12_ The Good Guy.mp3");
//
//		for(File file : dir.listFiles())
//			if(file.isFile())
//			{
//				System.out.println(file.getAbsolutePath());
//				getFileInfo(file.getAbsolutePath());
//			}
//
////		mediaInfoItem = getFileInfo("C:\\Documents and Settings\\Bob\\My Documents\\My Videos\\BillyTheWhiteStripes.avi");
////		mediaInfoItem = getFileInfo("C:\\Documents and Settings\\Bob\\My Documents\\My Music\\My Sounds\\THX.mp3");
////		mediaInfoItem = getFileInfo("M:\\Movies\\The L Word - ''Pilot''.mp4");
////		mediaInfoItem = getFileInfo("M:\\Movies\\tour_ggt3.wmv");
//		//File file = new File("C:\\Program Files\\iNetVOD\\contmgr\\content\\0b6\\0b6f4ac4-81e3-4ace-a484-277de061f746.mov");
//		//File file = new File("C:\\Program Files\\iNetVOD\\contmgr\\content\\1a2\\1a2aece9-99b8-47d0-9c04-ba47884ab603.mov");
//		//File file = new File("C:\\Documents and Settings\\Bob\\My Documents\\My Music\\My Sounds\\THX.mp3");
//
//		//System.out.println(displayFileSummary(file.getAbsolutePath()));
//	}

//	private static String displayFileSummary(String filename) throws Exception
//	{
//	/* Path to the library */
//	//MediaInfo.setLibraryName("MediaInfo.dll");
//
////	String To_Display = "";
//
//	//Info about the library
//
////	To_Display += MediaInfo.Option_Static("Info_Version");
//		System.out.println(MediaInfo.Option_Static("Info_Version"));
//
////	To_Display += "\r\n\r\nInfo_Parameters\r\n";
////	To_Display += MediaInfo.Option_Static("Info_Parameters");
////
////	To_Display += "\r\n\r\nInfo_Capacities\r\n";
////	To_Display += MediaInfo.Option_Static("Info_Capacities");
////
////	To_Display += "\r\n\r\nInfo_Codecs\r\n";
////	To_Display += MediaInfo.Option_Static("Info_Codecs");
//		System.out.println(MediaInfo.Option_Static("Info_Codecs"));
//
//
//	//An example of how to use the library
//
//	MediaInfo MI = new MediaInfo();
//
////	To_Display += "\r\n\r\nOpen\r\n";
//		int rc = MI.Open(filename);
//		System.out.println(String.format("Open: %d", rc));
//
////	To_Display += "\r\n\r\nInform with Complete=false\r\n";
////	MI.Option("Complete", "");
////	To_Display += MI.Inform();
////
////	To_Display += "\r\n\r\nInform with Complete=true\r\n";
////	MI.Option("Complete", "1");
////	To_Display += MI.Inform();
//		MI.Option("Complete", "1");
//		System.out.println("Inform complete:");
//		System.out.println(MI.Inform());
//		System.out.println();
//
////	To_Display += "\r\n\r\nCustom Inform\r\n";
////	MI.Option("Inform", "General;Example : FileSize=%FileSize%");
////	To_Display += MI.Inform();
//
//		int count = MI.Count_Get(MediaInfo.Stream_General);
//		System.out.println(String.format("Count: %s", count));
//
//		for(int i = 0; i < count; i++)
//			System.out.println(MI.Get(MediaInfo.Stream_General, 0, i));
//
//		count = Integer.parseInt(MI.Get(MediaInfo.Stream_Audio, 0, 0, MediaInfo.Info_Text));
//		System.out.println(String.format("Count: %s", count));
//
//		for(int i = 1; i <= count; i++)
//		{
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Name));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Name_Text));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_HowTo));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Info));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Text));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Max));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Measure));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Measure_Text));
//			System.out.print("/");
//			System.out.print(MI.Get(MediaInfo.Stream_Audio, 0, i, MediaInfo.Info_Options));
//			System.out.println();
//		}
//
//		System.out.println(MI.Get(MediaInfo.Stream_Audio, 0, "Codec/CC", MediaInfo.Info_Text));
//
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Name));
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec/cc", MediaInfo.Info_Text));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Name_Text));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_HowTo));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Info));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Max));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Measure));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Measure_Text));
////		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Codec", MediaInfo.Info_Options));
//
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Width", MediaInfo.Info_Text));
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "Height", MediaInfo.Info_Text));
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "FrameRate", MediaInfo.Info_Text));
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "BitRate", MediaInfo.Info_Text));
//		System.out.println(MI.Get(MediaInfo.Stream_Video, 0, "PlayTime", MediaInfo.Info_Text));	//seconds
//
//
////	To_Display += "\r\n\r\nGetI with Stream=General and Parameter=13\r\n";
////	To_Display += MI.Get(MediaInfo.Stream_General, 0, 46, MediaInfo.Info_Text);
////
////	To_Display += "\r\n\r\nCount_Get with StreamKind=Stream_Audio\r\n";
////	To_Display += MI.Count_Get(MediaInfo.Stream_Audio, -1);
//
////	To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"AudioCount\"\r\n";
////	To_Display += MI.Get(MediaInfo.Stream_General, 0, "AudioCount", MediaInfo.Info_Text, MediaInfo.Info_Name);
////
////	To_Display += "\r\n\r\nGet with Stream=Audio and Parameter=\"StreamCount\"\r\n";
////	To_Display += MI.Get(MediaInfo.Stream_Audio, 0, "StreamCount", MediaInfo.Info_Text, MediaInfo.Info_Name);
////
////	To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"FileSize\"\r\n";
////	To_Display += MI.Get(MediaInfo.Stream_General, 0, "FileSize", MediaInfo.Info_Text, MediaInfo.Info_Name);
//
////	To_Display += "\r\n\r\nClose\r\n";
//	System.out.println("Close");
//	MI.Close();
//
////		return To_Display;
//		return "";
//	}
}
