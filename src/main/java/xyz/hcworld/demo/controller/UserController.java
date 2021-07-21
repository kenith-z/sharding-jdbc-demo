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