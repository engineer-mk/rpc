package xmg.client.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcApi {

    /**
     * 指定url下此配置无效
     * @return value
     */
    String value() default "";

    /**
     * 优先级高于value
     * @return url
     */
    String url() default "";

    String trace() default "false";

    /**
     * 主从配置下主库节点Id 注:指定url模式下无效
     * @return masterId
     */
    String masterId() default "";
}
