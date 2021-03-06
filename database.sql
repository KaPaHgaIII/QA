USE [master]
GO
/****** Object:  Database [qa]    Script Date: 25.12.2015 0:00:21 ******/
CREATE DATABASE [qa]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'qa', FILENAME = N'C:\Program Files (x86)\Microsoft SQL Server\MSSQL12.MSSQLSERVER\MSSQL\DATA\qa.mdf' , SIZE = 41600KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB )
 LOG ON 
( NAME = N'qa_log', FILENAME = N'C:\Program Files (x86)\Microsoft SQL Server\MSSQL12.MSSQLSERVER\MSSQL\DATA\qa_log.ldf' , SIZE = 1088KB , MAXSIZE = 2048GB , FILEGROWTH = 10%)
GO
ALTER DATABASE [qa] SET COMPATIBILITY_LEVEL = 110
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [qa].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [qa] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [qa] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [qa] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [qa] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [qa] SET ARITHABORT OFF 
GO
ALTER DATABASE [qa] SET AUTO_CLOSE ON 
GO
ALTER DATABASE [qa] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [qa] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [qa] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [qa] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [qa] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [qa] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [qa] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [qa] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [qa] SET  ENABLE_BROKER 
GO
ALTER DATABASE [qa] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [qa] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [qa] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [qa] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [qa] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [qa] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [qa] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [qa] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [qa] SET  MULTI_USER 
GO
ALTER DATABASE [qa] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [qa] SET DB_CHAINING OFF 
GO
ALTER DATABASE [qa] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [qa] SET TARGET_RECOVERY_TIME = 0 SECONDS 
GO
ALTER DATABASE [qa] SET DELAYED_DURABILITY = DISABLED 
GO
EXEC sys.sp_db_vardecimal_storage_format N'qa', N'ON'
GO
USE [qa]
GO
/****** Object:  User [qa_user]    Script Date: 25.12.2015 0:00:21 ******/
CREATE USER [qa_user] FOR LOGIN [qa_user] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  Schema [m2ss]    Script Date: 25.12.2015 0:00:21 ******/
CREATE SCHEMA [m2ss]
GO
/****** Object:  UserDefinedFunction [dbo].[enum2str$votes$vote_type]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[enum2str$votes$vote_type] 
( 
   @setval tinyint
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
/****** Object:  UserDefinedFunction [dbo].[norm_enum$votes$vote_type]    Script Date: 25.12.2015 0:00:21 ******/
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
/****** Object:  UserDefinedFunction [dbo].[str2enum$votes$vote_type]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[str2enum$votes$vote_type] 
( 
   @setval nvarchar(max)
)
RETURNS tinyint
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
/****** Object:  Table [dbo].[favourite_questions]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[favourite_questions](
	[question_id] [int] NOT NULL,
	[uid] [int] NOT NULL,
 CONSTRAINT [PK_favourite_questions] PRIMARY KEY CLUSTERED 
(
	[question_id] ASC,
	[uid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[interesting_tags]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[interesting_tags](
	[uid] [int] NOT NULL,
	[tag_id] [int] NOT NULL,
 CONSTRAINT [PK_interesting_tags] PRIMARY KEY CLUSTERED 
(
	[uid] ASC,
	[tag_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[messages]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[messages](
	[message_id] [int] IDENTITY(50,1) NOT NULL,
	[uid] [int] NOT NULL,
	[question_id] [int] NOT NULL,
	[addressee] [int] NULL,
	[number] [int] NOT NULL,
	[text] [nvarchar](max) NOT NULL,
	[votes] [int] NULL CONSTRAINT [DF__messages__votes__2E1BDC42]  DEFAULT ((0)),
	[time] [datetime] NOT NULL CONSTRAINT [DF__messages__time__2F10007B]  DEFAULT (getdate()),
 CONSTRAINT [PK_messages_message_id] PRIMARY KEY CLUSTERED 
(
	[message_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[question_tags]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[question_tags](
	[question_id] [int] NOT NULL,
	[tag_id] [int] NOT NULL,
 CONSTRAINT [PK_question_tags] PRIMARY KEY CLUSTERED 
(
	[question_id] ASC,
	[tag_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[questions]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[questions](
	[question_id] [int] IDENTITY(18,1) NOT NULL,
	[uid] [int] NOT NULL,
	[title] [nvarchar](255) NOT NULL CONSTRAINT [DF__questions__title__300424B4]  DEFAULT (NULL),
	[text] [text] NOT NULL,
	[votes] [int] NOT NULL CONSTRAINT [DF__questions__votes__30F848ED]  DEFAULT ((0)),
	[messages] [int] NOT NULL CONSTRAINT [DF__questions__messa__31EC6D26]  DEFAULT ((0)),
	[asked_time] [datetime] NOT NULL CONSTRAINT [DF__questions__asked__32E0915F]  DEFAULT (getdate()),
	[updated_time] [datetime] NOT NULL CONSTRAINT [DF__questions__updat__33D4B598]  DEFAULT (getdate()),
 CONSTRAINT [PK_questions_question_id] PRIMARY KEY CLUSTERED 
(
	[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[restore_password]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[restore_password](
	[uid] [int] NOT NULL,
	[hash] [nchar](62) NOT NULL,
	[time] [datetime] NOT NULL,
 CONSTRAINT [IX_restore_password_uid] UNIQUE NONCLUSTERED 
(
	[uid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[tags]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tags](
	[tag_id] [int] IDENTITY(5,1) NOT NULL,
	[real_id] [int] NULL,
	[name] [nvarchar](255) NOT NULL,
	[usage] [int] NOT NULL CONSTRAINT [DF_tags_usage]  DEFAULT ((0)),
 CONSTRAINT [PK_tags_tag_id] PRIMARY KEY CLUSTERED 
(
	[tag_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [tags_unique_name] UNIQUE NONCLUSTERED 
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[user_roles]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user_roles](
	[role_id] [int] IDENTITY(5,1) NOT NULL,
	[uid] [int] NOT NULL,
	[role] [nvarchar](45) NOT NULL,
 CONSTRAINT [PK_user_roles_role_id] PRIMARY KEY CLUSTERED 
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[users]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[users](
	[uid] [int] IDENTITY(5,1) NOT NULL,
	[username] [nvarchar](45) NOT NULL,
	[email] [nvarchar](255) NULL,
	[password] [nvarchar](60) NULL DEFAULT (NULL),
	[reputation] [int] NULL DEFAULT ((0)),
	[vk_uid] [int] NULL,
 CONSTRAINT [PK_users_uid] PRIMARY KEY CLUSTERED 
(
	[uid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[votes]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[votes](
	[vote_id] [int] IDENTITY(91,1) NOT NULL,
	[uid] [int] NOT NULL,
	[vote_type] [nvarchar](8) NOT NULL,
	[message_id] [int] NULL DEFAULT (NULL),
	[question_id] [int] NULL DEFAULT (NULL),
	[sign] [int] NULL DEFAULT ((1)),
 CONSTRAINT [PK_votes_vote_id] PRIMARY KEY CLUSTERED 
(
	[vote_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Index [fk_uid_idx]    Script Date: 25.12.2015 0:00:21 ******/
