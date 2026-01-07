<template>
	<!-- 话题输入弹窗 -->
	<view v-if="visible" class="modal-overlay" @click="close">
		<view class="topic-modal-new" @click.stop>
			<view class="modal-header">
				<text class="modal-title">添加话题</text>
				<text class="modal-close" @click="close">×</text>
			</view>
			
			<!-- 输入框 -->
			<view class="topic-input-section">
				<view class="topic-input-wrapper-new">
					<text class="topic-hash-new">#</text>
					<input 
						class="topic-input-new" 
						v-model="topicInput"
						placeholder="输入自定义话题..."
						maxlength="20"
						@confirm="addCustomTopic"
					/>
					<view class="topic-add-btn-new" @click="addCustomTopic">
						<text>添加</text>
					</view>
				</view>
			</view>
			
			<!-- 话题列表 -->
			<scroll-view class="topic-list-scroll" scroll-y>
				<!-- 最近使用 -->
				<view v-if="selectedTopics.length > 0" class="topic-group">
					<view class="topic-group-header">
						<text class="topic-group-icon">🕐</text>
						<text class="topic-group-title">最近使用</text>
					</view>
					<view class="topic-tags-wrapper">
						<view 
							class="topic-tag-item" 
							v-for="(topic, index) in selectedTopics" 
							:key="'selected-' + index"
						>
							<text class="topic-tag-text">#{{ topic.name }}</text>
							<text class="topic-tag-remove" @click.stop="removeTopic(index)">×</text>
						</view>
					</view>
				</view>
				
				<!-- 热门推荐 -->
				<view class="topic-group">
					<view class="topic-group-header">
						<text class="topic-group-icon">🔥</text>
						<text class="topic-group-title">热门推荐</text>
					</view>
					<view class="topic-tags-wrapper">
						<view 
							class="topic-tag-item hot-topic" 
							:class="{ selected: isTopicSelected(topic) }"
							v-for="topic in hotTopics" 
							:key="topic.id"
							@click="toggleHotTopic(topic)"
						>
							<text class="topic-tag-icon" v-if="topic.isHot">🔥</text>
							<text class="topic-tag-text">#{{ topic.name }}</text>
						</view>
					</view>
				</view>
				
				<view style="height: 40rpx;"></view>
			</scroll-view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'
import { getHotTopics } from '../api/common'

const props = defineProps({
	visible: {
		type: Boolean,
		default: false
	},
	modelValue: {
		type: Array,
		default: () => []
	}
})

const emit = defineEmits(['update:visible', 'update:modelValue'])

const selectedTopics = ref(props.modelValue)
const topicInput = ref('')
const hotTopics = ref([])

watch(() => props.visible, (val) => {
	if (val) {
		loadHotTopics()
	}
})

watch(() => props.modelValue, (val) => {
	selectedTopics.value = val
})

// Sync back to parent
watch(selectedTopics, (val) => {
	emit('update:modelValue', val)
}, { deep: true })

const close = () => {
	emit('update:visible', false)
	topicInput.value = ''
}

const loadHotTopics = async () => {
    // Only load if empty or refresh needed. 
	if (hotTopics.value.length === 0) {
		try {
			const topics = await getHotTopics(1, 20)
			if (topics.list && topics.list.length > 0) {
				// 过滤掉已选择的话题
				hotTopics.value = topics.list.filter(
					t => !selectedTopics.value.find(st => st.name === t.name)
				)
			}
		} catch (e) {
			console.error('加载热门话题失败:', e)
		}
	}
}

const addCustomTopic = () => {
	const topicName = topicInput.value.trim()
	
	if (!topicName) {
		uni.showToast({ title: '请输入话题名称', icon: 'none' })
		return
	}
	
	if (topicName.length > 20) {
		uni.showToast({ title: '话题名称不能超过20个字', icon: 'none' })
		return
	}
	
	if (selectedTopics.value.length >= 5) {
		uni.showToast({ title: '最多选择5个话题', icon: 'none' })
		return
	}
	
	// 检查是否已存在
	if (selectedTopics.value.find(t => t.name === topicName)) {
		uni.showToast({ title: '该话题已添加', icon: 'none' })
		return
	}
	
	// 添加自定义话题（id为null表示自定义）
	selectedTopics.value.push({
		id: null,
		name: topicName
	})
	
	topicInput.value = ''
	uni.showToast({ 
		title: `已添加 (${selectedTopics.value.length}/5)`, 
		icon: 'success',
		duration: 1000
	})
}

