package com.businessreviews.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.model.dataobject.BrowseHistoryDO;
import com.businessreviews.mapper.BrowseHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 浏览历史清理定时任务
 * 每天凌晨2点清理7天前的浏览历史
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowseHistoryCleanupTask {

    private final BrowseHistoryMapper browseHistoryMapper;

    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredHistory() {
        log.info("开始清理过期浏览历史...");
        
        try {
            // 计算7天前的时间
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);
            
            // 删除7天前的浏览历史
            LambdaQueryWrapper<BrowseHistoryDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(BrowseHistoryDO::getCreatedAt, expireTime);
            
            int deletedCount = browseHistoryMapper.delete(wrapper);
            log.info("清理过期浏览历史完成，共删除 {} 条记录", deletedCount);
        } catch (Exception e) {
            log.error("清理过期浏览历史失败", e);
        }
    }
}
