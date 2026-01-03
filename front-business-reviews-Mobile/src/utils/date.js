/**
 * 格式化相对时间
 * @param {String|Number|Date} timeStr 时间
 * @returns {String} 相对时间字符串
 */
export const formatTime = (timeStr) => {
    if (!timeStr) return ''

    try {
        let date;
        if (typeof timeStr === 'string') {
            // 解决IOS无法识别连字符问题
            date = new Date(timeStr.replace(/-/g, '/'))
        } else {
            date = new Date(timeStr)
        }

        const now = new Date()
        const diff = now - date
        const minutes = Math.floor(diff / 60000)
        const hours = Math.floor(diff / 3600000)
        const days = Math.floor(diff / 86400000)

        if (minutes < 1) return '刚刚'
        if (minutes < 60) return `${minutes}分钟前`
        if (hours < 24) return `${hours}小时前`
        if (days < 30) return `${days}天前`
        // 超过30天显示具体日期
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    } catch (e) {
        return String(timeStr)
    }
}

export default {
    formatTime
}
