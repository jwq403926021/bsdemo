<template>
  <el-row :class="isFold ? 'flex-box-flod' : 'flex-box'" type="flex">
    <el-col :span="17" class="filter-cond">
      <slot />
    </el-col>
    <el-col :span="7" :class="isFold ? 'handling-box-flod' : 'handling-box'" v-if="hasSearch" :style="{ 'min-width': minMenuWidth + 'px' }">
        <el-button class="broder-radius-16" type="primary" @click="search" >
          Search
        </el-button>
        <el-button v-if="hasFold" class="broder-radius-16" type="primary" @click="onFold">
            <img :src="FoldDown" alt="icon" :class="isFold ? 'fold-up-icon' : 'fold-down-icon'" />
        </el-button>
        <el-button type="text" plain>
          <img :src="Separator" alt="icon" class="separator-icon" />
        </el-button>
        <el-button v-if="hasReset" type="text" plain @click="reset">
          <img :src="Reload" alt="icon" class="icon-button" />
        </el-button>
        <el-button v-if="hasRefresh" type="text" plain @click="search">
          <img :src="Refresh" alt="icon" class="icon-button" />
        </el-button>
        <el-button v-if="hasDownload" type="text" plain @click="">
          <img :src="DownLoad" alt="icon" class="icon-button" />
        </el-button>
        <div style="float: right">
          <slot name="operation" />
        </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import FoldDown from '@/assets/img/fold_down.png';
import Separator from '@/assets/img/separator.png';
import Reload from '@/assets/img/reload.png';
import Refresh from '@/assets/img/refresh3.png';
import DownLoad from '@/assets/img/download_xls.png';

const emit = defineEmits<{
  (e: 'reset'): void;
  (e: 'search'): void;
}>();

withDefaults(
  defineProps<{
    hasSearch?: boolean;
    hasReset?: boolean;
    hasFold?: boolean;
    hasRefresh?: boolean;
    hasDownload?: boolean;
    minMenuWidth?: number;
  }>(),
  {
    hasSearch: true,
    hasReset: true,
    hasFold: false,
    hasRefresh: false,
    hasDownload: false,
    // 这个值在size为default时会导致某些页面按钮折行
    minMenuWidth: 200,
  },
);

let isFold = ref(false)

const search = () => {
  emit('search');
};
const reset = () => {
  emit('reset');
};
const onFold = () => {
  isFold.value = !isFold.value
};
</script>

<style lang="scss" scoped>
.flex-box {
  padding: 16px 24px 16px;
  background-color: white;
}
.flex-box-flod {
  height: 112px;
  overflow: hidden;
  padding: 16px 24px 16px;
  background-color: white;
}
.filter-cond {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.handling-box, .handling-box-flod{
  display:flex;
  justify-content: flex-end;
  align-items: flex-end;
  flex-shrink: 0;
  margin-bottom: 16px;
  gap: 5px;
  flex-wrap: wrap;
}
.handling-box-flod{
  height: 64px;
  margin-bottom: 0px;
}
.fold-down-icon{
  width: 20px;
  height: 20px;
  transform: rotate(0deg);
  transition: transform 0.3s ease-in-out;
}
.fold-up-icon{
  width: 20px;
  height: 20px;
  transform: rotate(180deg);
  transition: transform 0.3s ease-in-out;
}
.separator-icon{
  height: 29px;
}
.icon-button {
  width: 29px;
  height: 29px;
}
:deep(.el-form-item) {
  margin-right: 8px;
  margin-bottom: 16px;
}
.extend-box {
  img {
    cursor: pointer;
    margin-left: 8px;
  }
}


</style>
