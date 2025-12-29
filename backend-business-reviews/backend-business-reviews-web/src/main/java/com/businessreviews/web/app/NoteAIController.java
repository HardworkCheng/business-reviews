package com.businessreviews.web.app;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import com.businessreviews.model.vo.ai.NoteGenerateVO;
import com.businessreviews.service.ai.VisionNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI智能笔记生成控制器
 * 
 * 提供探店笔记AI智能生成接口：
 * - POST /note/generate - 根据图片和标签生成探店笔记
 * 
 * @author businessreviews
 */
@Slf4j
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteAIController {

    private final VisionNoteService visionNoteService;

    /**
     * AI智能生成探店笔记
     * 
     * 根据用户上传的图片URL和可选的标签，利用视觉AI模型生成小红书风格的探店笔记
     * 
     * @param request 生成请求，包含商家名、图片URL列表、标签列表
     * @return 生成的笔记标题和正文
     */
    @PostMapping("/generate")
    public Result<NoteGenerateVO> generateNote(@RequestBody @Valid NoteGenerateRequest request) {
        log.info("收到AI生成笔记请求，商家: {}, 图片数量: {}, 标签数量: {}",
                request.getShopName(),
                request.getImageUrls() != null ? request.getImageUrls().size() : 0,
                request.getTags() != null ? request.getTags().size() : 0);

        NoteGenerateVO result = visionNoteService.generateNote(request);

        log.info("AI笔记生成成功，标题长度: {}, 内容长度: {}",
                result.getTitle().length(),
                result.getContent().length());

        return Result.success(result);
    }
}
