package com.wang.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

@Documented //标记这些注解是否包含在用户文档中
@Retention(RetentionPolicy.RUNTIME) //标识这个注解怎么保存，是只在代码中，还是编入class文件中，或者是在运行时可以通过反射访问
@Target({ElementType.TYPE}) //标记这个注解应该是哪种 Java 成员
@Inherited //标记这个注解是继承于哪个注解类(默认注解并没有继承于任何子类)
@Component //被spring扫描
public @interface RpcService {

    /**
     * 服务版本，默认为空
     */
    String version() default "";

    /**
     * 服务组，默认为空
     */
    String group() default "";
}
