package xCloud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 16:03
 * @ClassName User
 * SQL 初始化：创建 src/main/resources/schema.sql（建表）：
 * CREATE TABLE IF NOT EXISTS users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255)); 和 data.sql（插数据）：
 * INSERT INTO users (name, email) VALUES ('John', 'john@example.com');
 */
@Data
@TableName("users")  // 对应表名
public class User {
    @TableId(type = IdType.AUTO)  // 自增主键
    private Long id;

    @TableField("name")
    private String name;

    private String email;

//    @TableLogic  // 逻辑删除
//    private Integer deleted = 0;
}