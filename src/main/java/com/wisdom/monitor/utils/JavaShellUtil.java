package com.wisdom.monitor.utils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class JavaShellUtil {
    //基本路径
    private static final String basePath = "/tmp/";

    //记录Shell执行状况的日志文件的位置(绝对路径)
    private static final String executeShellLogFile = basePath + "executeShell.log";

    //发送文件到Kondor系统的Shell的文件名(绝对路径)
    private static final String sendKondorShellName = basePath + "sendKondorFile.sh";

    public static boolean judgeImages(String shellCommand, String[] containername) {
        int tag = 0;
        try {
            Runtime rt = Runtime.getRuntime();
            String[] cmd = {"/bin/sh", "-c", shellCommand};
            Process proc = rt.exec(cmd);
            InputStream stderr = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stderr, "GBK");
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            List<String> strList = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                strList.add(line);
                System.out.println(line);
            }
            for (int l = 1; l < strList.size(); l++) {
                if (strList.get(l).indexOf(containername[0]) != -1 || strList.get(l).indexOf(containername[1]) != -1) {
                    tag++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tag == containername.length;
    }

    public static int executeShell(String shellCommand) throws IOException {
        int success = 0;
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        //格式化日期时间，记录日志时使用
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS ");

        try {
            stringBuffer.append(dateFormat.format(new Date())).append("准备执行Shell命令 ").append(shellCommand).append(" \r\n");

            Process pid = null;
            String[] cmd = {"/bin/sh", "-c", shellCommand};
            //执行Shell命令
            pid = Runtime.getRuntime().exec(cmd);
            if (pid != null) {
                stringBuffer.append("进程号：").append(pid.toString()).append("\r\n");
                //bufferedReader用于读取Shell的输出内容 bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
                pid.waitFor();
            } else {
                stringBuffer.append("没有pid\r\n");
            }
            stringBuffer.append(dateFormat.format(new Date())).append("Shell命令执行完毕\r\n执行结果为：\r\n");
            String line = null;
            //读取Shell的输出内容，并添加到stringBuffer中
            while (bufferedReader != null && (line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\r\n");
                System.out.println(line);
            }
        } catch (Exception ioe) {
            stringBuffer.append("执行Shell命令时发生异常：\r\n").append(ioe.getMessage()).append("\r\n");
        } finally {
            if (bufferedReader != null) {
                OutputStreamWriter outputStreamWriter = null;
                try {
                    bufferedReader.close();
                    //将Shell的执行情况输出到日志文件中
                    OutputStream outputStream = new FileOutputStream(executeShellLogFile);
                    outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                    outputStreamWriter.write(stringBuffer.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    outputStreamWriter.close();
                }
            }
            success = 1;
        }
        return success;
    }
}
