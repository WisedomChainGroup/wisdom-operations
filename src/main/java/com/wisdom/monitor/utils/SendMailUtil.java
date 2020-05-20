package com.wisdom.monitor.utils;

import com.alibaba.fastjson.JSONObject;
import com.wisdom.monitor.leveldb.Leveldb;
import com.wisdom.monitor.model.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
@EnableScheduling
public class SendMailUtil {
    private static final Logger logger = LoggerFactory.getLogger(SendMailUtil.class);
    public static boolean sendMailOutLook(String title, String body) throws IOException {
        Leveldb leveldb = new Leveldb();
        Mail mail = new Mail();
        Object read = JSONObject.parseObject(leveldb.readAccountFromSnapshot("mail"), Mail.class);
        if (read != null) {
            mail= (Mail) read;
            logger.info("-------------sender------------"+mail.getSender());
            logger.info("-------------Password------------"+mail.getPassword());
            logger.info("-------------Receiver------------"+mail.getReceiver());
            //设置参数
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.outlook.com");
            props.put("mail.smtp.port", "587");
            //自定义信息
            props.put("username", mail.getSender());//你的邮箱
            props.put("password", mail.getPassword());//你的密码
            props.put("to", mail.getReceiver());//接收的邮箱
            return SendMailUtil.send(props,title,body);
        }
        return false;
    }

    /**
     * 获取系统当前的时间
     * 以传入时间格式返回，传空返回默认格式
     * @param format 时间格式
     * @return
     */
    private static String getTitleTimeFormat(String format) {
        if (format == null) {
            format = "yyyy-MM-dd HH:mm:ss/SSS";
        }
        SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 发送邮件，获取参数，和标题还有内容
     * @param props 参数
     * @param title 标题
     * @param body 内容
     * @return
     */
    private static Boolean send(Properties props, String title, String body){
        //发送邮件地址
        final String username=props.getProperty("username");
        //发送邮件名称
        final String password=props.getProperty("password");
        //接收邮件地址
        String to=props.getProperty("to");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(title+"("+SendMailUtil.getTitleTimeFormat(null)+")");
            message.setContent(body,"text/html;charset=utf-8");

            Transport.send(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        System.out.println("发送完毕！");

        return true;
    }

    public static void main(String[] args) throws IOException {
        StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
        messageText.append("<span>警告:</span></br>");
        messageText.append("<span>您绑定的节点出现分叉！</span></br>");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.outlook.com");
        props.put("mail.smtp.port", "587");
        //自定义信息
        props.put("username", "wdcserver@outlook.com");//你的邮箱
        props.put("password", "Wisdom!@#123");//你的密码
        props.put("to", "840043122@qq.com");//接收的邮箱
        SendMailUtil.send(props,"通知",messageText.toString());

    }
}
