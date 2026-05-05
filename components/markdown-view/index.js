const { parseMarkdown } = require('../../utils/markdown-parser')

Component({
  properties: {
    content: {
      type: String,
      value: '',
      observer: function(newVal) {
        if (newVal) {
          this.setData({
            nodes: parseMarkdown(newVal)
          });
        }
      }
    }
  },
  data: {
    nodes: []
  }
})
