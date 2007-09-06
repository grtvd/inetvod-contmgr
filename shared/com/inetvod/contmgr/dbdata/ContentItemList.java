/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.dbdata;

import java.sql.Types;
import java.util.ArrayList;

import com.inetvod.common.core.StrUtil;
import com.inetvod.common.dbdata.DatabaseProcParam;
import com.inetvod.contmgr.data.VideoCodec;

public class ContentItemList extends ArrayList<ContentItem>
{
	/* Construction */
	public static ContentItemList findBySourceURLNeedVideoCodec(String sourceURL, VideoCodec needVideoCodec) throws Exception
	{
		if(!StrUtil.hasLen(sourceURL))
			throw new IllegalArgumentException("sourceURL cannot be null or empty");

		DatabaseProcParam params[] = new DatabaseProcParam[2];

		params[0] = new DatabaseProcParam(Types.VARCHAR, sourceURL);
		params[1] = new DatabaseProcParam(Types.VARCHAR, VideoCodec.convertToString(needVideoCodec));

		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetBySourceURLNeedVideoCodec", params);
	}

	public static ContentItemList findByStatusToDownloadOrTranscode() throws Exception
	{
		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetByOldestStatusToDownloadOrTranscode", null);
	}

	public static long countTotalFileSizeForLocal() throws Exception
	{
		Object totalFileSize = ContentItem.getDatabaseAdaptor().executeProcWithReturn("ContentItemList_CountTotalFileSizeForLocal", null);
		if(totalFileSize instanceof Long)
			return (Long)totalFileSize;
		throw new Exception("Bad return value");
	}

	/**
	 * Find Local items that don't have any related items (no others with same SourceURL).
	 */
	public static ContentItemList findBySoloLocal() throws Exception
	{
		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetBySoloLocal", null);
	}

	/**
	 * Find Local items that don't have any related items (no others with same SourceURL) or no related items that
	 * need transcoding.
	 */
	public static ContentItemList findBySoloLocalNoToTranscode() throws Exception
	{
		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetBySoloLocalNoToTranscode", null);
	}

	/**
	 * Find Local items that were previously transcoded.
	 */
	public static ContentItemList findByLocalWasTranscoded() throws Exception
	{
		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetByLocalWasTranscoded", null);
	}

	/**
	 * Find Error items.
	 */
	public static ContentItemList findByError() throws Exception
	{
		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetByError", null);
	}
}
