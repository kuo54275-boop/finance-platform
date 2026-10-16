package com.finance.controller;

import com.finance.service.UserService;
import com.finance.service.RecordService;
import com.finance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin // 💡 允许管理员跨域调用
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 一、全局系统宏观核心统计看板
     */
    @GetMapping("/sys-stats")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> result = new HashMap<>();
        long totalUsers = userService.count();
        long totalRecords = recordService.count();

        String sqlActive = "SELECT COUNT(DISTINCT user_id) FROM record";
        Integer activeUsers = jdbcTemplate.queryForObject(sqlActive, Integer.class);
        activeUsers = (activeUsers == null) ? 0 : activeUsers;

        Map<String, Object> stats = new HashMap<>();
        stats.put("platformTotalUsers", totalUsers);
        stats.put("platformTotalRecords", totalRecords);
        stats.put("activeUsers30Days", activeUsers);

        result.put("code", 200);
        result.put("data", stats);
        return result;
    }

    /**
     * 二、获取全量平台注册用户列表（精准加载全部用户）
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userService.list());
        return result;
    }

    /**
     * 三、注销/删除指定平台用户
     */
    @DeleteMapping("/user/delete/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean removed = userService.removeById(id);
        if (removed) {
            result.put("code", 200);
            result.put("msg", "该平台用户已依法从系统底层彻底注销清理！");
        } else {
            result.put("code", 500);
            result.put("msg", "注销失败：未找到该用户");
        }
        return result;
    }

    /**
     * 四、获取全部系统预设常用收支类别一览
     */
    @GetMapping("/category/list")
    public Map<String, Object> getCategoryList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", categoryService.list());
        return result;
    }

    /**
     * 五、建立并配置系统常用类别字典（支持类别提交）
     */
    @PostMapping("/category/add")
    public Map<String, Object> addCategory(@RequestBody com.finance.entity.Category category) {
        Map<String, Object> result = new HashMap<>();
        boolean saved = categoryService.save(category);
        if (saved) {
            result.put("code", 200);
            result.put("msg", "新财务收支核算常用预设类别发布成功！");
        } else {
            result.put("code", 500);
            result.put("msg", "预设类别发布失败");
        }
        return result;
    }

    /**
     * 六、剔除多余的常用预设类别
     */
    @DeleteMapping("/category/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean removed = categoryService.removeById(id);
        if (removed) {
            result.put("code", 200);
            result.put("msg", "该预设收支类别剔除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "剔除失败");
        }
        return result;
    }
}