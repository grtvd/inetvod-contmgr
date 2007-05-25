/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.data;

import com.inetvod.common.core.StrUtil;

public enum ContentItemStatus
{
	ToDownload,
	Local,
	ToTranscode,
	NotLocal;

	/* Constants */
	public static final int MaxLength = 16;

	/* Construction */
	private ContentItemStatus()
	{
	}

	public static ContentItemStatus convertFromString(String value)
	{
		if(!StrUtil.hasLen(value))
			return null;

		return valueOf(value);	// will throw exception on unknown value
	}

	public static String convertToString(ContentItemStatus value)
	{
		if(value == null)
			return null;
		return value.toString();
	}
}