CREATE NONCLUSTERED INDEX [fk_uid_idx] ON [dbo].[user_roles]
(
	[uid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER INDEX [fk_uid_idx] ON [dbo].[user_roles] DISABLE
GO
ALTER TABLE [dbo].[restore_password] ADD  CONSTRAINT [DF_restore_password_time]  DEFAULT (getdate()) FOR [time]
GO
ALTER TABLE [dbo].[favourite_questions]  WITH CHECK ADD  CONSTRAINT [FK_favourite_questions_questions] FOREIGN KEY([question_id])
REFERENCES [dbo].[questions] ([question_id])
GO
ALTER TABLE [dbo].[favourite_questions] CHECK CONSTRAINT [FK_favourite_questions_questions]
GO
ALTER TABLE [dbo].[favourite_questions]  WITH CHECK ADD  CONSTRAINT [FK_favourite_questions_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[favourite_questions] CHECK CONSTRAINT [FK_favourite_questions_users]
GO
ALTER TABLE [dbo].[interesting_tags]  WITH CHECK ADD  CONSTRAINT [FK_interesting_tags_tags] FOREIGN KEY([tag_id])
REFERENCES [dbo].[tags] ([tag_id])
GO
ALTER TABLE [dbo].[interesting_tags] CHECK CONSTRAINT [FK_interesting_tags_tags]
GO
ALTER TABLE [dbo].[interesting_tags]  WITH CHECK ADD  CONSTRAINT [FK_interesting_tags_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[interesting_tags] CHECK CONSTRAINT [FK_interesting_tags_users]
GO
ALTER TABLE [dbo].[messages]  WITH CHECK ADD  CONSTRAINT [FK_messages_questions] FOREIGN KEY([question_id])
REFERENCES [dbo].[questions] ([question_id])
GO
ALTER TABLE [dbo].[messages] CHECK CONSTRAINT [FK_messages_questions]
GO
ALTER TABLE [dbo].[messages]  WITH CHECK ADD  CONSTRAINT [FK_messages_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[messages] CHECK CONSTRAINT [FK_messages_users]
GO
ALTER TABLE [dbo].[question_tags]  WITH CHECK ADD  CONSTRAINT [FK_question_tags_questions] FOREIGN KEY([question_id])
REFERENCES [dbo].[questions] ([question_id])
GO
ALTER TABLE [dbo].[question_tags] CHECK CONSTRAINT [FK_question_tags_questions]
GO
ALTER TABLE [dbo].[question_tags]  WITH CHECK ADD  CONSTRAINT [FK_question_tags_tags] FOREIGN KEY([tag_id])
REFERENCES [dbo].[tags] ([tag_id])
GO
ALTER TABLE [dbo].[question_tags] CHECK CONSTRAINT [FK_question_tags_tags]
GO
ALTER TABLE [dbo].[questions]  WITH CHECK ADD  CONSTRAINT [FK_questions_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[questions] CHECK CONSTRAINT [FK_questions_users]
GO
ALTER TABLE [dbo].[restore_password]  WITH CHECK ADD  CONSTRAINT [FK_restore_password_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[restore_password] CHECK CONSTRAINT [FK_restore_password_users]
GO
ALTER TABLE [dbo].[tags]  WITH CHECK ADD  CONSTRAINT [FK_tags_tags] FOREIGN KEY([real_id])
REFERENCES [dbo].[tags] ([tag_id])
GO
ALTER TABLE [dbo].[tags] CHECK CONSTRAINT [FK_tags_tags]
GO
ALTER TABLE [dbo].[user_roles]  WITH CHECK ADD  CONSTRAINT [user_roles$fk_uid] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [user_roles$fk_uid]
GO
ALTER TABLE [dbo].[votes]  WITH CHECK ADD  CONSTRAINT [FK_votes_messages] FOREIGN KEY([message_id])
REFERENCES [dbo].[messages] ([message_id])
GO
ALTER TABLE [dbo].[votes] CHECK CONSTRAINT [FK_votes_messages]
GO
ALTER TABLE [dbo].[votes]  WITH CHECK ADD  CONSTRAINT [FK_votes_questions] FOREIGN KEY([question_id])
REFERENCES [dbo].[questions] ([question_id])
GO
ALTER TABLE [dbo].[votes] CHECK CONSTRAINT [FK_votes_questions]
GO
ALTER TABLE [dbo].[votes]  WITH CHECK ADD  CONSTRAINT [FK_votes_users] FOREIGN KEY([uid])
REFERENCES [dbo].[users] ([uid])
GO
ALTER TABLE [dbo].[votes] CHECK CONSTRAINT [FK_votes_users]
GO
/****** Object:  StoredProcedure [dbo].[select_notifications]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[select_notifications]
	@uid int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	DECLARE @now datetime;
	SET @now = GETDATE();
	
	SELECT title as n_title, CONVERT(nvarchar(10), question_id) as param, updated_time as n_time, 'new_message' as n_type
	FROM questions 
	WHERE question_id IN (
		SELECT question_id 
		FROM favourite_questions 
		WHERE uid=@uid
	) AND DATEADD(dd,1,updated_time) >= @now

	UNION

	SELECT q.title as n_title, CONVERT(nvarchar(10), m.question_id) as param, m.n_time, 'addressed_message' as n_type
	FROM (
	SELECT question_id as question_id, MAX(time) as n_time
	FROM messages
	WHERE addressee=@uid AND DATEADD(dd,1,time) >= @now
	GROUP BY question_id
	) m
	LEFT JOIN questions q ON m.question_id=q.question_id

	UNION

	SELECT title as n_title, CONVERT(nvarchar(10), question_id) as param, asked_time as n_time, 'new_question' as n_type
	FROM questions 
	WHERE question_id IN (
		SELECT DISTINCT question_id
		FROM question_tags
		WHERE tag_id IN (
			SELECT tag_id 
			FROM interesting_tags
			WHERE uid=@uid
		)
	) AND DATEADD(dd,1,asked_time) >= @now
	
	ORDER BY n_time
END


GO
/****** Object:  StoredProcedure [dbo].[vote_message]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[vote_message]
	@uid int,
	@message_id int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	DECLARE @result int;
	DECLARE @votes int;
	if ((SELECT COUNT(vote_id) FROM dbo.votes WHERE uid=@uid AND message_id=@message_id AND vote_type='MESSAGE')=0)
	BEGIN
		INSERT INTO dbo.votes (uid,vote_type,message_id) VALUES (@uid,'MESSAGE',@message_id);
		SET @result = 1;
	END
	ELSE 
	BEGIN
		DELETE FROM dbo.votes WHERE uid=@uid AND message_id=@message_id AND vote_type='MESSAGE';
		SET @result = 0;
	END
	SET @votes = (SELECT votes FROM messages WHERE message_id=@message_id);
	SELECT @result as result, @votes as votes;
END


GO
/****** Object:  StoredProcedure [dbo].[vote_question]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[vote_question]
	@uid int,
	@question_id int,
	@sign int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	DECLARE @result int;
	DECLARE @votes int;
	if ((SELECT COUNT(vote_id) FROM dbo.votes WHERE uid=@uid AND question_id=@question_id AND vote_type='QUESTION')=0)
	BEGIN
		INSERT INTO dbo.votes (uid,vote_type,question_id,sign) VALUES (@uid,'QUESTION',@question_id,@sign);
		SET @result = 1;
	END
	ELSE 
	BEGIN
		DECLARE @old_sign int;
		SET @old_sign = (SELECT sign FROM dbo.votes WHERE uid=@uid AND question_id=@question_id AND vote_type='QUESTION');

		if (@old_sign = @sign)
		BEGIN
			DELETE FROM dbo.votes WHERE uid=@uid AND question_id=@question_id AND vote_type='QUESTION';
			SET @result = 0;
		END
		ELSE
		BEGIN
			UPDATE dbo.votes SET sign=@sign WHERE uid=@uid AND question_id=@question_id AND vote_type='QUESTION';
			SET @result = 1;
		END
	END
	SET @votes = (SELECT votes FROM questions WHERE question_id=@question_id);
	SELECT @result as result, @votes as votes;
END


GO
/****** Object:  Trigger [dbo].[messages_insert]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[messages_insert]
   ON  [dbo].[messages]
   AFTER INSERT
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	DECLARE @added_count int;
	SET @added_count = (SELECT COUNT(message_id) FROM inserted);
	DECLARE @question_id int;
	SET @question_id = (SELECT question_id FROM inserted);

	UPDATE questions SET messages = messages + @added_count, updated_time = GETDATE() WHERE question_id = @question_id;

END

GO
/****** Object:  Trigger [dbo].[question_tag_insert]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[question_tag_insert]
   ON  [dbo].[question_tags]
   AFTER INSERT
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	UPDATE t
	SET t.usage = t.usage + ins.c
	FROM tags t
	JOIN (
	SELECT COUNT(tag_id) c, tag_id FROM inserted GROUP BY tag_id
	) ins ON t.tag_id = ins.tag_id
	

END

GO
/****** Object:  Trigger [dbo].[questions_insert]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TRIGGER [dbo].[questions_insert]
   ON  [dbo].[questions]
   AFTER INSERT
AS 
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	
	DECLARE @question_id int;
	SET @question_id = (SELECT question_id FROM inserted);
	DECLARE @uid int;
	SET @uid = (SELECT uid FROM inserted);

	INSERT INTO dbo.favourite_questions (question_id, uid) VALUES (@question_id, @uid);

END

GO
/****** Object:  Trigger [dbo].[vote_delete]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TRIGGER [dbo].[vote_delete]
   ON  [dbo].[votes]
   AFTER DELETE
AS 
BEGIN
	SET NOCOUNT ON;

	DECLARE @message_id int;
	SET @message_id = (SELECT message_id FROM deleted);
	DECLARE @question_id int;
	SET @question_id = (SELECT question_id FROM deleted);
	DECLARE @uid int;

    IF @message_id IS NOT NULL
	BEGIN
		SET @uid = (SELECT uid FROM messages WHERE message_id = @message_id)
		UPDATE dbo.messages SET votes = votes - 1 WHERE message_id = @message_id;
		UPDATE dbo.users SET reputation = reputation - 1 WHERE uid = @uid;
	END

	
    IF @question_id IS NOT NULL
	BEGIN
		DECLARE @deleted_sign int;
		SET @deleted_sign = (SELECT sign from deleted);
		SET @uid = (SELECT uid FROM questions WHERE question_id = @question_id)
		UPDATE dbo.questions SET votes = votes - @deleted_sign WHERE question_id = @question_id;
		UPDATE dbo.users SET reputation = reputation - 10 * @deleted_sign WHERE uid = @uid;
	END

END

GO
/****** Object:  Trigger [dbo].[vote_insert]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TRIGGER [dbo].[vote_insert]
   ON  [dbo].[votes]
   AFTER INSERT
AS 
BEGIN
	SET NOCOUNT ON;
	
	DECLARE @message_id int;
	SET @message_id = (SELECT message_id FROM inserted);
	DECLARE @question_id int;
	SET @question_id = (SELECT question_id FROM inserted);
	DECLARE @uid int;

    IF @message_id IS NOT NULL
	BEGIN
		SET @uid = (SELECT uid FROM messages WHERE message_id = @message_id)
		UPDATE dbo.messages SET votes = votes + 1 WHERE message_id = @message_id;
		UPDATE dbo.users SET reputation = reputation + 1 WHERE uid = @uid;
	END

    IF @question_id IS NOT NULL
	BEGIN
		DECLARE @inserted_sign int;
		SET @inserted_sign = (SELECT sign from inserted);
		SET @uid = (SELECT uid FROM questions WHERE question_id = @question_id)
		UPDATE dbo.questions SET votes = votes + @inserted_sign WHERE question_id = @question_id;
		UPDATE dbo.users SET reputation = reputation + 10 * @inserted_sign WHERE uid = @uid;
	END


END

GO
/****** Object:  Trigger [dbo].[vote_update]    Script Date: 25.12.2015 0:00:21 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TRIGGER [dbo].[vote_update]
   ON  [dbo].[votes]
   AFTER UPDATE
AS 
BEGIN
	SET NOCOUNT ON;

	DECLARE @question_id int;
	SET @question_id = (SELECT question_id FROM deleted);
	DECLARE @inserted_sign int, @deleted_sign int;
	SET @inserted_sign = (SELECT sign from inserted);
	SET @deleted_sign = (SELECT sign from deleted);

    IF @question_id IS NOT NULL
	BEGIN
	  UPDATE dbo.questions SET votes = votes - @deleted_sign + @inserted_sign WHERE question_id = @question_id;
	  UPDATE users SET reputation = reputation + (@inserted_sign - @deleted_sign) * 10 
	  WHERE uid = (SELECT uid FROM questions WHERE question_id = @question_id);
	END

END

GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.votes' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'FUNCTION',@level1name=N'enum2str$votes$vote_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.votes' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'FUNCTION',@level1name=N'norm_enum$votes$vote_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.votes' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'FUNCTION',@level1name=N'str2enum$votes$vote_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.messages' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'messages'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.question_tags' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'question_tags'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.questions' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'questions'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.tags' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'tags'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.user_roles' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'user_roles'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.users' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'users'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'qa.votes' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'votes'
GO
USE [master]
GO
ALTER DATABASE [qa] SET  READ_WRITE 
GO
