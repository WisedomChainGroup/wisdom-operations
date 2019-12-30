package com.wisdom.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.model.Transaction;
import com.wisdom.monitor.service.TransactionService;
import com.wisdom.monitor.utils.ApiResult.APIResult;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class ExportDb {

    @Autowired
    TransactionService transactionService;

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "thymeleaf");
        return "test-push";
    }

//    @PostMapping("/exportExcel")
//    @ResponseBody
    @GetMapping("/export/txrecordFromAddress")
    public void export(HttpServletResponse response, @RequestParam("json") String json){
        try {
            Transaction transaction = JSON.parseObject(json, Transaction.class);
            APIResult result = transactionService.getTxrecordFromAddress(transaction.getFrom());
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            //创建excel文件
            HSSFWorkbook wb = new HSSFWorkbook();
            //创建sheet页
            HSSFSheet sheet = wb.createSheet("事务详情");
            //创建标题行
            HSSFRow titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("事务哈希");
            titleRow.createCell(1).setCellValue("金额");
            titleRow.createCell(2).setCellValue("所在区块高度");
            titleRow.createCell(3).setCellValue("发起者");
            titleRow.createCell(4).setCellValue("接收者");
            titleRow.createCell(5).setCellValue("区块哈希");
            titleRow.createCell(6).setCellValue("日期");
            sheet.setDefaultColumnWidth(10);
            sheet.setColumnWidth(0, 64*256);
            sheet.setColumnWidth(1, 17*256);
            sheet.setColumnWidth(3, 40*256);
            sheet.setColumnWidth(4, 40*256);
            sheet.setColumnWidth(5, 54*256);
            sheet.setColumnWidth(6, 20*256);
            for (Map map:list){
                HSSFRow dataRow =sheet.createRow(sheet.getLastRowNum()+1);
                dataRow.createCell(0).setCellValue(map.get("tx_hash").toString());
                dataRow.createCell(1).setCellValue(map.get("amount").toString());
                dataRow.createCell(2).setCellValue(map.get("height").toString());
                dataRow.createCell(3).setCellValue(map.get("from").toString());
                dataRow.createCell(4).setCellValue(map.get("to").toString());
                dataRow.createCell(5).setCellValue(map.get("block_hash").toString());
                dataRow.createCell(6).setCellValue(map.get("datetime").toString());
            }
            // 设置下载时客户端Excel的名称
            String filename =new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-test.xls";
            //设置下载的文件
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename=" + filename);
            OutputStream ouputStream = response.getOutputStream();//打开流
            wb.write(ouputStream); //在excel内写入流
            ouputStream.flush();// 刷新流
            ouputStream.close();// 关闭流
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
