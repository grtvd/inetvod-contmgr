/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.data;

import java.lang.reflect.Constructor;
import java.util.UUID;

import com.inetvod.common.core.CtorUtil;
import com.inetvod.common.core.UUStringID;

public class ContentItemID extends UUStringID
{
	public static final Constructor<ContentItemID> CtorString = CtorUtil.getCtorString(ContentItemID.class);
	public static final int MaxLength = 64;

	public ContentItemID(String value)
	{
		super(value);
	}

	public static ContentItemID newInstance()
	{
		return new ContentItemID(UUID.randomUUID().toString());
	}
}
