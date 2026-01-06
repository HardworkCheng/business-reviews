package com.businessreviews.model.annotation;

import com.businessreviews.model.serializer.SensitiveSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 敏感数据脱敏注解
 * <p>
 * 在 JSON 序列化阶段自动对敏感数据进行脱敏处理。
 * 支持手机号、邮箱、身份证号等多种脱敏策略。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * public class UserVO {
 *     &#64;Sensitive(type = SensitiveType.PHONE)
 *     private String phone;
 * 
 *     &#64;Sensitive(type = SensitiveType.EMAIL)
 *     private String email;
 * }
 * </pre>
 *
 * @author businessreviews
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {

    /**
     * 脱敏类型
     */
    SensitiveType type() default SensitiveType.PHONE;
}
