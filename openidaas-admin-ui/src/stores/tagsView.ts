import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTagsViewStore = defineStore('tagsView', () => {
  // 状态
  const visitedViews = ref<any[]>([])
  const cachedViews = ref<any[]>([])

  // Actions
  const addView = (view: any) => {
    addVisitedView(view)
    addCachedView(view)
  }

  const addVisitedView = (view: any) => {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push(
      Object.assign({}, view, {
        title: view.meta?.title || 'no-name'
      })
    )
  }

  const addCachedView = (view: any) => {
    if (cachedViews.value.includes(view.name)) return
    if (!view.meta?.noCache) {
      cachedViews.value.push(view.name)
    }
  }

  const delView = (view: any) => {
    return new Promise(resolve => {
      delVisitedView(view)
      delCachedView(view)
      resolve({
        visitedViews: visitedViews.value,
        cachedViews: cachedViews.value
      })
    })
  }

  const delVisitedView = (view: any) => {
    return new Promise(resolve => {
      for (const [i, v] of visitedViews.value.entries()) {
        if (v.path === view.path) {
          visitedViews.value.splice(i, 1)
          break
        }
      }
      resolve([...visitedViews.value])
    })
  }

  const delCachedView = (view: any) => {
    return new Promise(resolve => {
      const index = cachedViews.value.indexOf(view.name)
      index > -1 && cachedViews.value.splice(index, 1)
      resolve([...cachedViews.value])
    })
  }

  const delOthersViews = (view: any) => {
    return new Promise(resolve => {
      delOthersVisitedViews(view)
      delOthersCachedViews(view)
      resolve({
        visitedViews: visitedViews.value,
        cachedViews: cachedViews.value
      })
    })
  }

  const delOthersVisitedViews = (view: any) => {
    visitedViews.value = visitedViews.value.filter(v => {
      return v.meta?.affix || v.path === view.path
    })
  }

  const delOthersCachedViews = (view: any) => {
    const index = cachedViews.value.indexOf(view.name)
    if (index > -1) {
      cachedViews.value = cachedViews.value.slice(index, index + 1)
    } else {
      cachedViews.value = []
    }
  }

  const delAllViews = () => {
    return new Promise(resolve => {
      delAllVisitedViews()
      delAllCachedViews()
      resolve({
        visitedViews: visitedViews.value,
        cachedViews: cachedViews.value
      })
    })
  }

  const delAllVisitedViews = () => {
    const affixTags = visitedViews.value.filter(tag => tag.meta?.affix)
    visitedViews.value = affixTags
  }

  const delAllCachedViews = () => {
    cachedViews.value = []
  }

  const updateVisitedView = (view: any) => {
    for (let v of visitedViews.value) {
      if (v.path === view.path) {
        v = Object.assign(v, view)
        break
      }
    }
  }

  return {
    // 状态
    visitedViews,
    cachedViews,
    
    // Actions
    addView,
    addVisitedView,
    addCachedView,
    delView,
    delVisitedView,
    delCachedView,
    delOthersViews,
    delOthersVisitedViews,
    delOthersCachedViews,
    delAllViews,
    delAllVisitedViews,
    delAllCachedViews,
    updateVisitedView
  }
})