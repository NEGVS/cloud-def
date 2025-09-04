-- 创建数据库
CREATE DATABASE mydb;
-- 创建新数据库CREATE DATABASE new_database
WITH OWNER = postgres
ENCODING = 'UTF8'
CONNECTION LIMIT = -1;

-- 列出所有数据库
\l
-- 列出所有数据库
SELECT datname FROM pg_database;

-- 切换数据库
\c mydb
-- 删除数据库
DROP DATABASE mydb;
dropdb 数据库名


You are using psql, the command-line interface to PostgreSQL.
Type:  \copyright for distribution terms
\h for help with SQL commands
\? for help with psql commands
\g or terminate with semicolon to execute query
\q to quit

