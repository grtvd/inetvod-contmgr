/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.dbdata;

import java.sql.Types;
import java.util.ArrayList;

import com.inetvod.common.core.StrUtil;
import com.inetvod.common.dbdata.DatabaseProcParam;
import com.inetvod.contmgr.data.ContentItemStatus;
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

	public static ContentItemList findByOldestStatus(ContentItemStatus contentItemStatus) throws Exception
	{
		if(contentItemStatus == null)
			throw new IllegalArgumentException("contentItemStatus cannot be null");

		DatabaseProcParam params[] = new DatabaseProcParam[1];

		params[0] = new DatabaseProcParam(Types.VARCHAR, ContentItemStatus.convertToString(contentItemStatus));

		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetByOldestStatus", params);
	}

	public static ContentItemList findByStatusToTranscode() throws Exception
	{
		return findByOldestStatus(ContentItemStatus.ToTranscode);
	}

	public static ContentItemList findByStatusToDownload() throws Exception
	{
		return findByOldestStatus(ContentItemStatus.ToDownload);
	}
}
