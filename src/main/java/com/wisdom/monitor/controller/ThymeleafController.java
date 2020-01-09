package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.model.WDCInfo;
import com.wisdom.monitor.utils.HttpRequestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ThymeleafController {

    @Value("${node}")
    private String url_node;

    @RequestMapping("/thymeleaf")
    public String index(ModelMap map) {
        // 加入一个属性，用来在模板中读取
        map.addAttribute("baidu", "http://www.baidu.com");
        // return模板文件的名称，对应src/main/resources/templates/index.html
        return "test";
    }

    @RequestMapping(value = {"/", "/basic"})
    public String Basci(ModelMap map) {
        return "basic";
    }

    @RequestMapping("/basic_info")
    public String localRefresh(ModelMap map)
    {
        WDCInfo info = new WDCInfo();
        try {
            JSONObject get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/WisdomCore/ExplorerInfo", url_node), null)).getJSONObject("data");
            String get_price = JSON.parseObject(HttpRequestUtil.sendGet(url_price, "market=wdc_qc")).getJSONObject("data").getString("last");
            info = JSON.toJavaObject(get_info, WDCInfo.class);
            info.setPrice(get_price);
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.addAttribute("result", info);
        return "basic_info::basic_info";
    }

    @RequestMapping("/pass")
    public String pass(ModelMap map) {
        return "pass";
    }

    private static String url_price = "https://api.ceobi.com/api/market/ticker";
}
