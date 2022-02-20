<template>
  <div class="form-single-fragment" style="position: relative;">
    <el-form ref="formModifyPassword" class="full-width-input" style="width: 100%;"
      label-width="80px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="用户头像">
            <el-upload
              class="upload-image-item" name="uploadFile" :headers="getUploadHeaders"
              :action="headImageUploadUrl"
              :show-file-list="false" accept=".jpg,.png,.jpeg"
              :on-success="onHeadImageUploadSuccess"
              :on-error="onUploadError" :on-exceed="onUploadLimit"
            >
              <img v-if="getHeadImageUrl()" class="upload-image-show" :src="getHeadImageUrl()">
              <i v-else class="el-icon-plus upload-image-item" />
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { SystemController } from '@/api';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin } from '@/core/mixins';

export default {
  name: 'formModifyHeadImage',
  mixins: [uploadMixin, statsDateRangeMixin],
  data () {
    return {
    }
  },
  methods: {
    /**
     * 课程图片上传成功
     */
    onHeadImageUploadSuccess (response, file, fileList) {
      console.log(response, file);
      if (response.success) {
        this.setHeadImage(response.data);
      } else {
        this.$message.error(response.message);
      }
    },
    onUploadError (e, file, fileList) {
      this.$message.error('文件上传失败');
    },
    onUploadLimit (files, fileList) {
      this.$message.error('已经超出最大上传个数限制');
    },
    getHeadImageUrl () {
      if (this.getUserInfo && this.getUserInfo.headImageUrl != null && this.getUserInfo.headImageUrl !== '') {
        let temp = this.getUploadFileUrl(this.getUserInfo.headImageUrl, { filename: this.getUserInfo.headImageUrl.filename });
        return temp;
      } else {
        return null;
      }
    },
    ...mapMutations(['setHeadImage'])
  },
  computed: {
    headImageUploadUrl () {
      return this.getUploadActionUrl(SystemController.changeHeadImageUrl());
    },
    ...mapGetters(['getUserInfo'])
  }
}
</script>

<style>
</style>
