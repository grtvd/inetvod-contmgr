--//////////////////////////////////////////////////////////////////////////////
-- Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
-- iNetVOD Confidential and Proprietary.  See LEGAL.txt.
--//////////////////////////////////////////////////////////////////////////////

use [ContMgr]
GO

--//////////////////////////////////////////////////////////////////////////////

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem_Insert]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItem_Insert]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem_Update]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItem_Update]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem_GetAll]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItem_GetAll]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetBySourceURLNeedVideoCodec]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetBySourceURLNeedVideoCodec]
GO

--//////////////////////////////////////////////////////////////////////////////

SET QUOTED_IDENTIFIER ON
GO
SET ANSI_NULLS OFF
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItem_Insert
	@ContentItemID uniqueidentifier,
	@SourceURL varchar(892),
	@NeedVideoCodec varchar(8),
	@RequestedAt datetime,
	@Status varchar(16),
	@LocalFilePath char(64),
	@FileSize int,
	@VideoCodec varchar(8),
	@AudioCodec varchar(8),
	@CanRelease bit
AS
	insert into ContentItem
	(
		ContentItemID,
		SourceURL,
		NeedVideoCodec,
		RequestedAt,
		Status,
		LocalFilePath,
		FileSize,
		VideoCodec,
		AudioCodec,
		CanRelease
	)
	values
	(
		@ContentItemID,
		@SourceURL,
		@NeedVideoCodec,
		@RequestedAt,
		@Status,
		@LocalFilePath,
		@FileSize,
		@VideoCodec,
		@AudioCodec,
		@CanRelease
	)
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItem_Update
	@ContentItemID uniqueidentifier,
	@SourceURL varchar(892),
	@NeedVideoCodec varchar(8),
	@RequestedAt datetime,
	@Status varchar(16),
	@LocalFilePath char(64),
	@FileSize int,
	@VideoCodec varchar(8),
	@AudioCodec varchar(8),
	@CanRelease bit
AS
	update ContentItem set
		SourceURL = @SourceURL,
		NeedVideoCodec = @NeedVideoCodec,
		RequestedAt = @RequestedAt,
		Status = @Status,
		LocalFilePath = @LocalFilePath,
		FileSize = @FileSize,
		VideoCodec = @VideoCodec,
		AudioCodec = @AudioCodec,
		CanRelease = @CanRelease
	where ContentItemID = @ContentItemID
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItem_GetAll
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItemList_GetBySourceURLNeedVideoCodec
	@SourceURL varchar(892),
	@NeedVideoCodec varchar(8)
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
	where (SourceURL = @SourceURL) and (NeedVideoCodec = @NeedVideoCodec)
GO

--//////////////////////////////////////////////////////////////////////////////

GRANT EXECUTE ON [dbo].[ContentItem_Insert] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItem_Update] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItem_GetAll] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetBySourceURLNeedVideoCodec] TO [contmgr]

--//////////////////////////////////////////////////////////////////////////////

SET QUOTED_IDENTIFIER OFF
GO
SET ANSI_NULLS ON
GO

