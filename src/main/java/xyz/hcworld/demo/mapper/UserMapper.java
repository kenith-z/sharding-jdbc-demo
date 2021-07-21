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
