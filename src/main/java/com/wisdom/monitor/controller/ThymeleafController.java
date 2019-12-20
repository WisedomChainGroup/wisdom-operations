package com.wisdom.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ThymeleafController {

    @RequestMapping("/thymeleaf")
    public String index(ModelMap map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("baidu", "http://www.baidu.com");
        // return模板文件的名称，对应src/main/resources/templates/index.html
        return "test";
    }
}
