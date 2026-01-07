package com.businessreviews.web.app;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import com.businessreviews.model.vo.ai.NoteGenerateVO;
import com.businessreviews.service.ai.VisionNoteService;
import com.businessreviews.service.ai.VisionNoteStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI智能笔记生成控制器
 * <p>
 * 提供探店笔记AI智能生成接口：
 * - POST /note/generate - 同步生成（等待完成后返回）
 * - POST /note/generate/stream - 流式生成（SSE 打字机效果）
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.ai.VisionNoteService
 * @see com.businessreviews.service.ai.VisionNoteStreamService
 */
@Slf4j
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteAIController {

    private final VisionNoteService visionNoteService;
    private final VisionNoteStreamService visionNoteStreamService;

    // 线程池用于异步处理 SSE
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * AI智能生成探店笔记（同步版本）
     * <p>
     * 根据用户上传的图片URL和可选的标签，利用视觉AI模型生成小红书风格的探店笔记。
     * 等待生成完成后一次性返回结果。
     * </p>
     *
     * @param request 生成请求，包含商家名、图片URL列表、标签列表
     * @return 生成的笔记标题和正文
     */
    @PostMapping("/generate")
    public Result<NoteGenerateVO> generateNote(@RequestBody @Valid NoteGenerateRequest request) {
        log.info("收到AI生成笔记请求（同步），商家: {}, 图片数量: {}, 标签数量: {}",
                request.getShopName(),
                request.getImageUrls() != null ? request.getImageUrls().size() : 0,
                request.getTags() != null ? request.getTags().size() : 0);

        NoteGenerateVO result = visionNoteService.generateNote(request);

        log.info("AI笔记生成成功，标题长度: {}, 内容长度: {}",
                result.getTitle().length(),
                result.getContent().length());

        return Result.success(result);
    }

    /**
     * AI智能生成探店笔记（SSE 流式版本）
     * <p>
     * 使用 Server-Sent Events 实现打字机效果，AI 生成的内容会逐字推送给前端。
     * 提供更好的用户体验，用户可以看到实时生成过程。
     * </p>
     * 
     * <p>
     * SSE 事件类型：
     * </p>
     * <ul>
     * <li><code>token</code> - 每个生成的字符/词元</li>
     * <li><code>done</code> - 生成完成信号 "[DONE]"</li>
     * <li><code>error</code> - 生成失败错误信息</li>
     * </ul>
     *
     * @param request 生成请求，包含商家名、图片URL列表、标签列表
     * @return SseEmitter 用于流式推送
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateNoteStream(@RequestBody @Valid NoteGenerateRequest request) {
        log.info("收到AI生成笔记请求（流式），商家: {}, 图片数量: {}, 标签数量: {}",
                request.getShopName(),
                request.getImageUrls() != null ? request.getImageUrls().size() : 0,
                request.getTags() != null ? request.getTags().size() : 0);

        // 创建 SseEmitter，超时时间 3 分钟（视觉模型可能需要更长时间）
        SseEmitter emitter = new SseEmitter(180000L);

        // 异步执行流式生成
        executor.execute(() -> visionNoteStreamService.generateNoteStream(request, emitter));

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接关闭"));
        emitter.onError(e -> log.error("SSE 连接错误: {}", e.getMessage()));

        return emitter;
    }
}
