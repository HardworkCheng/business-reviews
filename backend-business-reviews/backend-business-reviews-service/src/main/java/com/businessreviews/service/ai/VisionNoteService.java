package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import com.businessreviews.model.vo.ai.NoteGenerateVO;

/**
 * 智能探店笔记生成服务接口
 * 
 * 利用视觉AI模型识别图片内容，结合用户标签，生成小红书风格的探店笔记
 * 
 * @author businessreviews
 */
public interface VisionNoteService {

    /**
     * 根据图片和标签生成探店笔记
     * 
     * 场景说明：
     * - 场景A (有图+无标签): AI充当"眼睛"，识别图片中的食物色泽、分量、环境氛围，自动发挥生成笔记
     * - 场景B (有图+有标签): AI将视觉信息与用户标签结合，生成更贴合用户感受的笔记
     * 
     * @param request 包含商家名、图片URL列表、标签列表的请求对象
     * @return 生成的笔记标题和正文
     */
    NoteGenerateVO generateNote(NoteGenerateRequest request);
}
