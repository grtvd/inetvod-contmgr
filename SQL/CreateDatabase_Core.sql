--//////////////////////////////////////////////////////////////////////////////
-- Copyright © 2007 iNetVOD, Inc. All Rights Reserved.
-- iNetVOD Confidential and Proprietary.  See LEGAL.txt.
--//////////////////////////////////////////////////////////////////////////////

use master

IF NOT EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'ContMgr')
BEGIN
print 'creating database'

CREATE DATABASE [ContMgr]  ON (NAME = N'ContMgr_Data', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL\Data\ContMgr_Data.MDF' , SIZE = 1, FILEGROWTH = 10%) LOG ON (NAME = N'ContMgr_Log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL\Data\ContMgr_Log.LDF' , SIZE = 1, FILEGROWTH = 10%)
 COLLATE SQL_Latin1_General_CP1_CI_AS

END
ELSE
print 'database not created, already existed'
GO

exec sp_dboption N'ContMgr', N'autoclose', N'true'
GO

exec sp_dboption N'ContMgr', N'bulkcopy', N'false'
GO

exec sp_dboption N'ContMgr', N'trunc. log', N'true'
GO

exec sp_dboption N'ContMgr', N'torn page detection', N'true'
GO

exec sp_dboption N'ContMgr', N'read only', N'false'
GO

exec sp_dboption N'ContMgr', N'dbo use', N'false'
GO

exec sp_dboption N'ContMgr', N'single', N'false'
GO

exec sp_dboption N'ContMgr', N'autoshrink', N'true'
GO

exec sp_dboption N'ContMgr', N'ANSI null default', N'false'
GO

exec sp_dboption N'ContMgr', N'recursive triggers', N'false'
GO

exec sp_dboption N'ContMgr', N'ANSI nulls', N'false'
GO

exec sp_dboption N'ContMgr', N'concat null yields null', N'false'
GO

exec sp_dboption N'ContMgr', N'cursor close on commit', N'false'
GO

exec sp_dboption N'ContMgr', N'default to local cursor', N'false'
GO

exec sp_dboption N'ContMgr', N'quoted identifier', N'false'
GO

exec sp_dboption N'ContMgr', N'ANSI warnings', N'false'
GO

exec sp_dboption N'ContMgr', N'auto create statistics', N'true'
GO

exec sp_dboption N'ContMgr', N'auto update statistics', N'true'
GO

if( ( (@@microsoftversion / power(2, 24) = 8) and (@@microsoftversion & 0xffff >= 724) ) or ( (@@microsoftversion / power(2, 24) = 7) and (@@microsoftversion & 0xffff >= 1082) ) )
	exec sp_dboption N'ContMgr', N'db chaining', N'false'
GO

--//////////////////////////////////////////////////////////////////////////////
--//////////////////////////////////////////////////////////////////////////////

use [ContMgr]
GO

--//////////////////////////////////////////////////////////////////////////////

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[ContentItem]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[ContentItem]
GO

--//////////////////////////////////////////////////////////////////////////////

CREATE TABLE [dbo].[ContentItem] (
	[ContentItemID] [uniqueidentifier] NOT NULL ,
	[SourceURL] [varchar] (892) NOT NULL ,
	[NeedVideoCodec] [varchar] (8) NULL ,
	[RequestedAt] [datetime] NOT NULL ,
	[Status] [varchar] (16) NOT NULL ,
	[LocalFilePath] [char] (64) NULL ,
	[FileSize] [int] NULL ,
	[VideoCodec] [varchar] (8) NULL ,
	[AudioCodec] [varchar] (8) NULL ,
	[CanRelease] [bit] NOT NULL
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ContentItem] WITH NOCHECK ADD 
	CONSTRAINT [PK_ContentItem] PRIMARY KEY  CLUSTERED 
	(
		[ContentItemID]
	)  ON [PRIMARY] 
GO

 CREATE  UNIQUE  INDEX [IX_ContentItem_SourceURL] ON [dbo].[ContentItem]([SourceURL], [NeedVideoCodec]) ON [PRIMARY]
GO



--//////////////////////////////////////////////////////////////////////////////

if exists (select * from dbo.sysusers where name = N'contmgr' and uid < 16382)
	EXEC sp_revokedbaccess  N'contmgr'
GO

if exists (select * from master.dbo.syslogins where loginname = N'contmgr')
	EXEC sp_droplogin N'contmgr'
GO

--//////////////////////////////////////////////////////////////////////////////

if not exists (select * from master.dbo.syslogins where loginname = N'contmgr')
BEGIN
	declare @logindb nvarchar(132), @loginlang nvarchar(132) select @logindb = N'ContMgr', @loginlang = N'us_english'
	if @logindb is null or not exists (select * from master.dbo.sysdatabases where name = @logindb)
		select @logindb = N'master'
	if @loginlang is null or (not exists (select * from master.dbo.syslanguages where name = @loginlang) and @loginlang <> N'us_english')
		select @loginlang = @@language
	exec sp_addlogin N'contmgr', null, @logindb, @loginlang
END
GO

--//////////////////////////////////////////////////////////////////////////////

if not exists (select * from dbo.sysusers where name = N'contmgr' and uid < 16382)
	EXEC sp_grantdbaccess N'contmgr', N'contmgr'
GO

--//////////////////////////////////////////////////////////////////////////////
--//////////////////////////////////////////////////////////////////////////////
