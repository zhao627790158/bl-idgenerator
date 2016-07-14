package cn.blueshit.idgenerator.dao;

import java.lang.annotation.*;

/**
 * Created by zhaoheng on 2016/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyBatisScan {
    String value() default "";
}
