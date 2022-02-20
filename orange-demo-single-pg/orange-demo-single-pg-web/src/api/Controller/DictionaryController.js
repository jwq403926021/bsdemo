import * as staticDict from '@/staticDict'

export default class DictionaryController {
  static dictSysRole (sender, params, axiosOption, httpOption) {
    return new Promise((resolve, reject) => {
      sender.doUrl('/admin/upms/sysRole/listDict', 'get', params, axiosOption, httpOption).then(res => {
        let dictData = new staticDict.DictionaryBase('角色字典');
        dictData.setList(res.data);
        resolve(dictData);
      }).catch(err => {
        reject(err);
      });
    });
  }
  static dictSysUserStatus () {
    return new Promise((resolve) => {
      resolve(staticDict.SysUserStatus);
    });
  }
  static dictSysUserType () {
    return new Promise((resolve) => {
      resolve(staticDict.SysUserType);
    });
  }
  static dictSysDept (sender, params, axiosOption, httpOption) {
    return new Promise((resolve, reject) => {
      sender.doUrl('/admin/upms/sysDept/listDict', 'get', params, axiosOption, httpOption).then(res => {
        let dictData = new staticDict.DictionaryBase('部门字典');
        dictData.setList(res.data);
        resolve(dictData);
      }).catch(err => {
        reject(err);
      });
    });
  }
  static dictSysDeptByParentId (sender, params, axiosOption, httpOption) {
    return new Promise((resolve, reject) => {
      sender.doUrl('/admin/upms/sysDept/listDictByParentId', 'get', params, axiosOption, httpOption).then(res => {
        let dictData = new staticDict.DictionaryBase('部门字典');
        dictData.setList(res.data);
        resolve(dictData);
      }).catch(err => {
        reject(err);
      });
    });
  }
  static dictSysDataPermType () {
    return new Promise((resolve) => {
      resolve(staticDict.SysDataPermType);
    });
  }
}
