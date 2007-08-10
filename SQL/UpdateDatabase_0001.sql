--//////////////////////////////////////////////////////////////////////////////
-- Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
-- iNetVOD Confidential and Proprietary.  See LEGAL.txt.
--//////////////////////////////////////////////////////////////////////////////

use [ContMgr]
GO

--//////////////////////////////////////////////////////////////////////////////

BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT

--//////////////////////////////////////////////////////////////////////////////

BEGIN TRANSACTION

CREATE TABLE dbo.Tmp_ContentItem
	(
	ContentItemID uniqueidentifier NOT NULL,
	SourceURL varchar(892) NOT NULL,
	NeedVideoCodec varchar(8) NULL,
	RequestedAt datetime NOT NULL,
	Status varchar(16) NOT NULL,
	RetryCount smallint NOT NULL,
	LocalFilePath varchar(64) NULL,
	FileSize bigint NULL,
	MediaMIME varchar(32) NULL,
	VideoCodec varchar(8) NULL,
	AudioCodec varchar(8) NULL,
	HorzResolution smallint NULL,
	VertResolution smallint NULL,
	FramesPerSecond smallint NULL,
	BitRate smallint NULL,
	RunningTimeSecs int NULL,
	CanRelease bit NOT NULL
	)  ON [PRIMARY]
GO
IF EXISTS(SELECT * FROM dbo.ContentItem)
	 EXEC('INSERT INTO dbo.Tmp_ContentItem (ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status, RetryCount, LocalFilePath, FileSize, VideoCodec, AudioCodec, HorzResolution, VertResolution, FramesPerSecond, BitRate, RunningTimeSecs, CanRelease)
		SELECT ContentItemID, SourceURL, NeedVideoCodec, RequestedAt, Status, RetryCount, LocalFilePath, FileSize, VideoCodec, AudioCodec, HorzResolution, VertResolution, FramesPerSecond, BitRate, RunningTimeSecs, CanRelease FROM dbo.ContentItem TABLOCKX')
GO
DROP TABLE dbo.ContentItem
GO
EXECUTE sp_rename N'dbo.Tmp_ContentItem', N'ContentItem', 'OBJECT'
GO
ALTER TABLE dbo.ContentItem ADD CONSTRAINT
	PK_ContentItem PRIMARY KEY CLUSTERED 
	(
	ContentItemID
	) ON [PRIMARY]

GO
CREATE UNIQUE NONCLUSTERED INDEX IX_ContentItem_SourceURL ON dbo.ContentItem
	(
	SourceURL,
	NeedVideoCodec
	) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX IX_ContentItem_NeedVideoCodec ON dbo.ContentItem
	(
	NeedVideoCodec
	) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX IX_ContentItem_StatusRequestedAt ON dbo.ContentItem
	(
	Status,
	RequestedAt
	) ON [PRIMARY]
GO

COMMIT

--//////////////////////////////////////////////////////////////////////////////

