--//////////////////////////////////////////////////////////////////////////////
-- Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
-- iNetVOD Confidential and Proprietary.  See LEGAL.txt.
--//////////////////////////////////////////////////////////////////////////////

use [ContMgr]
GO

--//////////////////////////////////////////////////////////////////////////////

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContMgr_ValidationQuery]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContMgr_ValidationQuery]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem_Insert]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItem_Insert]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem_Update]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItem_Update]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetBySourceURLNeedVideoCodec]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetBySourceURLNeedVideoCodec]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetByOldestStatusToDownloadOrTranscode]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetByOldestStatusToDownloadOrTranscode]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_CountTotalFileSizeForLocal]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_CountTotalFileSizeForLocal]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetBySoloLocal]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetBySoloLocal]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetBySoloLocalNoToTranscode]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetBySoloLocalNoToTranscode]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItemList_GetByLocalWasTranscoded]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[ContentItemList_GetByLocalWasTranscoded]
GO

--//////////////////////////////////////////////////////////////////////////////

SET QUOTED_IDENTIFIER ON
GO
SET ANSI_NULLS OFF
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContMgr_ValidationQuery
AS
	select count(*) from ContentItem
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItem_Insert
	@ContentItemID uniqueidentifier,
	@SourceURL varchar(892),
	@NeedVideoCodec varchar(8),
	@RequestedAt datetime,
	@Status varchar(16),
	@LocalFilePath varchar(64),
	@FileSize bigint,
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
	@LocalFilePath varchar(64),
	@FileSize bigint,
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

CREATE PROCEDURE dbo.ContentItemList_GetByOldestStatusToDownloadOrTranscode
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
	where (Status = 'ToDownload') or (Status = 'ToTranscode')
	order by RequestedAt
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE PROCEDURE dbo.ContentItemList_CountTotalFileSizeForLocal
AS
	select "TotalFileSize" = isnull(sum(FileSize),0) from ContentItem where (Status = 'Local')
GO

--//////////////////////////////////////////////////////////////////////////////

-- Find Local items that don't have any related items (no others with same SourceURL)

CREATE PROCEDURE dbo.ContentItemList_GetBySoloLocal
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
	where (Status = 'Local') and (NeedVideoCodec is null)
	and (SourceURL not in (select distinct SourceURL from ContentItem
		where (NeedVideoCodec is not null)))
	order by RequestedAt
GO

--//////////////////////////////////////////////////////////////////////////////

-- Find Local items that don't have any related items (no others with same SourceURL)
-- or no related items that need transcoding

CREATE PROCEDURE dbo.ContentItemList_GetBySoloLocalNoToTranscode
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
	where (Status = 'Local') and (NeedVideoCodec is null)
	and (SourceURL not in (select distinct SourceURL from ContentItem
		where (NeedVideoCodec is not null) and (Status = 'ToTranscode')))
	order by RequestedAt
GO

--//////////////////////////////////////////////////////////////////////////////

-- Find Local items that were previously transcoded

CREATE PROCEDURE dbo.ContentItemList_GetByLocalWasTranscoded
AS
	select ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status,
		LocalFilePath, FileSize, VideoCodec, AudioCodec, CanRelease
	from ContentItem
	where (Status = 'Local') and (NeedVideoCodec is not null)
	order by RequestedAt
GO

--//////////////////////////////////////////////////////////////////////////////

GRANT EXECUTE ON [dbo].[ContMgr_ValidationQuery] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItem_Insert] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItem_Update] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetBySourceURLNeedVideoCodec] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetByOldestStatusToDownloadOrTranscode] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_CountTotalFileSizeForLocal] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetBySoloLocal] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetBySoloLocalNoToTranscode] TO [contmgr]
GRANT EXECUTE ON [dbo].[ContentItemList_GetByLocalWasTranscoded] TO [contmgr]

--//////////////////////////////////////////////////////////////////////////////

SET QUOTED_IDENTIFIER OFF
GO
SET ANSI_NULLS ON
GO

