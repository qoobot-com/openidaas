<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item,index) in levelList" :key="item.path">
        <span v-if="item.redirect==='noRedirect'||index==levelList.length-1" class="no-redirect">
          {{ item.meta?.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">
          {{ item.meta?.title }}
        </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter, RouteLocationMatched } from 'vue-router'

interface BreadcrumbItem {
  path: string
  meta?: {
    title?: string
    breadcrumb?: boolean
  }
  redirect?: string
}

const route = useRoute()
const router = useRouter()
const levelList = ref<BreadcrumbItem[]>([])

const getBreadcrumb = () => {
  // only show routes with meta.title
  let matched: RouteLocationMatched[] = route.matched.filter(item => item.meta && item.meta.title)
  const first = matched[0]

  if (!isDashboard(first)) {
    matched = [{ path: '/dashboard', meta: { title: '首页' } }, ...matched] as RouteLocationMatched[]
  }

  levelList.value = matched
    .filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
    .map(item => ({
      path: item.path,
      meta: item.meta as BreadcrumbItem['meta'],
      redirect: item.redirect as string
    }))
}

const isDashboard = (routeItem: RouteLocationMatched | undefined) => {
  const name = routeItem?.name
  if (!name) {
    return false
  }
  return String(name).trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
}

const handleLink = (item: BreadcrumbItem) => {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watch(route, () => {
  getBreadcrumb()
}, { immediate: true })
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>