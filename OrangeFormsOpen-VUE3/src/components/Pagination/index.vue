<template>
  <el-row type="flex" :justify="justify" style="margin-top: 16px">
    <el-pagination
      class="custom-pagination"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :layout="layout"
      :size="size"
      @current-change="onCurrentChange"
      @size-change="onSizeChange"
    >
    </el-pagination>
  </el-row>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    total: number;
    currentPage: number;
    pageSize: number;
    layout?: string;
    size?: string;
    justify?: string;
    // onCurrentChange: (page: number) => void;
    // onSizeChange: (size: number) => void;
  }>(),
  {
    layout: 'total, prev, pager, next, sizes',
    size: 'default',
    justify: 'start',
  },
);

// Emits to propagate events to parent
const emit = defineEmits(['currentChange', 'sizeChange']);

const onCurrentChange = (page: number) => {
  emit('currentChange', page); // Emit the event to parent
};

const onSizeChange = (size: number) => {
  emit('sizeChange', size); // Emit the event to parent
};
</script>

<style scoped></style>
