import request from '@/utils/request'
import type { ApiResponse, UploadResult } from '@/types'

/** 上传聊天文件（图片/视频/文件），type 为 image/video/file */
export const uploadFile = (file: File, type: string): Promise<ApiResponse<UploadResult>> => {
  const form = new FormData()
  form.append('file', file)
  return request.post<UploadResult>(`/files/upload?type=${encodeURIComponent(type)}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}