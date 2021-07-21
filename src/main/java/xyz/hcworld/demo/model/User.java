package xyz.hcworld.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: User
 * @Author: 张红尘
 * @Date: 2021-07-20
 * @Version： 1.0
 */
@Data
@TableName("t_user")
public class User {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String nickname;

    private String password;

    private Integer sex;

    private String birthday;
}