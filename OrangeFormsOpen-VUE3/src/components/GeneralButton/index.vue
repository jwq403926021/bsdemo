<template>
  <el-button
    :size="size"
    :style="computedStyle"
    :disabled="disabled"
    class="custom-button"
    @click="onClick"
  >
    {{ text }}
  </el-button>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    text: string;
    type?: string;
    size?: string;
    disabled?: boolean;
    style?: Record<string, string | number>;
  }>(),
  {
    type: 'primary', // 'primary' | 'success' | 'warning' | 'danger'
    size: 'default', // 'large' | 'default' | 'small'
    disabled: false,
    style: () => ({}),
  },
);

let btnColor =
  props.type === 'success'
    ? '#67C23A'
    : props.type === 'warning'
    ? '#FF9900'
    : props.type === 'danger'
    ? '#F56C6C'
    : '#1a457a';

const buttonStyle = {
  borderColor: btnColor,
  color: btnColor,
};

const computedStyle = computed(() => ({
  ...buttonStyle,
  ...props.style, // Merge user-defined styles
}));

// Emits to propagate events to parent
const emit = defineEmits(['btnClick']);

const onClick = (page: number) => {
  emit('btnClick', page); // Emit the event to parent
};
</script>

<style lang="scss" scoped>
.custom-button {
  border: 1px solid;
  border-radius: 10px;
  padding: 4px 16px;
}
</style>
