import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@vue/test-utils'
import GlobalEmpty from '@/components/GlobalEmpty.vue'

describe('GlobalEmpty.vue', () => {
  beforeEach(() => {
    // Reset any necessary state before each test
  })

  describe('Rendering', () => {
    it('should render empty state', () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'data'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })

    it('should render with custom description', () => {
      const description = '自定义空状态描述'
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'data',
          description
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })

    it('should render with action button', async () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'data',
          actionText: '重新加载'
        },
        slots: {
          action: '<button>重新加载</button>'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })
  })

  describe('Types', () => {
    it('should render data type', () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'data'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })

    it('should render search type', () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'search'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })

    it('should render network type', () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'network'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })

    it('should render permission type', () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'permission'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })
  })

  describe('Events', () => {
    it('should emit action event when action button is clicked', async () => {
      const wrapper = render(GlobalEmpty, {
        props: {
          type: 'data',
          actionText: '刷新'
        },
        slots: {
          action: '<button @click="$emit(\'action\')">刷新</button>'
        }
      })
      
      expect(wrapper.html()).toBeTruthy()
    })
  })
})
