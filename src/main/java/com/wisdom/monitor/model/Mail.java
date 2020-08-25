package com.wisdom.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Mail {
    private String sender;
    private String receiver;
    private String password;
}
