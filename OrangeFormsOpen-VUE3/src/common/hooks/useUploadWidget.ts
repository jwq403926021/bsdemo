import { UploadFile } from 'element-plus';

export const useUploadWidget = (maxFileCount = 1) => {
  const fileList = ref<UploadFile[]>([]);
  const maxCount = ref(maxFileCount);

  /**
   * 上传文件列表改变
   * @param {Object} uploadFile 改变的文件
   * @param {Array} uploadFiles 改变后的文件列表
   */
  const onFileChange = (uploadFile: UploadFile | null, uploadFiles: UploadFile[] | null) => {
    if (uploadFiles && uploadFiles.length > 0) {
      if (maxFileCount == 1) {
        fileList.value = [uploadFiles[uploadFiles.length - 1]];
      } else {
        fileList.value = uploadFiles;
      }
    } else {
      fileList.value = [];
    }
  };

  return { fileList, onFileChange, maxCount };
};