const removeTopic = (index) => {
	const removed = selectedTopics.value.splice(index, 1)[0]
	
	// 如果是热门话题，重新加入热门列表
	if (removed.id) {
		hotTopics.value.unshift(removed)
	}
	
	uni.showToast({ 
		title: '已移除', 
		icon: 'success',
		duration: 800
	})
}

// 检查话题是否已选中
const isTopicSelected = (topic) => {
	return selectedTopics.value.some(t => t.name === topic.name)
}

// 切换热门话题选中状态
const toggleHotTopic = (topic) => {
	const index = selectedTopics.value.findIndex(t => t.name === topic.name)
	
	if (index >= 0) {
		// 已选中，移除
		selectedTopics.value.splice(index, 1)
		uni.showToast({ 
			title: '已移除', 
			icon: 'success',
			duration: 800
		})
	} else {
		// 未选中，添加
		if (selectedTopics.value.length >= 5) {
			uni.showToast({ title: '最多选择5个话题', icon: 'none' })
			return
		}
		
		selectedTopics.value.push(topic)
		uni.showToast({ 
			title: `已添加 (${selectedTopics.value.length}/5)`, 
			icon: 'success',
			duration: 1000
		})
	}
}
</script>

<style lang="scss" scoped>
// 话题弹窗
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
	z-index: 1000;
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

@keyframes slideUp {
	from { transform: translateY(100%); }
	to { transform: translateY(0); }
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx 40rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.modal-close {
	font-size: 48rpx;
	color: #999;
	line-height: 1;
	padding: 0 10rpx;
}

// 话题弹窗 - 新样式
.topic-modal-new {
	width: 100%;
	max-height: 75vh;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	animation: slideUp 0.3s ease;
	display: flex;
	flex-direction: column;
}

.topic-input-section {
	padding: 20rpx 32rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.topic-input-wrapper-new {
	background: #f7f9fc;
	border-radius: 20rpx;
	padding: 16rpx 24rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.topic-hash-new {
	font-size: 32rpx;
	color: #ff9f43;
	font-weight: bold;
}

.topic-input-new {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	background: transparent;
}

.topic-add-btn-new {
	padding: 8rpx 24rpx;
	background: linear-gradient(135deg, #ffaf40, #ff9f43);
	border-radius: 20rpx;
	
	text {
		font-size: 24rpx;
		color: white;
		font-weight: 500;
	}
	
	&:active {
		opacity: 0.8;
	}
}

.topic-list-scroll {
	flex: 1;
	padding: 0 32rpx 40rpx 32rpx;
	max-height: 60vh;
}

.topic-group {
	margin-top: 30rpx;
}

.topic-group-header {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 20rpx;
}

.topic-group-icon {
	font-size: 28rpx;
}

.topic-group-title {
	font-size: 26rpx;
	color: #b2bec3;
	font-weight: 500;
}

.topic-tags-wrapper {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.topic-tag-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 14rpx 24rpx;
	background: #f7f9fc;
	border: 2rpx solid #e8e8e8;
	border-radius: 28rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.95);
	}
	
	&.hot-topic {
		background: white;
		
		&.selected {
			background: linear-gradient(135deg, #fff5e6 0%, #ffedd5 100%);
			border-color: #ff9f43;
		}
	}
}

.topic-tag-icon {
	font-size: 24rpx;
}

.topic-tag-text {
	font-size: 26rpx;
	color: #666;
	
	.topic-tag-item.selected & {
		color: #ff9f43;
		font-weight: 500;
	}
}

.topic-tag-remove {
	font-size: 28rpx;
	color: #ff8a50;
	font-weight: bold;
	line-height: 1;
	margin-left: 4rpx;
}
</style>
