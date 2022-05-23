package com.wang;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Hello implements Serializable { //序列化
    private String message;
    private String description;
}
