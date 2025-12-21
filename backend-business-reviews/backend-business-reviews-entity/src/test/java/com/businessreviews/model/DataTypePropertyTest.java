package com.businessreviews.model;

import com.businessreviews.model.dataobject.*;
import com.businessreviews.model.vo.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据类型规范属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 8: POJO属性类型规范**
 * **Feature: backend-alibaba-refactor, Property 9: Service方法返回类型规范**
 * **Validates: Requirements 5.1, 5.2**
 * 
 * 验证POJO类属性使用包装数据类型而非基本数据类型
 */
class DataTypePropertyTest {
    
    /**
     * 基本数据类型集合
     */
    private static final Set<Class<?>> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
        int.class, long.class, double.class, float.class, 
        short.class, byte.class, char.class, boolean.class
    ));
    
    /**
     * Property 8: POJO属性类型规范
     * *For any* POJO类中的数值类型属性，类型 SHALL 为包装类型（Integer/Long/Double等）而非基本类型
     */
    @Test
    void doClassesShouldUseWrapperTypes() {
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
            checkWrapperTypes(clazz);
        }
    }
    
    /**
     * 验证VO类使用包装类型
     */
    @Test
    void voClassesShouldUseWrapperTypes() {
        List<Class<?>> voClasses = List.of(
            UserVO.class,
            ShopVO.class,
            NoteVO.class
        );
        
        for (Class<?> clazz : voClasses) {
            checkWrapperTypes(clazz);
        }
    }
    
    /**
     * 检查类中的字段是否使用包装类型
     */
    private void checkWrapperTypes(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            
            // 跳过serialVersionUID
            if (field.getName().equals("serialVersionUID")) {
                continue;
            }
            
            // 检查是否使用了基本数据类型
            assertThat(PRIMITIVE_TYPES.contains(fieldType))
                .as("Field '%s' in class %s should use wrapper type instead of primitive type %s", 
                    field.getName(), clazz.getSimpleName(), fieldType.getSimpleName())
                .isFalse();
        }
    }
    
    /**
     * 验证DO类的ID字段使用Long而非long
     */
    @Test
    void idFieldsShouldUseLongWrapper() {
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
            try {
                Field idField = clazz.getDeclaredField("id");
                assertThat(idField.getType())
                    .as("ID field in class %s should be Long", clazz.getSimpleName())
                    .isEqualTo(Long.class);
            } catch (NoSuchFieldException e) {
                // 如果没有id字段，跳过
            }
        }
    }
    
    /**
     * 验证状态字段使用Integer而非int
     */
    @Test
    void statusFieldsShouldUseIntegerWrapper() {
        List<Class<?>> doClasses = List.of(
            UserDO.class,
            ShopDO.class,
            NoteDO.class,
            MerchantDO.class,
            CouponDO.class
        );
        
        for (Class<?> clazz : doClasses) {
            try {
                Field statusField = clazz.getDeclaredField("status");
                assertThat(statusField.getType())
                    .as("Status field in class %s should be Integer", clazz.getSimpleName())
                    .isEqualTo(Integer.class);
            } catch (NoSuchFieldException e) {
                // 如果没有status字段，跳过
            }
        }
    }
}
