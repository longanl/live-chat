/**
 * 全局常量。
 *
 * 说明：内置群会话 ID（后端 chat.sql 固定为 1）已不再由前端硬编码——
 * 群会话一律来自 `/conversations` 接口与消息体中的 conversationId，
 * 因此这里只保留与后端分页约定相关的常量。
 */

/** 历史消息每页条数：前端判定"是否加载到底"的唯一依据 */
export const HISTORY_PAGE_SIZE = 30
