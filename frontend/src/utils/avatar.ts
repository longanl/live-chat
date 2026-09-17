/**
 * 头像兜底：无头像时用「昵称首字符 + 稳定背景色」，避免所有用户长得一样。
 */

/** 稳定色板（与品牌色协调的低饱和色） */
const COLORS = [
  '#2b79f5',
  '#12b7a8',
  '#7a5af8',
  '#f2994a',
  '#eb5757',
  '#27ae60',
  '#2d9cdb',
  '#9b51e0'
];

/** 按名称生成稳定的展示字符（中文取首字，英文取首字母大写） */
export function avatarText(name?: string): string {
  const text = (name ?? '').trim();
  if (!text) return '?';
  return text.slice(0, 1).toUpperCase();
}

/** 按名称生成稳定的背景色（同名同色） */
export function avatarColor(name?: string): string {
  const text = (name ?? '').trim();
  if (!text) return COLORS[0];
  let hash = 0;
  for (let i = 0; i < text.length; i += 1) {
    hash = (hash * 31 + text.charCodeAt(i)) % 100000;
  }
  return COLORS[hash % COLORS.length];
}