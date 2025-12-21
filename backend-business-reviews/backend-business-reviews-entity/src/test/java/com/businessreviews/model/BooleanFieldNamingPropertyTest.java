package com.businessreviews.model;

import com.businessreviews.model.dataobject.*;
import com.businessreviews.model.vo.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boolean字段命名规范属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 7: Boolean字段命名规范**
 * **Validates: Requirements 4.1**
 * 
 * 验证POJO类中的Boolean字段不以"is"开头
 */
class BooleanFieldNamingPropertyTest {
    
    /**
     * Property 7: Boolean字段命名规范
     * *For any* POJO类中的Boolean类型字段，字段名 SHALL 不以"is"开头
     */
    @Test
    void doClassesBooleanFieldsShouldNotStartWithIs() {
        List<Class<?>> doClasses = List.of(
            UserDO.class,
            ShopDO.class,
            NoteDO.class,
            MerchantDO.class,
            MerchantUserDO.class,
            CommentDO.class,
            CouponDO.class
        );
        
        for (Class<?> clazz : doClasses) {
            checkBooleanFieldNaming(clazz);
        }
    }
    
    /**
     * 验证VO类的Boolean字段命名
     */
    @Test
    void voClassesBooleanFieldsShouldNotStartWithIs() {
        List<Class<?>> voClasses = List.of(
            UserVO.class,
            ShopVO.class,
            NoteVO.class
        );
        
        for (Class<?> clazz : voClasses) {
            checkBooleanFieldNaming(clazz);
        }
    }
    
    /**
     * 检查类中的Boolean字段命名
     */
    private void checkBooleanFieldNaming(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            
            // 检查Boolean和boolean类型的字段
            if (fieldType == Boolean.class || fieldType == boolean.class) {
                String fieldName = field.getName();
                
                assertThat(fieldName)
                    .as("Boolean field '%s' in class %s should not start with 'is'", 
                        fieldName, clazz.getSimpleName())
                    .doesNotMatch("^is[A-Z].*");
            }
        }
    }
    
    /**
     * 列出所有需要修正的Boolean字段（用于生成修复报告）
     */
    @Test
    void listBooleanFieldsStartingWithIs() {
        List<Class<?>> allClasses = new ArrayList<>();
        
        // 添加所有需要检查的类
        allClasses.add(UserDO.class);
        allClasses.add(ShopDO.class);
        allClasses.add(NoteDO.class);
        allClasses.add(MerchantDO.class);
        allClasses.add(MerchantUserDO.class);
        allClasses.add(CommentDO.class);
        allClasses.add(CouponDO.class);
        allClasses.add(UserVO.class);
        allClasses.add(ShopVO.class);
        allClasses.add(NoteVO.class);
        
        List<String> violations = new ArrayList<>();
        
        for (Class<?> clazz : allClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> fieldType = field.getType();
                if ((fieldType == Boolean.class || fieldType == boolean.class) 
                    && field.getName().matches("^is[A-Z].*")) {
                    violations.add(clazz.getSimpleName() + "." + field.getName());
                }
            }
        }
        
        // 打印违规字段（用于调试）
        if (!violations.isEmpty()) {
            System.out.println("Boolean fields starting with 'is' that need to be fixed:");
            violations.forEach(v -> System.out.println("  - " + v));
        }
        
        // 新创建的DO和VO类应该没有违规
        assertThat(violations)
            .as("New DO and VO classes should not have Boolean fields starting with 'is'")
            .isEmpty();
    }
}
