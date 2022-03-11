package xmg.client.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author makui
 * @created on  2022/3/12
 **/
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcMode {
    /**
     * post / get
     * @return value
     */
    String value() default "";
}
