/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.dbdata;

import java.sql.Types;
import java.util.ArrayList;

import com.inetvod.common.core.StrUtil;
import com.inetvod.common.dbdata.DatabaseProcParam;

public class ContentItemList extends ArrayList<ContentItem>
{
	/* Construction */
	public static ContentItemList findBySourceURLNeedVideoCodec(String sourceURL, String needVideoCodec) throws Exception
	{
		if(!StrUtil.hasLen(sourceURL))
			throw new IllegalArgumentException("SourceURL cannot be null or empty");

		DatabaseProcParam params[] = new DatabaseProcParam[2];

		params[0] = new DatabaseProcParam(Types.VARCHAR, sourceURL);
		params[1] = new DatabaseProcParam(Types.VARCHAR, needVideoCodec);

		return ContentItem.getDatabaseAdaptor().selectManyByProc("ContentItemList_GetBySourceURLNeedVideoCodec", params);
	}


}
