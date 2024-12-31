<template>
  <el-button
    :size="size"
    :style="buttonStyle"
    class="custom-button"
    @click="onClick">
    {{ text }}
  </el-button>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    text: string;
    type?: string;
    size?: string;
  }>(),
  {
    type: "primary",  // 'primary' | 'success' | 'warning'
    size: "default",  // 'large' | 'default' | 'small'
  },
);

let btnColor = props.type === 'success' ? '#67C23A': props.type === 'warning'? '#FF9900' : '#1a457a'

const buttonStyle = {
  borderColor: btnColor,
  color: btnColor,
}

// Emits to propagate events to parent
const emit = defineEmits(['btnClick']);

const onClick = (page: number) => {
  emit('btnClick', page); // Emit the event to parent
};



</script>

<style lang="scss" scoped>
  .custom-button{
    border: 1px solid;
    border-radius: 10px;
    padding: 4px 16px;
  }
</style>
