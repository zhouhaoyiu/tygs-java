package com.zhy.market.controller;

import com.zhy.market.domain.User;
import com.zhy.market.domain.UserInfo;
import com.zhy.market.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static com.zhy.market.controller.utils.getJsonRes;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserMapper userMapper;

    @PostMapping("login")
    public Object userLogin(@RequestBody User userLoginInfo) {
        String username = userLoginInfo.getUsername();
        String password = userLoginInfo.getPassword();

        List<UserInfo> list = userMapper.userLogin(username, password);
        if (list.isEmpty()) {
            return getJsonRes(1, "登录失败,用户名或密码错误", (List) null);
        }
        return getJsonRes(0, "登录成功", list);

    }

    @PostMapping("userRegis")
    public Object userRegis(@RequestBody User user) {

        String username = user.getUsername();
        String password = user.getPassword();
        String address = user.getAddress();
        String phonenumber = user.getPhonenumber();
        Boolean gender = user.getGender();
        UUID useruuid = UUID.randomUUID();

        Integer result = userMapper.userRegis(username, password, gender, address, phonenumber, useruuid.toString());
        if (result > 0) {
            return getJsonRes(0, "注册成功", (List) null);
        }
        return getJsonRes(1, "注册失败", (List) null);
    }

    @GetMapping("allUsers")
    public Object getAllUser() {
        List<User> allUsers = userMapper.getAllUser();
        return getJsonRes(0, "获取成功", allUsers);
    }
}
