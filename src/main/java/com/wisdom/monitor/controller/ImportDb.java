package com.wisdom.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class ImportDb {
    @RequestMapping("/")
    public String indexw() {
        System.out.println("====this is index");
        return "index";
    }
}
