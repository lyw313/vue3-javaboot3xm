package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$",message = "用户名格式错误") String username, @Pattern(regexp = "^\\S{5,16}$",message = "密码格式错误")String password) {


        //查询用户
        User u = userService.findByUserName(username);
        if (u == null) {
            //没有占用
            //注册
            userService.register(username, password);
            return Result.success();
        } else {
            //占用
            return Result.error("用户名已被占用");
        }
    }

    @PostMapping("/login")
    public Result login(@Pattern(regexp = "^\\S{5,16}$",message = "用户名格式错误") String username, @Pattern(regexp = "^\\S{5,16}$",message = "密码格式错误")String password) {
        User logigUser = userService.findByUserName(username);
        if (logigUser  == null) {
            return Result.error("用户名不存在");
        } else {
            //密码校验
            if (Md5Util.getMD5String( password).equals(logigUser .getPassword())) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("id", logigUser .getId());
                claims.put("username", logigUser .getUsername());
                String token = JwtUtil.genToken(claims);
                return Result.success(token);
            } else {
                return Result.error("密码错误");
            }
        }
    }
    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name = "Authorization") String token*/) {
        //根据用户名查询用户
       /* Map<String, Object> map = JwtUtil.parseToken(token);
        String username = (String) map.get("username");*/
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        return Result.success(user);
    }
    @PutMapping("/update")
    public Result update(@RequestBody @Validated  User user) {
        userService.update(user);
        return Result.success();
    }
    @PatchMapping("/updateAvatar")
    public Result updateAvater(@RequestParam @URL String avatarUrl) {
        userService.updateAvater(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, Object> param) {

        String oldPwd = (String) param.get("old_Pwd");
        String newPwd = (String) param.get("new_Pwd");
        String rePwd = (String) param.get("re_Pwd");
        if (String.valueOf(oldPwd).isEmpty() || String.valueOf(newPwd).isEmpty() || String.valueOf(rePwd).isEmpty()) {
            return Result.error("密码不能为空");
        }

        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        if (!Md5Util.getMD5String(oldPwd).equals(user.getPassword())) {
            return Result.error("旧密码错误");
        }
        if (!newPwd.equals(rePwd)) {
            return Result.error("两次密码不一致");
        }
        if (newPwd.equals(oldPwd)) {
            return Result.error("新密码不能与旧密码一致");
        }

        userService.updatePwd(Md5Util.getMD5String(newPwd));
        return Result.success();
    }


}


