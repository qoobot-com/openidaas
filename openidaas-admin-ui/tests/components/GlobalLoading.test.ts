import { describe, it, expect, vi } from 'vitest'
import { render } from '@vue/test-utils'
import GlobalLoading from '@/components/GlobalLoading.vue'

describe('GlobalLoading.vue', () => {
  it('should render loading state', () => {
    const wrapper = render(GlobalLoading)
    
    expect(wrapper.html()).toBeTruthy()
  })

  it('should render with custom text', () => {
    const wrapper = render(GlobalLoading, {
      props: {
        text: '加载中...'
      }
    })
    
    expect(wrapper.html()).toBeTruthy()
  })

  it('should render with fullscreen prop', () => {
    const wrapper = render(GlobalLoading, {
      props: {
        fullscreen: true
      }
    })
    
    expect(wrapper.html()).toBeTruthy()
  })
})
