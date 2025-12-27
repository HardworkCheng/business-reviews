import request from './request'

// 获取评论列表
export function getCommentList(params: any) {
  return request.get('/merchant/comments', { params })
}

// 获取笔记评论列表
export function getNoteComments(noteId: number, params?: any) {
  return request.get(`/merchant/notes/${noteId}/comments`, { params })
}

// 创建笔记评论
export function createNoteComment(noteId: number, data: { content: string; parentId?: number | null }) {
  return request.post(`/merchant/notes/${noteId}/comments`, data)
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

// 获取评论数据概览
export function getCommentDashboard(params?: any) {
  return request.get('/merchant/comments/dashboard', { params })
}

// 置顶评论
export function topComment(id: number, isTop: boolean) {
  return request.put(`/merchant/comments/${id}/top`, { isTop })
}

// 导出评论数据
export function exportComments(params: any) {
  return request.get('/merchant/comments/export', { 
    params,
    responseType: 'blob'
  })
}

// 获取商家门店列表
export function getMerchantShops() {
  return request.get('/merchant/shops')
}
