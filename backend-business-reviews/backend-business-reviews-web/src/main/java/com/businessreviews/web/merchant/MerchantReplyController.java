package com.businessreviews.web.merchant;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.ai.GenerateReplyDTO;
import com.businessreviews.model.vo.ai.GenerateReplyVO;
import com.businessreviews.service.ai.ReviewReplyService;
import com.businessreviews.service.ai.SmartReplyStreamAgent;
import dev.langchain4j.service.TokenStream;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 商家 AI 回复控制器
 * 
 * @author businessreviews
 */
@Slf4j
@RestController
@RequestMapping("/merchant/reply")
@RequiredArgsConstructor
public class MerchantReplyController {

    private final ReviewReplyService reviewReplyService;
    private final SmartReplyStreamAgent smartReplyStreamAgent;

    // 线程池用于异步处理 SSE
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * AI 生成差评回复（同步版本，保持向后兼容）
     * 
     * @param dto 请求参数
     * @return 生成的回复内容
     */
    @PostMapping("/generate")
    public Result<GenerateReplyVO> generateReply(@Valid @RequestBody GenerateReplyDTO dto) {
        log.info("收到AI生成回复请求, 评论长度: {}, 策略: {}",
                dto.getReviewText().length(),
                dto.getStrategy());

        String reply = reviewReplyService.generateReply(dto.getReviewText(), dto.getStrategy());

        GenerateReplyVO vo = GenerateReplyVO.builder()
                .reply(reply)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        return Result.success(vo);
    }

    /**
     * AI 生成差评回复（SSE 流式版本）
     * 使用 Server-Sent Events 实现打字机效果
     * 
     * @param dto 请求参数
     * @return SseEmitter 用于流式推送
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateReplyStream(@Valid @RequestBody GenerateReplyDTO dto) {
        log.info("收到AI流式生成回复请求, 评论长度: {}, 策略: {}",
                dto.getReviewText().length(),
                dto.getStrategy());

        // 创建 SseEmitter，超时时间 2 分钟
        SseEmitter emitter = new SseEmitter(120000L);

        executor.execute(() -> {
            try {
                // 处理赠礼策略
                String giftStrategy = StringUtils.hasText(dto.getStrategy())
                        ? dto.getStrategy()
                        : "无（不提供任何赠品或优惠）";

                // 获取 TokenStream
                TokenStream tokenStream = smartReplyStreamAgent.generateSmartReplyStream(
                        dto.getReviewText(),
                        giftStrategy);

                // 订阅 token 流
                tokenStream
                        .onNext(token -> {
                            try {
                                // 发送每个 token 作为 SSE 事件
                                emitter.send(SseEmitter.event()
                                        .name("token")
                                        .data(token));
                            } catch (IOException e) {
                                log.error("SSE 发送失败", e);
                                emitter.completeWithError(e);
                            }
                        })
                        .onComplete(response -> {
                            try {
                                // 发送完成事件
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("[DONE]"));
                                emitter.complete();
                                log.info("AI流式回复生成完成");
                            } catch (IOException e) {
                                log.error("SSE 完成事件发送失败", e);
                            }
                        })
                        .onError(error -> {
                            log.error("AI流式回复生成失败", error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("AI生成失败: " + error.getMessage()));
                            } catch (IOException e) {
                                log.error("SSE 错误事件发送失败", e);
                            }
                            emitter.completeWithError(error);
                        })
                        .start();

            } catch (Exception e) {
                log.error("AI流式回复初始化失败", e);
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接关闭"));

        return emitter;
    }
}
