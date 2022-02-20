<template>
  <el-popover trigger="click" width="300px" @show="onInit" v-model="isShow">
    <el-form ref="dictData" :model="formData" class="full-width-input" :rules="rules" style="width: 100%;"
      label-width="100px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
      <el-form-item label="字典键类型">
        <el-radio-group v-model="formData.type" :disabled="value != null">
          <el-radio-button label="Integer">整数</el-radio-button>
          <el-radio-button label="String">字符串</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type === 'String'" label="字典键数据" prop="id">
        <el-input v-model="formData.id" />
      </el-form-item>
      <el-form-item v-if="formData.type === 'Integer'" label="字典键数据" prop="id">
        <el-input-number v-model="formData.id" />
      </el-form-item>
      <el-form-item label="字典值数据" prop="id">
        <el-input v-model="formData.name" />
      </el-form-item>
    </el-form>
    <el-row type="flex" justify="end">
      <el-button :size="defaultFormItemSize" type="primary" :plain="true" @click="onCancel">取消</el-button>
      <el-button :size="defaultFormItemSize" type="primary" @click="onSubmit">保存</el-button>
    </el-row>
    <el-button class="table-btn" :class="{'success': value != null}" slot="reference" :size="defaultFormItemSize" type="text">{{btnText}}</el-button>
  </el-popover>
</template>

<script>
export default {
  props: {
    width: {
      type: String,
      default: '200px'
    },
    btnText: {
      type: String,
      required: true
    },
    value: {
      type: Object
    }
  },
  data () {
    return {
      isShow: false,
      formData: {
        type: 'Integer',
        id: undefined,
        name: undefined
      },
      rules: {
        id: [
          {required: true, message: '字典键数据不能为空', trigger: 'blur'}
        ],
        name: [
          {required: true, message: '字典值数据不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    onCancel () {
      this.isShow = false;
    },
    onSubmit () {
      this.$refs.dictData.validate(valid => {
        if (!valid) return;
        if (this.formData.type === 'Integer') this.formData.id = Number.parseInt(this.formData.id);
        if (this.formData.type === 'String') this.formData.id = this.formData.id.toString();
        this.$emit('save', this.formData, this.value);
        this.isShow = false;
      });
    },
    onInit () {
      this.formData = {
        type: 'Integer',
        id: undefined,
        name: undefined
      }

      if (this.value != null) {
        this.formData = { ...this.value };
      }
    }
  }
}
</script>

<style>
</style>
