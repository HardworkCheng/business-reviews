package com.businessreviews.converter;

import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.model.vo.UserVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserConverter属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 5: 对象转换正确性**
 * **Feature: backend-alibaba-refactor, Property 6: 手机号脱敏正确性**
 * **Validates: Requirements 2.3, 2.5**
 */
@PropertyDefaults(tries = 100)
class UserConverterPropertyTest {
    
    private final UserConverter converter = new UserConverter();
    
    /**
     * Property 5: 对象转换正确性
     * *For any* UserDO对象，通过Converter转换为UserVO后，非敏感字段的值 SHALL 保持一致
     */
    @Property
    void conversionPreservesNonSensitiveFields(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long id,
            @ForAll @StringLength(min = 1, max = 50) String username,
            @ForAll @StringLength(min = 0, max = 200) String avatar,
            @ForAll @StringLength(min = 0, max = 500) String bio,
            @ForAll @IntRange(min = 0, max = 2) Integer gender
    ) {
        UserDO userDO = new UserDO();
        userDO.setId(id);
        userDO.setUsername(username);
        userDO.setAvatar(avatar);
        userDO.setBio(bio);
        userDO.setGender(gender);
        
        UserVO userVO = converter.toVO(userDO);
        
        assertThat(userVO).isNotNull();
        assertThat(userVO.getUserId()).isEqualTo(id.toString());
        assertThat(userVO.getUsername()).isEqualTo(username);
        assertThat(userVO.getAvatar()).isEqualTo(avatar);
        assertThat(userVO.getBio()).isEqualTo(bio);
        assertThat(userVO.getGender()).isEqualTo(gender);
    }
    
    /**
     * Property 6: 手机号脱敏正确性
     * *For any* 11位手机号，脱敏后的格式 SHALL 为"前3位****后4位"
     */
    @Property
    void phoneMaskingFormat(@ForAll("validPhones") String phone) {
        String masked = converter.maskPhone(phone);
        
        // 验证格式：前3位 + **** + 后4位
        assertThat(masked).hasSize(11);
        assertThat(masked.substring(0, 3)).isEqualTo(phone.substring(0, 3));
        assertThat(masked.substring(3, 7)).isEqualTo("****");
        assertThat(masked.substring(7)).isEqualTo(phone.substring(7));
    }
    
    /**
     * 验证脱敏后保留了正确的前缀和后缀
     */
    @Property
    void phoneMaskingPreservesEnds(@ForAll("validPhones") String phone) {
        String masked = converter.maskPhone(phone);
        
        // 前3位保持不变
        assertThat(masked.substring(0, 3)).isEqualTo(phone.substring(0, 3));
        // 后4位保持不变
        assertThat(masked.substring(7)).isEqualTo(phone.substring(7));
    }
    
    /**
     * 验证非11位手机号不进行脱敏
     */
    @Property
    void invalidPhoneNotMasked(@ForAll("invalidPhones") String phone) {
        String masked = converter.maskPhone(phone);
        assertThat(masked).isEqualTo(phone);
    }
    
    /**
     * 验证null输入返回null
     */
    @Example
    void nullUserDOReturnsNull() {
        assertThat(converter.toVO(null)).isNull();
    }
    
    /**
     * 验证null手机号返回null
     */
    @Example
    void nullPhoneReturnsNull() {
        assertThat(converter.maskPhone(null)).isNull();
    }
    
    /**
     * 生成有效的11位手机号
     */
    @Provide
    Arbitrary<String> validPhones() {
        return Arbitraries.strings()
                .numeric()
                .ofLength(11)
                .filter(s -> s.startsWith("1"));
    }
    
    /**
     * 生成无效的手机号（非11位）
     */
    @Provide
    Arbitrary<String> invalidPhones() {
        return Arbitraries.strings()
                .numeric()
                .ofMinLength(1)
                .ofMaxLength(20)
                .filter(s -> s.length() != 11);
    }
}
