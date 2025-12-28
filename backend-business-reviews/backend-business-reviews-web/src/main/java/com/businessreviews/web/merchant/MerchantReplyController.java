package com.businessreviews.web.merchant;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.ai.GenerateReplyDTO;
import com.businessreviews.model.vo.ai.GenerateReplyVO;
import com.businessreviews.service.ai.ReviewReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    /**
     * AI 生成差评回复
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
}
