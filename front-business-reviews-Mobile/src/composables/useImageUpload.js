import { ref } from 'vue'
import { uploadImages as apiUploadImages } from '../api/upload'

export function useImageUpload() {
    const imageList = ref([]) // 存储临时文件路径
    const uploadedImageUrls = ref([]) // 存储已上传的图片URL

    const chooseImage = () => {
        const remainCount = 9 - imageList.value.length
        if (remainCount <= 0) {
            uni.showToast({
                title: '最多上传9张图片',
                icon: 'none'
            })
            return
        }

        uni.chooseImage({
            count: remainCount,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                imageList.value.push(...res.tempFilePaths)
            }
        })
    }

    const removeImage = (index) => {
        imageList.value.splice(index, 1)
    }

    const uploadImages = async () => {
        if (imageList.value.length === 0) return { urls: [] }

        // Check if already uploaded
        if (uploadedImageUrls.value.length > 0) {
            // Assume if uploadedImageUrls matches imageList length we are good? 
            // Logic in original file was simple check if empty. 
            // For decoupled logic, we can stick to returning cached if exists.
            console.log('使用已缓存的图片URL:', uploadedImageUrls.value)
            return { urls: uploadedImageUrls.value }
        }

        console.log('开始上传图片:', imageList.value.length)
        const uploadResult = await apiUploadImages(imageList.value)
        uploadedImageUrls.value = uploadResult.urls
        console.log('图片上传成功:', uploadedImageUrls.value)
        return uploadResult
    }

    const clearImages = () => {
        imageList.value = []
        uploadedImageUrls.value = []
    }

    return {
        imageList,
        uploadedImageUrls,
        chooseImage,
        removeImage,
        uploadImages,
        clearImages
    }
}
