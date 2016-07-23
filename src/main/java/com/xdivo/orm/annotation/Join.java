package com.xdivo.orm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表关联注解
 * Created by jaleel on 16-7-22.
 */

@Target({java.lang.annotation.ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {

    String refColumn(); //被关联列名
}
