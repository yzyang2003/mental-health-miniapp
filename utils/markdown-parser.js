/**
 * 轻量级Markdown解析器
 * 支持：标题、粗体、斜体、列表、引用、行内代码、换行
 */

function parseMarkdown(text) {
  if (!text) return '';

  const lines = text.split('\n');
  const result = [];
  let inList = false;
  let listType = '';

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i];

    // 标题
    if (line.startsWith('### ')) {
      result.push({ type: 'h3', content: parseInline(line.slice(4)) });
      inList = false;
      continue;
    }
    if (line.startsWith('## ')) {
      result.push({ type: 'h2', content: parseInline(line.slice(3)) });
      inList = false;
      continue;
    }
    if (line.startsWith('# ')) {
      result.push({ type: 'h1', content: parseInline(line.slice(2)) });
      inList = false;
      continue;
    }

    // 引用
    if (line.startsWith('> ')) {
      result.push({ type: 'quote', content: parseInline(line.slice(2)) });
      inList = false;
      continue;
    }

    // 无序列表
    if (line.match(/^[-*]\s/)) {
      if (!inList || listType !== 'ul') {
        result.push({ type: 'ul-start' });
        inList = true;
        listType = 'ul';
      }
      result.push({ type: 'li', content: parseInline(line.slice(2)) });
      continue;
    }

    // 有序列表
    if (line.match(/^\d+\.\s/)) {
      if (!inList || listType !== 'ol') {
        result.push({ type: 'ol-start' });
        inList = true;
        listType = 'ol';
      }
      const content = line.replace(/^\d+\.\s/, '');
      result.push({ type: 'li', content: parseInline(content) });
      continue;
    }

    // 空行
    if (line.trim() === '') {
      if (inList) {
        result.push({ type: listType + '-end' });
        inList = false;
      }
      result.push({ type: 'br' });
      continue;
    }

    // 普通文本
    if (inList) {
      result.push({ type: listType + '-end' });
      inList = false;
    }
    result.push({ type: 'p', content: parseInline(line) });
  }

  if (inList) {
    result.push({ type: listType + '-end' });
  }

  return result;
}

function parseInline(text) {
  if (!text) return '';

  // 行内代码
  text = text.replace(/`([^`]+)`/g, '<text class="inline-code">$1</text>');

  // 粗体
  text = text.replace(/\*\*([^*]+)\*\*/g, '<text class="bold">$1</text>');

  // 斜体
  text = text.replace(/\*([^*]+)\*/g, '<text class="italic">$1</text>');

  return text;
}

module.exports = {
  parseMarkdown
}
