package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能探店笔记流式生成服务接口
 * <p>
 * 提供 SSE 流式输出的探店笔记生成功能，实现打字机效果体验。
 * </p>
 *
 * @author businessreviews
 */
public interface VisionNoteStreamService {

    /**
     * 流式生成探店笔记
     * <p>
     * 使用 Server-Sent Events 实现逐字推送，前端可实时显示生成过程。
     * </p>
     *
     * @param request 生成请求
     * @param emitter SSE 发射器
     */
    void generateNoteStream(NoteGenerateRequest request, SseEmitter emitter);
}
