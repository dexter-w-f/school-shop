/**
 * 极简 HTML 净化工具（无第三方依赖）。
 *
 * 用于 v-html 渲染「用户可控」内容（论坛帖子等）时降低 XSS 风险。
 * 采用「白名单之外一律丢弃」的策略：只保留安全标签与安全属性，
 * 并清洗 javascript: 等危险协议。
 *
 * 注意：这是防御性兜底，不是完整的 HTML 净化器。富文本编辑器内容
 * （商品详情，仅管理员可写）应视为可信来源。
 */

// 允许保留的标签
const ALLOWED_TAGS = new Set([
  'p', 'br', 'hr', 'div', 'span', 'strong', 'b', 'em', 'i', 'u', 's', 'del',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'ul', 'ol', 'li', 'blockquote', 'pre', 'code',
  'table', 'thead', 'tbody', 'tr', 'th', 'td',
  'img', 'a', 'font'
])

// 允许保留的属性（img 的 src、a 的 href 额外做协议校验）
const ALLOWED_ATTRS = new Set(['href', 'src', 'alt', 'title', 'width', 'height', 'style', 'colspan', 'rowspan'])

// 需要连同内容一起丢弃的标签
const DROP_WITH_CONTENT = ['script', 'style', 'iframe', 'object', 'embed', 'noscript', 'template', 'svg', 'math']

const DANGEROUS_PROTOCOL = /^\s*(javascript|vbscript|data:text\/html)/i
const EVENT_ATTR = /^on/i
const SAFE_STYLE = /^\s*(color|background-color|font-size|font-weight|text-align|width|height|margin|padding)\s*:/i

function sanitizeStyle(value) {
  return String(value)
    .split(';')
    .map(s => s.trim())
    .filter(s => s && SAFE_STYLE.test(s) && !/expression|url\s*\(/i.test(s))
    .join('; ')
}

/**
 * 净化 HTML 字符串。
 * @param {string} html 原始 HTML
 * @returns {string} 净化后的 HTML
 */
export function sanitizeHtml(html) {
  if (html === null || html === undefined) return ''
  let out = String(html)

  // 1. 先去注释，避免注释里拼接出标签
  out = out.replace(/<!--[\s\S]*?-->/g, '')

  // 2. 丢弃危险标签及其内容
  for (const tag of DROP_WITH_CONTENT) {
    out = out.replace(new RegExp(`<${tag}\\b[\\s\\S]*?<\\/${tag}\\s*>`, 'gi'), '')
    out = out.replace(new RegExp(`<${tag}\\b[^>]*\\/?>`, 'gi'), '')
  }

  // 3. 逐标签按白名单过滤
  out = out.replace(/<\/?([a-zA-Z][a-zA-Z0-9]*)((?:[^>"']|"[^"]*"|'[^']*')*)\/?>/g, (match, rawTag, rawAttrs) => {
    const tag = rawTag.toLowerCase()
    if (!ALLOWED_TAGS.has(tag)) return ''

    const isClosing = /^<\//.test(match)
    if (isClosing) return `</${tag}>`

    // 解析属性
    const attrs = []
    const attrRe = /([a-zA-Z_:][-a-zA-Z0-9_:.]*)\s*=\s*("[^"]*"|'[^']*'|[^\s"'>]+)/g
    let m
    while ((m = attrRe.exec(rawAttrs)) !== null) {
      const name = m[1].toLowerCase()
      let value = m[2].replace(/^["']|["']$/g, '')
      if (!ALLOWED_ATTRS.has(name)) continue
      if (EVENT_ATTR.test(name)) continue
      if (name === 'style') {
        const cleaned = sanitizeStyle(value)
        if (cleaned) attrs.push(`style="${cleaned}"`)
        continue
      }
      if (name === 'href' || name === 'src') {
        if (DANGEROUS_PROTOCOL.test(value)) continue
      }
      // 转义引号，避免属性逃逸
      attrs.push(`${name}="${value.replace(/"/g, '&quot;')}"`)
    }

    const selfClosing = tag === 'br' || tag === 'hr' || tag === 'img'
    return `<${tag}${attrs.length ? ' ' + attrs.join(' ') : ''}${selfClosing ? ' /' : ''}>`
  })

  return out
}

export default sanitizeHtml
