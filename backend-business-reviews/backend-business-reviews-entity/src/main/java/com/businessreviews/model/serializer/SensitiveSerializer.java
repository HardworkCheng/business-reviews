package com.businessreviews.model.serializer;

import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;

/**
 * 敏感数据脱敏序列化器
 * <p>
 * 在 JSON 序列化阶段对标记了 @Sensitive 注解的字段进行脱敏处理。
 * 不同的 SensitiveType 对应不同的脱敏规则。
 * </p>
 *
 * @author businessreviews
 */
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveType sensitiveType;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        String maskedValue = maskValue(value, sensitiveType);
        gen.writeString(maskedValue);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }

        // 获取字段上的 @Sensitive 注解
        Sensitive sensitive = property.getAnnotation(Sensitive.class);
        if (sensitive == null) {
            sensitive = property.getContextAnnotation(Sensitive.class);
        }

        if (sensitive != null && Objects.equals(property.getType().getRawClass(), String.class)) {
            return new SensitiveSerializer(sensitive.type());
        }

        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     * 根据脱敏类型对值进行脱敏处理
     *
     * @param value 原始值
     * @param type  脱敏类型
     * @return 脱敏后的值
     */
    private String maskValue(String value, SensitiveType type) {
        if (type == null || value == null || value.isEmpty()) {
            return value;
        }

        switch (type) {
            case PHONE:
                return maskPhone(value);
            case EMAIL:
                return maskEmail(value);
            case ID_CARD:
                return maskIdCard(value);
            case BANK_CARD:
                return maskBankCard(value);
            case NAME:
                return maskName(value);
            case ADDRESS:
                return maskAddress(value);
            case CUSTOM:
            default:
                return "***";
        }
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间替换为****
     * 138****1234
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }

        int prefixLen = 3;
        int suffixLen = 4;

        if (phone.length() <= prefixLen + suffixLen) {
            return phone;
        }

        return phone.substring(0, prefixLen) + "****" + phone.substring(phone.length() - suffixLen);
    }

    /**
     * 邮箱脱敏：@前只显示前3个字符，其余替换为***
     * abc***@example.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf("@");
        if (atIndex <= 3) {
            return email;
        }

        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    /**
     * 身份证号脱敏：保留前6位和后4位，中间替换为****
     * 330106****1234
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }

        return idCard.substring(0, 6) + "****" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡号脱敏：保留前4位和后4位，中间替换为****
     * 6222****8888
     */
    private String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }

        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 姓名脱敏：只显示第一个字符，其余替换为*
     * 张**
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }

        StringBuilder sb = new StringBuilder(name.substring(0, 1));
        for (int i = 1; i < name.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    /**
     * 地址脱敏：只显示前6个字符，其余替换为***
     * 浙江省杭州***
     */
    private String maskAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }

        return address.substring(0, 6) + "***";
    }
}
