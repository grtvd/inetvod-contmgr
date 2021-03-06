/**
 * Copyright � 2007-2008 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.processor.mediainfo;

import java.util.HashMap;
import java.util.HashSet;

import com.inetvod.common.core.Logger;
import com.inetvod.common.core.StrUtil;
import com.inetvod.contmgr.data.AudioCodec;
import com.inetvod.contmgr.data.VideoCodec;
import net.sourceforge.mediainfo.MediaInfo;

public class MediaInfoItem
{
	/* Fields */
	private static HashMap<String, VideoCodec> fVideoCodecMap;
	private static HashSet<String> fVideoCodecIgnored;
	private static HashMap<String, AudioCodec> fAudioCodecMap;

	private String fFileName;
	private VideoCodec fVideoCodec;
	private AudioCodec fAudioCodec;
	private Integer fWidth;
	private Integer fHeight;
	private Float fFrameRate;	// frames per second
	private Integer fBitRate;	// bits per second
	private Integer fPlayTime;	// milli-seconds

	/* Getters and Setters */
	public VideoCodec getVideoCodec() { return fVideoCodec; }
	public AudioCodec getAudioCodec() { return fAudioCodec; }
	public Integer getWidth() { return fWidth; }
	public Integer getHeight() { return fHeight; }
	public Float getFrameRate() { return fFrameRate; }
	public Integer getBitRate() { return fBitRate; }
	public Integer getPlayTime() { return fPlayTime; }

	/* Construction */
	static
	{
		fVideoCodecMap = new HashMap<String, VideoCodec>();
		fVideoCodecMap.put("WMV1", VideoCodec.WMV1);	//.wmv
		fVideoCodecMap.put("WMV2", VideoCodec.WMV2);	//.wmv
		fVideoCodecMap.put("WMV3", VideoCodec.WMV3);	//.wmv
		fVideoCodecMap.put("avc1", VideoCodec.AVC1);	//.mp4
		fVideoCodecMap.put("20", VideoCodec.MP4V);		//.mp4
		fVideoCodecMap.put("SVQ3", VideoCodec.SVQ3);	//.mov
		fVideoCodecMap.put("MPEG-1V", VideoCodec.WMV2);//.wmv - not really WMV2 but it will run under it

		fVideoCodecIgnored = new HashSet<String>();
		fVideoCodecIgnored.add("jpeg");
		fVideoCodecIgnored.add("rle ");
		fVideoCodecIgnored.add("png ");
		fVideoCodecIgnored.add("On2 VP6");

		fAudioCodecMap = new HashMap<String, AudioCodec>();
		fAudioCodecMap.put("40", AudioCodec.M4A);			//.m4a
		fAudioCodecMap.put("sowt", AudioCodec.M4A);			//.m4a
		fAudioCodecMap.put("A_AAC/MPEG4/LC", AudioCodec.M4A);//.m4a
		fAudioCodecMap.put("MPA1L3", AudioCodec.MP3);		//.mp3
		fAudioCodecMap.put("MPEG-1A L3", AudioCodec.MP3);	//.mp3
		fAudioCodecMap.put("MPA2L3", AudioCodec.MP3);		//.mp3
		fAudioCodecMap.put("MPA2.5L3", AudioCodec.MP3);		//.mp3
		fAudioCodecMap.put("55", AudioCodec.MP3);			//.mp3
		fAudioCodecMap.put("161", AudioCodec.WMA2);			//.wma
		fAudioCodecMap.put("AC3", AudioCodec.AC3);			//.???
		fAudioCodecMap.put("MPA1L2", AudioCodec.WMA2);		//.??? - not really WMA2 but it will run under it
		fAudioCodecMap.put("twos", AudioCodec.TWOS);		//.??? - used with QuickTime
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

			AudioCodec audioCodec = mediaInfoItem.confirmAudioCodec(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "Codec/cc", MediaInfo.Info_Text));
			if(audioCodec == null)
				audioCodec = mediaInfoItem.confirmAudioCodec(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "Codec", MediaInfo.Info_Text));

			if(audioCodec != null)
			{
				mediaInfoItem.fAudioCodec = audioCodec;
				if(mediaInfoItem.fPlayTime == null)
					mediaInfoItem.fPlayTime = parseInteger(mediaInfo.Get(MediaInfo.Stream_Audio, 0, "PlayTime", MediaInfo.Info_Text));
			}

			VideoCodec videoCodec;
			int numSteams = mediaInfo.Count_Get(MediaInfo.Stream_Video);
			for(int stream = 0; stream < numSteams; stream++)
			{
				videoCodec = mediaInfoItem.confirmVideoCodec(mediaInfo.Get(MediaInfo.Stream_Video, stream, "Codec/CC", MediaInfo.Info_Text));
				if(videoCodec == null)
					videoCodec = mediaInfoItem.confirmVideoCodec(mediaInfo.Get(MediaInfo.Stream_Video, stream, "Codec", MediaInfo.Info_Text));
				if(videoCodec != null)
				{
					mediaInfoItem.fVideoCodec = videoCodec;
					mediaInfoItem.fWidth = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, stream, "Width", MediaInfo.Info_Text));
					mediaInfoItem.fHeight = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, stream, "Height", MediaInfo.Info_Text));
					mediaInfoItem.fFrameRate = parseFloat(mediaInfo.Get(MediaInfo.Stream_Video, stream, "FrameRate", MediaInfo.Info_Text));
					mediaInfoItem.fBitRate = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, stream, "BitRate", MediaInfo.Info_Text));
					if(mediaInfoItem.fPlayTime == null)
						mediaInfoItem.fPlayTime = parseInteger(mediaInfo.Get(MediaInfo.Stream_Video, stream, "PlayTime", MediaInfo.Info_Text));
					break;
				}
			}

			//if((videoCodec == null) && (audioCodec == null))
			//	throw new Exception(String.format("Failed to get codec for file(%s)", fileName));
		}
		finally
		{
			try { mediaInfo.Close(); } catch(Exception ignore) {}
			try { mediaInfo.Delete(); } catch(Exception ignore) {}
		}

		//mediaInfoItem.print();
		return mediaInfoItem;
	}

	private VideoCodec confirmVideoCodec(String videoCodec)
	{
		VideoCodec mappedCodec = fVideoCodecMap.get(videoCodec);
		if((mappedCodec == null) && StrUtil.hasLen(videoCodec) && !fVideoCodecIgnored.contains(videoCodec))
			Logger.logErr(MediaInfoItem.class, "confirmVideoCodec", String.format("No match for video codec(%s) for file(%s)",
				videoCodec, fFileName));
		return mappedCodec;
	}

	private AudioCodec confirmAudioCodec(String audioCodec)
	{
		AudioCodec mappedCodec = fAudioCodecMap.get(audioCodec);
		if((mappedCodec == null) && StrUtil.hasLen(audioCodec))
			Logger.logErr(MediaInfoItem.class, "confirmAudioCodec", String.format("No match for audio codec(%s) for file(%s)",
				audioCodec, fFileName));
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
		catch(NumberFormatException ignore)
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
		catch(NumberFormatException ignore)
		{
		}

		return null;
	}

//	private void print()
//	{
//		StringBuilder sb = new StringBuilder();
//		sb.append("\n");
//		sb.append(String.format("File:        %s\n", fFileName));
//		sb.append(String.format("Video Codec: %s\n", fVideoCodec));
//		sb.append(String.format("Audio Codec: %s\n", fAudioCodec));
//		sb.append(String.format("Width:       %d\n", fWidth));
//		sb.append(String.format("Height:      %d\n", fHeight));
//		sb.append(String.format("FrameRate:   %f\n", fFrameRate));
//		sb.append(String.format("BitRate:     %d\n", fBitRate));
//		sb.append(String.format("PlayTime:    %d\n", fPlayTime));
//		sb.append("\n");
//
//		Logger.logInfo(MediaInfoItem.class, "print", sb.toString());
//		//System.out.print(sb.toString());
//	}
}
