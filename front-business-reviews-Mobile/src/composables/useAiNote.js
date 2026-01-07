import { ref } from 'vue'
import { generateNoteByAIStream } from '../api/note'

export function useAiNote() {
    const generating = ref(false) // AI生成中状态
    const aiLoadingText = ref('') // AI 加载状态文案

    // AI 加载状态文案列表
    const aiLoadingStages = [
        '正在分析图片内容...',
        '正在识别图片中的美食...',
        '正在提取关键特征...',
        '正在构思创意文案...',
        '正在组织优美语言...',
        'AI正在深度思考...',
        '即将完成，请稍候...'
    ]

    // 更新 AI 加载状态文案
    let loadingTimer = null
    const startLoadingAnimation = () => {
        let stageIndex = 0
        aiLoadingText.value = aiLoadingStages[0]

        loadingTimer = setInterval(() => {
            stageIndex++
            if (stageIndex < aiLoadingStages.length) {
                aiLoadingText.value = aiLoadingStages[stageIndex]
            }
        }, 2500) // 每 2.5 秒切换一次
    }

    const stopLoadingAnimation = () => {
        if (loadingTimer) {
            clearInterval(loadingTimer)
            loadingTimer = null
        }
        aiLoadingText.value = ''
    }

    /**
     * @param {Object} params - generation parameters
     * @param {Array} params.imageList - list of local image paths
     * @param {Function} params.uploadImagesFunc - function to upload images and return { urls: [] } or urls array
     * @param {Object} params.selectedShop - selected shop object
     * @param {Array} params.selectedTopics - selected topics array
     * @param {Ref} params.titleRef - vue ref for title
     * @param {Ref} params.contentRef - vue ref for content
     */
    const handleMagicGenerate = async ({ imageList, uploadImagesFunc, selectedShop, selectedTopics, titleRef, contentRef }) => {
        // 验证是否有图片
        if (!imageList || imageList.length === 0) {
            uni.showToast({
                title: '请先上传图片',
                icon: 'none'
            })
            return
        }

        // 防止重复点击
        if (generating.value) {
            return
        }

        generating.value = true
        startLoadingAnimation()

        // 清空现有内容，准备流式输入
        titleRef.value = ''
        contentRef.value = ''

        try {
            // 1. 先上传图片获取公网URL
            let imageUrls = []
            uni.showLoading({ title: '正在上传图片...', mask: true })
            try {
                const result = await uploadImagesFunc()
                imageUrls = result.urls || result
                console.log('Use AI Note Images:', imageUrls)
            } finally {
                uni.hideLoading()
            }

            if (!imageUrls || imageUrls.length === 0) {
                throw new Error("图片上传失败")
            }

            // 显示生成中的状态
            uni.showToast({
                title: 'AI正在创作中...',
                icon: 'none',
                duration: 60000 // 保持显示
            })

            // 2. 构建AI生成请求
            const generateRequest = {
                shopName: selectedShop ? selectedShop.name : '',
                imageUrls: imageUrls,
                tags: selectedTopics.map(t => t.name)
            }

            console.log('调用AI流式生成笔记，请求:', generateRequest)

            // 3. 使用流式 API，实现打字机效果
            let fullText = ''
            let isParsingTitle = true // 默认先解析标题
            let titleText = ''
            let contentText = ''

            await generateNoteByAIStream(
                generateRequest,
                // onToken 回调：每个 token 到达时触发
                (token) => {
                    fullText += token

                    // 检查是否遇到分隔符 "---"
                    if (isParsingTitle && fullText.includes('---')) {
                        // 分隔标题和正文
                        const parts = fullText.split('---')
                        titleText = parts[0].trim()
                        contentText = parts.slice(1).join('---').trim()
                        isParsingTitle = false

                        // 更新显示（打字机效果）
                        titleRef.value = titleText
                        contentRef.value = contentText
                    } else if (isParsingTitle) {
                        // 还在解析标题阶段
                        titleRef.value = fullText.trim()
                    } else {
                        // 正在解析正文阶段
                        const parts = fullText.split('---')
                        contentText = parts.slice(1).join('---').trim()
                        contentRef.value = contentText
                    }
                },
                // onComplete 回调：生成完成时触发
                (finalText) => {
                    console.log('AI流式生成完成，总长度:', finalText.length)

                    // 最终解析
                    if (finalText.includes('---')) {
                        const parts = finalText.split('---')
                        titleRef.value = parts[0].trim().replace(/^(标题[:：]?\s*)/, '')
                        contentRef.value = parts.slice(1).join('---').trim().replace(/^(正文[:：]?\s*)/, '')
                    } else if (finalText.includes('\n\n')) {
                        const parts = finalText.split('\n\n')
                        titleRef.value = parts[0].trim()
                        contentRef.value = parts.slice(1).join('\n\n').trim()
                    }

                    uni.hideToast()
                    stopLoadingAnimation()
                    uni.showToast({
                        title: 'AI创作完成！',
                        icon: 'success',
                        duration: 1500
                    })
                },
                // onError 回调：发生错误时触发
                (error) => {
                    console.error('AI流式生成失败:', error)
                    throw error
                }
            )

        } catch (e) {
            stopLoadingAnimation()
            uni.hideToast()
            console.error('AI生成失败:', e)
            uni.showToast({
                title: e.message || 'AI生成失败，请重试',
                icon: 'none',
                duration: 2000
            })
        } finally {
            generating.value = false
        }
    }

    return {
        generating,
        aiLoadingText,
        handleMagicGenerate
    }
}
