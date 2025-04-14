ALTER TABLE x_merchants ADD COLUMN deleted int DEFAULT 1 comment '是否删除' AFTER created_time;

ALTER TABLE table_name DROP COLUMN column_name;