<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <el-breadcrumb-item :to="{ name: layoutStore.indexName }" :replace="true" @click="hadleClick">
      <div class="breadcrumb-home">
        <img src="@/assets/img/s-home.png" alt="" /><span>Home</span>
      </div>
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in layoutStore.currentMenuPath" :key="item.menuId">
      {{ item.menuName }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script lang="ts" setup>
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
// 返回首页时，设置当前菜单为空
const hadleClick = () => {
  layoutStore.setCurrentMenu(null);
};
</script>

<style lang="scss" rel="stylesheet/scss" scoped>
.breadcrumb-home {
  display: flex;
  align-items: center;
  img {
    width: 20px;
    height: 20px;
    position: relative;
    top: -1px;
    margin-right: 4px;
    margin-left: 1px;
    vertical-align: top;
  }
  span {
    vertical-align: middle;
    color: $color-primary !important;
  }
}
.el-breadcrumb__item {
  display: flex;
  align-items: center;
  span {
    color: #999;
  }
  :deep(.el-breadcrumb__inner) {
    color: $color-primary !important;
  }
  &:last-child :deep(.el-breadcrumb__inner) {
    color: #333;
  }
}
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  line-height: 60px;
  margin-left: 25px;
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
  img {
    vertical-align: middle;
  }
  :deep(.el-breadcrumb__inner.is-link) {
    color: #606266 !important;
    font-weight: normal;
  }
}
</style>
