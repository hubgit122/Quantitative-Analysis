USE [Stock]
;
SET ANSI_NULLS ON
;
SET QUOTED_IDENTIFIER ON
;
CREATE TABLE [dbo].[?](
	[日期] [int] NOT NULL,
	[开盘价] [float] NOT NULL,
	[最高价] [float] NOT NULL,
	[收盘价] [float] NOT NULL,
	[最低价] [float] NOT NULL,
	[成交量] [float] NOT NULL,
	[成交额] [float] NOT NULL,
	[复权因子] [float] NOT NULL,
 CONSTRAINT [PK_?] PRIMARY KEY CLUSTERED 
(
	[日期] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]