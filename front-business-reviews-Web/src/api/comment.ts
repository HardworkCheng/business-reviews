import request from './request'

// 获取评论列表
export function getCommentList(params: any) {
  return request.get('/merchant/comments', { params })
}

// 回复评论
export function replyComment(id: number, data: { content: string }) {
  return request.post(`/merchant/comments/${id}/reply`, data)
}

// 删除评论
export function deleteComment(id: number) {
  return request.delete(`/merchant/comments/${id}`)
}

// 获取评论统计
export function getCommentStats() {
  return request.get('/merchant/comments/stats')
}