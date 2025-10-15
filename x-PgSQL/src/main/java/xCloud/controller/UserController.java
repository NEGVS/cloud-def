package xCloud.controller;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 16:02
 * @ClassName UserController
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xCloud.entity.User;
import xCloud.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    // 查所有（Read）
    @GetMapping
    public List<User> getAllUsers() {
        return userService.list();  // 内置方法
    }

    // 查单个（Read）
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // 查条件（自定义 Read）
    @GetMapping("/search")
    public List<User> searchByName(@RequestParam String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", name);
        return userService.list(wrapper);
    }

    // 增（Create）
    @PostMapping
    public boolean createUser(@RequestBody User user) {
        return userService.save(user);  // 内置方法
    }

    // 增（Create）
    @PostMapping("kk")
    public boolean createUsers(@RequestBody User user) {
        for (int i = 1; i < 10000; i++) {
            user.setId((long) i * 1000);
            userService.save(user);
        }
        return true;  // 内置方法
    }

    // 改（Update）
    @PutMapping("/{id}")
    public boolean updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateById(user);
    }

    // 删（Delete，逻辑删除）
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return userService.removeById(id);
    }
}