<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <Sidebar class="sidebar-container" />
    <div :class="{hasTagsView:needTagsView}" class="main-container">
      <div :class="{'fixed-header':fixedHeader}">
        <Navbar />
        <TagsView v-if="needTagsView" />
      </div>
      <AppMain />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TagsView from './TagsView.vue'
import AppMain from './AppMain.vue'

const appStore = useAppStore()

const device = computed(() => appStore.device)
const sidebar = computed(() => appStore.sidebar)
const needTagsView = computed(() => true)
const fixedHeader = computed(() => true)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const handleClickOutside = () => {
  appStore.closeSideBar(false)
}

// 监听屏幕尺寸变化
watchEffect(() => {
  const resizeHandler = () => {
    if (document.body.clientWidth <= 992) {
      appStore.toggleDevice('mobile')
      appStore.closeSideBar(true)
    } else {
      appStore.toggleDevice('desktop')
    }
  }

  window.addEventListener('resize', resizeHandler)
  resizeHandler()

  return () => {
    window.removeEventListener('resize', resizeHandler)
  }
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  &:after {
    content: "";
    display: table;
    clear: both;
  }
  
  position: relative;
  height: 100%;
  width: 100%;
  
  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{$sideBarWidth});
  transition: width 0.28s;
}

.hideSidebar {
  .fixed-header {
    width: calc(100% - 54px);
  }
}

.mobile {
  .fixed-header {
    width: 100%;
  }
}
</style>