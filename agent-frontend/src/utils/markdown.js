/**
 * 简单的Markdown渲染工具
 */

export function renderMarkdown(text) {
  if (!text) return ''
  
  let html = text
  
  // 转义HTML特殊字符（除了已经是HTML的部分）
  html = html
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 代码块 ```code```
  html = html.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre><code class="language-${lang || 'text'}">${code.trim()}</code></pre>`
  })
  
  // 行内代码 `code`
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // 分隔线 --- 或 ***
  html = html.replace(/^[\-*]{3,}\s*$/gim, '<hr>')
  
  // 标题（保留换行）
  html = html.replace(/^### (.*)$/gim, '<h3>$1</h3>\n')
  html = html.replace(/^## (.*)$/gim, '<h2>$1</h2>\n')
  html = html.replace(/^# (.*)$/gim, '<h1>$1</h1>\n')
  
  // 粗体 **text**
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // 斜体 *text*（避免匹配到列表标记）
  html = html.replace(/(?<!\*)\*(?!\*)([^*]+)\*(?!\*)/g, '<em>$1</em>')
  
  // 删除线 ~~text~~
  html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>')
  
  // 处理列表（改进版）
  const lines = html.split('\n')
  const processed = []
  let inList = false
  let listType = null
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    const unorderedMatch = line.match(/^\s*[-*+]\s+(.*)$/)
    const orderedMatch = line.match(/^\s*\d+\.\s+(.*)$/)
    
    if (unorderedMatch) {
      if (!inList || listType !== 'ul') {
        if (inList) processed.push(`</${listType}>`)
        processed.push('<ul>')
        listType = 'ul'
        inList = true
      }
      processed.push(`<li>${unorderedMatch[1]}</li>`)
    } else if (orderedMatch) {
      if (!inList || listType !== 'ol') {
        if (inList) processed.push(`</${listType}>`)
        processed.push('<ol>')
        listType = 'ol'
        inList = true
      }
      processed.push(`<li>${orderedMatch[1]}</li>`)
    } else {
      if (inList) {
        processed.push(`</${listType}>`)
        inList = false
        listType = null
      }
      processed.push(line)
    }
  }
  
  if (inList) {
    processed.push(`</${listType}>`)
  }
  
  html = processed.join('\n')
  
  // 图片 ![alt](url) - 必须在链接之前处理，因为图片语法包含链接语法
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="markdown-image" loading="lazy" />')
  
  // 链接 [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  
  // 换行处理：双换行变成段落分隔，单换行变成 <br>
  html = html.replace(/\n\n+/g, '</p><p>')
  html = html.replace(/\n/g, '<br>')
  
  // 包裹段落
  if (!html.startsWith('<h') && !html.startsWith('<ul') && !html.startsWith('<ol') && !html.startsWith('<pre')) {
    html = '<p>' + html + '</p>'
  }
  
  return html
}

