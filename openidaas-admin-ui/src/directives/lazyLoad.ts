import { DirectiveBinding } from 'vue'

interface LazyLoadElement extends HTMLElement {
  _lazyLoadObserver?: IntersectionObserver
  _lazyLoadSrc?: string
}

const defaultPlaceholder =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5Ij7nrb7ljYHnn6Xor4HkvYw8L3RleHQ+PC9zdmc+'

const loadImage = (el: LazyLoadElement, src: string) => {
  const img = new Image()
  img.onload = () => {
    el.src = src
    el.classList.add('lazy-loaded')
  }
  img.onerror = () => {
    el.classList.add('lazy-error')
  }
  img.src = src
}

const lazyLoad = {
  mounted(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    const src = binding.value || defaultPlaceholder
    el._lazyLoadSrc = src

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            loadImage(el, src)
            observer.unobserve(el)
          }
        })
      },
      {
        rootMargin: '100px',
        threshold: 0.1
      }
    )

    el._lazyLoadObserver = observer
    observer.observe(el)
  },

  updated(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    if (binding.value !== binding.oldValue) {
      const src = binding.value || defaultPlaceholder
      el._lazyLoadSrc = src

      if (el._lazyLoadObserver) {
        el._lazyLoadObserver.unobserve(el)
      }

      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              loadImage(el, src)
              observer.unobserve(el)
            }
          })
        },
        {
          rootMargin: '100px',
          threshold: 0.1
        }
      )

      el._lazyLoadObserver = observer
      observer.observe(el)
    }
  },

  unmounted(el: LazyLoadElement) {
    if (el._lazyLoadObserver) {
      el._lazyLoadObserver.disconnect()
    }
  }
}

export default lazyLoad
