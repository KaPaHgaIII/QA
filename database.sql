USE [qa]
GO
/****** Object:  Schema [m2ss]    Script Date: 15.02.2015 23:42:43 ******/
CREATE SCHEMA [m2ss]
GO
/****** Object:  StoredProcedure [dbo].[vote_message]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[vote_message]
@uid INT,
@message_id INT
AS
BEGIN
-- SET NOCOUNT ON added to prevent extra result sets from
-- interfering with SELECT statements.
SET NOCOUNT ON;
DECLARE @result INT;
DECLARE @votes INT;
IF ((SELECT COUNT(vote_id) FROM dbo.votes WHERE uid=@uid AND message_id=@message_id AND vote_type='MESSAGE')=0)
BEGIN
INSERT INTO dbo.votes (uid, vote_type, message_id) VALUES (@uid, 'MESSAGE', @message_id);
SET @result = 1;
END
ELSE
BEGIN
DELETE FROM dbo.votes
WHERE uid = @uid AND message_id = @message_id AND vote_type = 'MESSAGE';
SET @result = 0;
END
SET @votes = (SELECT votes
              FROM messages
              WHERE message_id = @message_id);
SELECT
  @result AS result,
  @votes  AS votes;
END

GO
/****** Object:  StoredProcedure [dbo].[vote_question]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[vote_question]
@uid INT,
@question_id INT,
@sign INT
AS
BEGIN
-- SET NOCOUNT ON added to prevent extra result sets from
-- interfering with SELECT statements.
SET NOCOUNT ON;
DECLARE @result INT;
DECLARE @votes INT;
IF ((SELECT COUNT(vote_id) FROM dbo.votes WHERE uid=@uid AND question_id=@question_id AND vote_type='QUESTION')=0)
BEGIN
INSERT INTO dbo.votes (uid, vote_type, question_id, sign) VALUES (@uid, 'QUESTION', @question_id, @sign);
SET @result = 1;
END
ELSE
BEGIN
DECLARE @old_sign INT;
SET @old_sign = (SELECT sign
                 FROM dbo.votes
                 WHERE uid = @uid AND question_id = @question_id AND vote_type = 'QUESTION');

IF (@old_sign = @sign)
BEGIN
DELETE FROM dbo.votes
WHERE uid = @uid AND question_id = @question_id AND vote_type = 'QUESTION';
SET @result = 0;
END
ELSE
BEGIN
UPDATE dbo.votes
SET sign = @sign
WHERE uid = @uid AND question_id = @question_id AND vote_type = 'QUESTION';
SET @result = 1;
END
END
SET @votes = (SELECT votes
              FROM questions
              WHERE question_id = @question_id);
SELECT
  @result AS result,
  @votes  AS votes;
END

GO
/****** Object:  UserDefinedFunction [dbo].[enum2str$votes$vote_type]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[enum2str$votes$vote_type]
(
@setval TINYINT
)
RETURNS nvarchar(max)
AS
BEGIN
RETURN
CASE @setval
WHEN 1 THEN 'MESSAGE'
WHEN 2 THEN 'QUESTION'
ELSE ''
END
END
GO
/****** Object:  UserDefinedFunction [dbo].[norm_enum$votes$vote_type]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[norm_enum$votes$vote_type]
(
@setval nvarchar(max)
)
RETURNS nvarchar(max)
AS
BEGIN
RETURN dbo.enum2str$votes$vote_type(dbo.str2enum$votes$vote_type(@setval))
END
GO
/****** Object:  UserDefinedFunction [dbo].[str2enum$votes$vote_type]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[str2enum$votes$vote_type]
(
@setval nvarchar(max)
)
RETURNS TINYINT
AS
BEGIN
RETURN
CASE @setval
WHEN 'MESSAGE' THEN 1
WHEN 'QUESTION' THEN 2
ELSE 0
END
END
GO
/****** Object:  Table [dbo].[messages]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[messages](
[message_id] [INT] IDENTITY(50, 1) NOT NULL,
[uid] [INT] NOT NULL,
[question_id] [INT] NOT NULL,
[number] [INT] NOT NULL,
[TEXT] [nvarchar](max) NOT NULL,
[votes] [INT] NULL,
[TIME] [DATETIME] NOT NULL,
CONSTRAINT [PK_messages_message_id] PRIMARY KEY CLUSTERED
(
[message_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[question_tags]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[question_tags](
[question_tag_id] [INT] IDENTITY(5, 1) NOT NULL,
[question_id] [INT] NOT NULL,
[tag_id] [INT] NOT NULL,
CONSTRAINT [PK_question_tags_question_tag_id] PRIMARY KEY CLUSTERED
(
[question_tag_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[questions]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[questions](
[question_id] [INT] IDENTITY(18, 1) NOT NULL,
[uid] [INT] NOT NULL,
[title] [nvarchar](255) NULL,
[TEXT] [nvarchar](max) NULL,
[votes] [INT] NULL,
[messages] [INT] NULL,
[asked_time] [DATETIME] NULL,
[updated_time] [DATETIME] NULL,
CONSTRAINT [PK_questions_question_id] PRIMARY KEY CLUSTERED
(
[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[tags]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tags](
[tag_id] [INT] IDENTITY(5, 1) NOT NULL,
[real_id] [INT] NULL,
[VALUE] [nvarchar](255) NULL,
CONSTRAINT [PK_tags_tag_id] PRIMARY KEY CLUSTERED
(
[tag_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[user_roles]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user_roles](
[role_id] [INT] IDENTITY(5, 1) NOT NULL,
[uid] [INT] NOT NULL,
[role] [nvarchar](45) NOT NULL,
CONSTRAINT [PK_user_roles_role_id] PRIMARY KEY CLUSTERED
(
[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[users]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[users](
[uid] [INT] IDENTITY(5, 1) NOT NULL,
[username] [nvarchar](45) NOT NULL,
[email] [nvarchar](255) NOT NULL,
[PASSWORD] [nvarchar](60) NULL,
[reputation] [INT] NULL,
CONSTRAINT [PK_users_uid] PRIMARY KEY CLUSTERED
(
[uid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[votes]    Script Date: 15.02.2015 23:42:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[votes](
[vote_id] [INT] IDENTITY(91, 1) NOT NULL,
[uid] [INT] NOT NULL,
[vote_type] [nvarchar](8) NOT NULL,
[message_id] [INT] NULL,
[question_id] [INT] NULL,
[sign] [INT] NULL,
CONSTRAINT [PK_votes_vote_id] PRIMARY KEY CLUSTERED
(
[vote_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
ALTER TABLE [dbo].[messages] ADD DEFAULT ((0)) FOR [votes]
GO
ALTER TABLE [dbo].[messages] ADD CONSTRAINT [DF__messages__time__2F10007B] DEFAULT (getdate()) FOR [TIME]
GO
ALTER TABLE [dbo].[questions] ADD DEFAULT (NULL) FOR [title]
GO
ALTER TABLE [dbo].[questions] ADD DEFAULT ((0)) FOR [votes]
GO
ALTER TABLE [dbo].[questions] ADD DEFAULT ((0)) FOR [messages]
GO
ALTER TABLE [dbo].[questions] ADD DEFAULT (getdate()) FOR [asked_time]
GO
ALTER TABLE [dbo].[questions] ADD DEFAULT (getdate()) FOR [updated_time]
GO
ALTER TABLE [dbo].[tags] ADD DEFAULT ((0)) FOR [real_id]
GO
ALTER TABLE [dbo].[users] ADD DEFAULT (NULL) FOR [PASSWORD]
GO
ALTER TABLE [dbo].[users] ADD DEFAULT ((0)) FOR [reputation]
GO
ALTER TABLE [dbo].[votes] ADD DEFAULT (NULL) FOR [message_id]
GO
ALTER TABLE [dbo].[votes] ADD DEFAULT (NULL) FOR [question_id]
GO
ALTER TABLE [dbo].[votes] ADD DEFAULT ((1)) FOR [sign]
GO
ALTER TABLE [dbo].[user_roles] WITH NOCHECK ADD CONSTRAINT [user_roles$fk_uid] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [user_roles$fk_uid]
GO