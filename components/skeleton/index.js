Component({
  properties: {
    /** 骨架条目数组，每项可含 titleWidth / descWidth / lines */
    items: {
      type: Array,
      value: [],
    },
    /** 是否显示头像占位 */
    showAvatar: {
      type: Boolean,
      value: false,
    },
    /** 是否显示描述行 */
    showDesc: {
      type: Boolean,
      value: true,
    },
    /** 布局类型：card | list | chat-left | chat-right | detail */
    type: {
      type: String,
      value: 'card',
    },
  },
})
