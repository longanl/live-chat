/**
 * 时间格式化：列表用紧凑格式，气泡用完整格式，消息流按天插入日期分隔。
 */

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

const pad = (n: number): string => (n < 10 ? `0${n}` : `${n}`);

/** 解析后端返回的时间（LocalDateTime 字符串、时间戳、Date 均可） */
function toDate(value?: string | number | Date): Date | null {
  if (value === undefined || value === null || value === '') return null;
  if (value instanceof Date) return value;
  // 后端形如 2025-07-21T16:15:26，部分浏览器对 T 分隔解析不稳定，统一转空格
  if (typeof value === 'string' && value.includes('T') && !value.endsWith('Z')) {
    const parsed = new Date(value.replace('T', ' '));
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

/** 是否同一天 */
function isSameDay(a: Date, b: Date): boolean {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

/** 会话列表时间：今天 HH:mm / 昨天 / 本周周X / 更早 yyyy/MM/dd */
export function formatListTime(value?: string | number | Date): string {
  const date = toDate(value);
  if (!date) return '';
  const now = new Date();
  if (isSameDay(date, now)) return `${pad(date.getHours())}:${pad(date.getMinutes())}`;

  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (isSameDay(date, yesterday)) return '昨天';

  const diffDays = Math.floor((now.getTime() - date.getTime()) / 86400000);
  if (diffDays < 7) return WEEKDAYS[date.getDay()];

  return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())}`;
}

/** 消息气泡内时间：今天 HH:mm，否则 MM/dd HH:mm */
export function formatBubbleTime(value?: string | number | Date): string {
  const date = toDate(value);
  if (!date) return '';
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  if (isSameDay(date, new Date())) return time;
  return `${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${time}`;
}

/** 消息流日期分隔文案：今天 / 昨天 / 周X / yyyy年M月d日 */
export function formatDayDivider(value?: string | number | Date): string {
  const date = toDate(value);
  if (!date) return '';
  const now = new Date();
  if (isSameDay(date, now)) return '今天';
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (isSameDay(date, yesterday)) return '昨天';
  const diffDays = Math.floor((now.getTime() - date.getTime()) / 86400000);
  if (diffDays < 7) return WEEKDAYS[date.getDay()];
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
}

/** 两个时间是否跨天（决定是否插入日期分隔） */
export function isDifferentDay(
  a?: string | number | Date,
  b?: string | number | Date
): boolean {
  const dateA = toDate(a);
  const dateB = toDate(b);
  if (!dateA || !dateB) return false;
  return !isSameDay(dateA, dateB);
}

/** 资料/群信息用的日期：yyyy-MM-dd */
export function formatDate(value?: string | number | Date): string {
  const date = toDate(value);
  if (!date) return '';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
