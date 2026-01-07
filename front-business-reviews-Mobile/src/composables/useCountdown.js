import { ref, onMounted, onUnmounted } from 'vue'

export function useCountdown() {
    const countdown = ref({ hours: '00', minutes: '00', seconds: '00' })
    let timer = null

    const updateCountdown = (endTime) => {
        if (!endTime) {
            countdown.value = { hours: '00', minutes: '00', seconds: '00' }
            return
        }

        const now = new Date().getTime()
        const end = new Date(endTime).getTime()
        const diff = Math.max(0, end - now)

        const hours = Math.floor(diff / (1000 * 60 * 60))
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
        const seconds = Math.floor((diff % (1000 * 60)) / 1000)

        countdown.value = {
            hours: String(hours).padStart(2, '0'),
            minutes: String(minutes).padStart(2, '0'),
            seconds: String(seconds).padStart(2, '0')
        }
    }

    const startCountdown = (getEndTime) => {
        // Initial update
        updateCountdown(getEndTime())

        timer = setInterval(() => {
            updateCountdown(getEndTime())
        }, 1000)
    }

    const stopCountdown = () => {
        if (timer) {
            clearInterval(timer)
            timer = null
        }
    }

    onUnmounted(() => {
        stopCountdown()
    })

    return {
        countdown,
        startCountdown,
        stopCountdown
    }
}
