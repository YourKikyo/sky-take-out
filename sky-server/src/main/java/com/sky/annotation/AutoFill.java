package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)         //表示这个注解只能加在方法上
@Retention(RetentionPolicy.RUNTIME) //表示这个注解在运行时生效
public @interface AutoFill {
    OperationType value();          //数据库操作类型：INSERT、UPDATE
}
