<template>
  <component :is="type" v-bind="linkProps(to)">
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { isExternal } from '@/utils/validator'

const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

const isExternalLink = computed(() => isExternal(props.to))

const type = computed(() => {
  if (isExternalLink.value) {
    return 'a'
  }
  return 'router-link'
})

const linkProps = (to: string) => {
  if (isExternalLink.value) {
    return {
      href: to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: to
  }
}
</script>