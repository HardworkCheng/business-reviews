package com.businessreviews.converter;

import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户对象转换器
 * 负责UserDO与UserVO之间的转换
 * 
 * @author businessreviews
 */
@Component
public class UserConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * UserDO转UserVO
     * 
     * @param userDO 用户数据对象
     * @return 用户视图对象
     */
    public UserVO toVO(UserDO userDO) {
        if (userDO == null) {
            return null;
        }
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(userDO, vo);
        
        // ID转字符串，避免前端精度丢失
        vo.setUserId(userDO.getId() != null ? userDO.getId().toString() : null);
        
        // 手机号脱敏
        vo.setPhone(maskPhone(userDO.getPhone()));
        
        // 生日格式化
        if (userDO.getBirthday() != null) {
            vo.setBirthday(userDO.getBirthday().format(DATE_FORMATTER));
        }
        
        return vo;
    }
    
    /**
     * UserDO列表转UserVO列表
     * 
     * @param doList 用户数据对象列表
     * @return 用户视图对象列表
     */
    public List<UserVO> toVOList(List<UserDO> doList) {
        if (doList == null) {
            return null;
        }
        return doList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 手机号脱敏
     * 将11位手机号中间4位替换为****
     * 
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
