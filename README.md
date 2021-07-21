# 一.前言

这是一个基于SpringBoot整合Sharding-JDBC实现读写分离的极简教程，笔者使用到的技术及版本如下：

SpringBoot 2.5.2

MyBatis-Plus 3.4.3

Sharding-JDBC 4.1.1

MySQL8集群（看笔者前一篇文章有[部署教程](https://www.hcworld.xyz/articles/2021/07/18/1626623984678.html)）

# 二.项目目录结构

![image.png](https://b3logfile.com/file/2021/07/image-efb9ddb1.png)

# 三.pom文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.2</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>xyz.hcworld</groupId>
    <artifactId>sharding-jdbc-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>sharding-jdbc-demo</name>
    <description>多数据源切换实例</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--   mybatis-plus依赖     -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.3</version>
        </dependency>
        <!--    mysql驱动    -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!--  sharding-jdbc（多数据源切换）     -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.1.1</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

四.配置文件（基于YAML）及SQL建表语句

```YAML
spring:
  main:
    allow-bean-definition-overriding: true
  #显示sql
  shardingsphere:
    props:
      sql:
        show: true
    masterslave:
      #配置主从名称
      name: ms
      #置主库master,负责数据的写入
      master-data-source-name: ds1
      #配置从库slave节点
      slave-data-source-names: ds2,ds3
      #配置slave节点的负载均衡均衡策略,采用轮询机制，有两种算法：round_robin(轮询)和random(随机)
      load-balance-algorithm-type: round_robin
    sharding:
      #配置默认数据源ds1 默认数据源,主要用于写
      default-data-source-name: ds1
    # 配置数据源
    datasource:
      names: ds1,ds2,ds3
      #master-ds1数据库连接信息
      ds1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://192.168.2.142:3307/sharding-jdbc-db?useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456
        maxPoolSize: 100
        minPoolSize: 5
      #slave-ds2数据库连接信息
      ds2:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://192.168.2.142:3308/sharding-jdbc-db?useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456
        maxPoolSize: 100
        minPoolSize: 5
      #slave-ds3数据库连接信息
      ds3:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://192.168.2.142:3309/sharding-jdbc-db?useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456
        maxPoolSize: 100
        minPoolSize: 5
#mybatis-plus配置
mybatis-plus:
  type-aliases-package: xyz.hcworld.demo.model
  mapper-locations: classpath*:/mapper/**Mapper.xml
```

```sql
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `birthday` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
```

# 五.Mapper.xml文件及Mapper接口

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="xyz.hcworld.demo.mapper.UserMapper">

    <update id="addUser">
        INSERT INTO t_user(nickname,PASSWORD,sex,birthday) VALUES(#{nickname},#{password},#{sex},#{birthday})
    </update>

    <select id="findUsers" resultType="xyz.hcworld.demo.model.User">
        SELECT
            id,
            nickname,
            PASSWORD,
            sex,
            birthday
        FROM t_user;
    </select>

</mapper>
```

```java
package xyz.hcworld.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import xyz.hcworld.demo.model.User;

import java.util.List;

/**
 * @ClassName: UserMapper
 * @Author: 张红尘
 * @Date: 2021-07-20
 * @Version： 1.0
 */
@Component
public interface UserMapper  extends BaseMapper<User> {


    void addUser(User user);


    List<User> findUsers();
}
```

# 六 .Controller及Mocel文件

```
package xyz.hcworld.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.hcworld.demo.mapper.UserMapper;
import xyz.hcworld.demo.model.User;

import java.security.SecureRandom;
import java.util.List;

/**
 * @ClassName: UserController
 * @Author: 张红尘
 * @Date: 2021-07-20
 * @Version： 1.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/save")
    public String addUser() {
        User user = new User();
        user.setNickname("zhangsan" + new SecureRandom().nextInt());
        user.setPassword("123456");
        user.setSex(1);
        user.setBirthday("1997-12-03");
        userMapper.addUser(user);
        return user.toString();
    }

    @GetMapping("/findUsers")
    public List<User> findUsers() {
        return userMapper.findUsers();
    }
}
```

```
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
```

# 七.结果

从结果可以看出，写入操作全部通过ds1数据库（master）数据库完成，而读操作因为设置了轮询的缘故，由ds2（slaver）、ds3（slaver2）数据库完成。这样就实现了基于一主二从的数据库集群的读写分离操作。
![image.png](https://b3logfile.com/file/2021/07/image-8a186f62.png)

# 八.Sharding-JDBC不同版本上的配置

网上Sharding-JDBC的教程多为4.0.0.RC1版本，笔者使用的是最新的4.1.1所以
在该部分数据库地址在4.1.1为jdbc-url在4.0.0.RC1上需要改为url否则会启动失败

```
jdbc-url: jdbc:mysql://XXXX/XXXX
```

且网上教程多为properties文件，笔者将其转变为YAML文件更加能直观感受
