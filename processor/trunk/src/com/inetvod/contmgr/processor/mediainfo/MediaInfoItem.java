/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor.mediainfo;

import java.util.HashMap;

import com.inetvod.common.core.Logger;
import com.inetvod.common.core.StrUtil;
import net.sourceforge.mediainfo.MediaInfo;

public class MediaInfoItem
{
	/* Fields */
	private static HashMap<String, String> fCodecMap;

	private String fFileName;
	private String fCodec;
	private Integer fWidth;
	private Integer fHeight;
	private Float fFrameRate;	// frames per second
	private Integer fBitRate;
	private Integer fPlayTime;	// milli-seconds

	/* Getters and Setters */
	public String getCodec() { return fCodec; }
	public Integer getWidth() { return fWidth; }
	public Integer getHeight() { return fHeight; }
	public Float getFrameRate() { return fFrameRate; }
	public Integer getBitRate() { return fBitRate; }
	public Integer getPlayTime() { return fPlayTime; }

	/* Construction */
	static
	{
		fCodecMap = new HashMap<String, String>();
		fCodecMap.put("avc1","avc1");	//.mp4
		fCodecMap.put("20","mp4v");		//.mp4
		fCodecMap.put("40","mp4a");		//.m4a
		fCodecMap.put("MPA1L3","mpga");	//.mp3
		fCodecMap.put("MPA2L3","mpga");	//.mp3
	}

	private MediaInfoItem(String fileName)
	{
		fFileName = fileName;
	}

	/* Implementation */
	public static MediaInfoItem newInstance(String fileName) throws Exception
	{
		MediaInfoItem mediaInfoItem = new MediaInfoItem(fileName);

		MediaInfo mediaInfo = new MediaInfo();
		if(mediaInfo.Open(fileName) != 1)
			throw new Exception(String.format("Failed to open file(%s)", fileName));

		try
		{
			//System.out.println(mediaInfo.Inform());

			mediaInfoItem.fPlayTime = parseInteger(mediaInfo.Get(MediaInfo.Stream_General, 0, "PlayTime", MediaInfo.Info_Text));

			mediaInfoItem.fCodec = confirmCodec(mediaInfo.Get(MediaInfo.Stream_Video, 0, "Codec/CC", MediaInfo.Info_Text));
			if(!StrUtil.hasLen(mediaInfoItem.fCodec))
			{
				mediaInfoItem.fCodec = confirmCodec(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "Codec/cc", MediaInfo.Info_Text));
				if(!StrUtil.hasLen(mediaInfoItem.fCodec))
					mediaInfoItem.fCodec = confirmCodec(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "Codec", MediaInfo.Info_Text));
				if(!StrUtil.hasLen(mediaInfoItem.fCodec))
					throw new Exception(String.format("Failed to get codec for file(%s)", fileName));

				if(mediaInfoItem.fPlayTime == null)
					mediaInfoItem.fPlayTime = parseInteger(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "PlayTime", MediaInfo.Info_Text));
			}
			else
			{
				mediaInfoItem.fWidth = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, 0, "Width", MediaInfo.Info_Text));
				mediaInfoItem.fHeight = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, 0, "Height", MediaInfo.Info_Text));
				mediaInfoItem.fFrameRate = parseFloat(mediaInfo.Get(MediaInfo.Stream_Video, 0, "FrameRate", MediaInfo.Info_Text));
				mediaInfoItem.fBitRate = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, 0, "BitRate", MediaInfo.Info_Text));
				if(mediaInfoItem.fPlayTime == null)
					mediaInfoItem.fPlayTime = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, 0, "PlayTime", MediaInfo.Info_Text));
			}
		}
		finally
		{
			try { mediaInfo.Close(); } catch(Exception e) {}
			try { mediaInfo.Delete(); } catch(Exception e) {}
		}

		mediaInfoItem.print();
		return mediaInfoItem;
	}

	private static String confirmCodec(String codec)
	{
		String mappedCodec = fCodecMap.get(codec);
		if(mappedCodec == null)
			Logger.logInfo(MediaInfoItem.class, "confirmCodec", String.format("No match for codec(%s)", codec));
		return mappedCodec;
	}

	private static Integer parseInteger(String value)
	{
		if((value == null) || (value.trim().length() == 0))
			return null;

		try
		{
			int intValue = Integer.parseInt(value);
			if(intValue != 0)
				return intValue;
		}
		catch(NumberFormatException e)
		{
			Float floatValue = parseFloat(value);
			if(floatValue != null)
				return Math.round(floatValue);
		}

		return null;
	}

	private static Float parseFloat(String value)
	{
		if((value == null) || (value.trim().length() == 0))
			return null;

		try
		{
			return Float.parseFloat(value);
		}
		catch(NumberFormatException e)
		{
		}

		return null;
	}

	private void print()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(String.format("File:      %s\n", fFileName));
		sb.append(String.format("Codec:     %s\n", fCodec));
		sb.append(String.format("Width:     %d\n", fWidth));
		sb.append(String.format("Height:    %d\n", fHeight));
		sb.append(String.format("FrameRate: %f\n", fFrameRate));
		sb.append(String.format("BitRate:   %d\n", fBitRate));
		sb.append(String.format("PlayTime:  %d\n", fPlayTime));
		sb.append("\n");

		Logger.logInfo(MediaInfoItem.class, "print", sb.toString());
		//System.out.print(sb.toString());
	}
}
