/**
 * Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
 * iNetVOD Confidential and Proprietary.  See LEGAL.txt.
 */
package com.inetvod.contmgr.webservice;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.inetvod.common.core.Logger;
import com.inetvod.common.core.StrUtil;
import com.inetvod.common.core.StreamUtil;
import com.inetvod.common.dbdata.DatabaseAdaptor;
import com.inetvod.contmgr.data.ContentItemStatus;
import com.inetvod.contmgr.data.VideoCodec;
import com.inetvod.contmgr.dbdata.ContentItem;

public class ContentManagerServlet extends HttpServlet
{
	/* Constants */
	private static final String GET_CONTENT_FUNC = "/gc";
	private static final String GET_CONTENT_STATS_FUNC = "/gcs";
	private static final String SOURCE_URL_PARAM = "u";
	private static final String NEED_VIDEO_CODEC_PARAM = "v";

	/* Fields */
	private static File fContentDir;

	/* Implementation */
	public void init() throws ServletException
	{
		try
		{
			// set the log file
			Logger.initialize(getServletContext().getRealPath("/log4j.xml"),
				getServletContext().getInitParameter("logdir"));

			// setup db connection
			DatabaseAdaptor.setDBConnectFile(getServletContext().getInitParameter("dbconnect"));

			// store the content dir
			fContentDir = new File(getServletContext().getInitParameter("contentDir"));

			// prime UUID, first hit is big
			UUID.randomUUID();
		}
		catch(Exception e)
		{
			throw new ServletException(e.getMessage(), e);
		}

		// Preload DatabaseAdaptors
		ContentItem.getDatabaseAdaptor();
	}

	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException
	{
		try
		{
			String function = httpServletRequest.getPathInfo();
			if(!StrUtil.hasLen(function) ||
				(!GET_CONTENT_FUNC.equals(function) && !GET_CONTENT_STATS_FUNC.equals(function)))
			{
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			String sourceURL = httpServletRequest.getParameter(SOURCE_URL_PARAM);
			if(!StrUtil.hasLen(sourceURL))
			{
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			VideoCodec needVideoCodec;
			try
			{
				needVideoCodec = VideoCodec.convertFromString(httpServletRequest.getParameter(NEED_VIDEO_CODEC_PARAM));
			}
			catch(Exception e)
			{
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}


			ContentItem contentItem = getContentItem(sourceURL, needVideoCodec);

			if(GET_CONTENT_FUNC.equals(function))
			{
				fullfillGetContentRequest(httpServletResponse, contentItem);
			}
			else if(GET_CONTENT_STATS_FUNC.equals(function))
			{
				//TODO return the file stats
				httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
				//TODO return;
			}
		}
		catch(Exception e)
		{
			httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			Logger.logErr(this, "doGet", e);
		}
	}

	private static ContentItem getContentItem(String sourceURL, VideoCodec needVideoCodec) throws Exception
	{
		ContentItem contentItem = ContentItem.getCreate(sourceURL, needVideoCodec);

		if(contentItem.getNeedVideoCodec() == null)
		{
			if(ContentItemStatus.NotLocal.equals(contentItem.getStatus()))
				contentItem.setStatus(ContentItemStatus.ToDownload);
		}
		else
		{
			ContentItem sourceContentItem = ContentItem.getCreate(sourceURL, null);
			if(ContentItemStatus.NotLocal.equals(sourceContentItem.getStatus()))
				sourceContentItem.setStatus(ContentItemStatus.ToDownload);
			sourceContentItem.setRequestedAt();
			sourceContentItem.update();

			if(ContentItemStatus.NotLocal.equals(contentItem.getStatus()))
				contentItem.setStatus(ContentItemStatus.ToTranscode);
		}

		contentItem.setRequestedAt();
		contentItem.update();

		return contentItem;
	}

	private void fullfillGetContentRequest(HttpServletResponse httpServletResponse, ContentItem contentItem)
		throws Exception
	{
		if(!ContentItemStatus.Local.equals(contentItem.getStatus()))
		{
			httpServletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}

		File file = new File(fContentDir, contentItem.getLocalFilePath());
		Logger.logInfo(this, "", String.format("Streaming %s", file.getAbsolutePath()));

		String fileName = file.getName();
		httpServletResponse.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");

		if (contentItem.getFileSize() <= Integer.MAX_VALUE)
			httpServletResponse.setContentLength(contentItem.getFileSize().intValue());
		else
			Logger.logWarn(this, "", String.format("'%s' is %d bytes and is larger than Integer.MAX_VALUE",
				file.getAbsoluteFile(), contentItem.getFileSize()));

		//TODO httpServletResponse.setHeader("Transfer-Encoding", "chunked");
		httpServletResponse.setContentType("application/octet-stream");		//TODO send correct mime

		ServletOutputStream out = httpServletResponse.getOutputStream();
		StreamUtil.streamFile(file, out);
	}
}
