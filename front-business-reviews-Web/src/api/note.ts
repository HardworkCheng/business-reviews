import request from './request'

// 获取笔记列表
export function getNoteList(params: any) {
  return request.get('/merchant/notes', { params })
}

// 创建笔记
export function createNote(data: any) {
  return request.post('/merchant/notes', data)
}

// 更新笔记
export function updateNote(id: number, data: any) {
  return request.put(`/merchant/notes/${id}`, data)
}

// 删除笔记
export function deleteNote(id: number) {
  return request.delete(`/merchant/notes/${id}`)
}

// 获取笔记详情
export function getNoteDetail(id: number) {
  return request.get(`/merchant/notes/${id}`)
}

// 发布笔记
export function publishNote(id: number) {
  return request.post(`/merchant/notes/${id}/publish`)
}

// 下线笔记
export function offlineNote(id: number) {
  return request.post(`/merchant/notes/${id}/offline`)
}

// 获取笔记统计
export function getNoteStats(id: number) {
  return request.get(`/merchant/notes/${id}/stats`)
}