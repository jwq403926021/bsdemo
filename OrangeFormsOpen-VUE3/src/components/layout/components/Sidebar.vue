<template>
  <div style="position: relative; height: 100%" class="sidebar-bg">
    <multiColumn v-if="layoutStore.supportColumn" :menuList="layoutStore.menuList" />
    <div
      v-else
      class="left-menu"
      :class="layoutStore.collapsed ? 'collapse' : ''"
      style="height: 100%; padding-top: 16px; padding-bottom: 16px"
    >
      <el-scrollbar wrap-class="scrollbar_dropdown__wrap" style="height: 100%">
        <el-menu
          mode="vertical"
          active-text-color="#ffd04b"
          text-color="#1a457a"
          :default-active="layoutStore.currentMenuId"
          :unique-opened="true"
          @select="selectMenuById"
          :collapse="layoutStore.collapsed"
        >
          <template v-for="menu in layoutStore.menuList" :key="menu.menuId">
            <sub-menu :menu="menu" />
          </template>
        </el-menu>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLayoutStore } from '@/store';
import SubMenu from './SubMenu.vue';
import multiColumn from './multi-column.vue';
import { useSelectMenu } from './hooks';

const layoutStore = useLayoutStore();
const { selectMenuById } = useSelectMenu();
</script>

<style lang="scss">
.sidebar-title-text {
  font-size: 18px;
  font-weight: bold;
}
.left-menu .el-submenu__title {
  height: 50px;
  line-height: 50px;
  .el-submenu__icon-arrow {
    margin-top: -4px;
  }
}
.collapse .is-active .el-submenu__title {
  position: relative;
  background-color: #43474e !important;
  &::before {
    position: absolute;
    top: 0;
    left: 0;
    display: block;
    width: 4px;
    height: 100%;
    background: $color-primary;
    content: '';
  }
}
.collapse .el-icon-arrow-right {
  display: none;
}
.el-menu-item.is-active {
  color: $color-white !important;
  background-color: $color-primary !important;
  border-radius: 4px;
  &::before {
    position: absolute;
    top: 0;
    left: 0;
    display: block;
    width: 4px;
    height: 100%;
    background: $color-primary !important;
    content: '';
  }
  i {
    color: white !important;
  }
}
.el-menu--vertical .el-submenu__title {
  & > span {
    display: inline !important;
  }
}
.el-menu--vertical .el-menu.el-menu--popup {
  padding: 16px 8px;
  background-color: white !important;
  .el-menu-item,
  .el-submenu__title {
    height: 40px;
    padding: 0 3px !important;
    color: #333;
    line-height: 40px;
    &::before {
      display: none;
    }
    &:hover {
      background-color: #f6f6f6 !important;
      & > .multi-column-menu-popover {
        display: block;
      }
    }
    &.is-active {
      color: $color-primary !important;
      background-color: $color-primary-light-9 !important;
    }
    .el-submenu__icon-arrow {
      margin-top: -5px;
      color: #333;
    }
  }
  .el-menu--vertical {
    transform: translateX(6px);
  }
}
.sidebar-bg .el-submenu {
  .el-menu {
    background-color: #1d1f24 !important;
  }
}
</style>
