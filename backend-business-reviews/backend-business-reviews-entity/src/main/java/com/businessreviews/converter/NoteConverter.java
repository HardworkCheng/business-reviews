package com.businessreviews.converter;

import com.businessreviews.model.dataobject.NoteDO;
import com.businessreviews.model.vo.NoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记对象转换器
 * 负责NoteDO与NoteVO之间的转换
 * 
 * @author businessreviews
 */
@Component
public class NoteConverter {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * NoteDO转NoteVO
     * 
     * @param noteDO 笔记数据对象
     * @return 笔记视图对象
     */
    public NoteVO toVO(NoteDO noteDO) {
        if (noteDO == null) {
            return null;
        }
        
        NoteVO vo = new NoteVO();
        BeanUtils.copyProperties(noteDO, vo);
        
        // ID转字符串，避免前端精度丢失
        vo.setNoteId(noteDO.getId() != null ? noteDO.getId().toString() : null);
        
        // 解析图片JSON为列表
        vo.setImages(parseImages(noteDO.getImages()));
        
        // 推荐状态转Boolean
        vo.setRecommend(noteDO.getRecommend() != null && noteDO.getRecommend() == 1);
        
        // 创建时间格式化
        if (noteDO.getCreatedAt() != null) {
            vo.setCreatedAt(noteDO.getCreatedAt().format(DATE_TIME_FORMATTER));
        }
        
        return vo;
    }
    
    /**
     * NoteDO列表转NoteVO列表
     * 
     * @param doList 笔记数据对象列表
     * @return 笔记视图对象列表
     */
    public List<NoteVO> toVOList(List<NoteDO> doList) {
        if (doList == null) {
            return null;
        }
        return doList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 解析图片JSON字符串为列表
     * 
     * @param imagesJson 图片JSON字符串
     * @return 图片URL列表
     */
    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
