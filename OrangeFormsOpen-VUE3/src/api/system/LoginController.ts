import { loginParam, LoginUserInfo } from '@/types/upms/login';
import { UserInfo } from '@/types/upms/user';
import { BaseController } from '@/api/BaseController';
import { API_CONTEXT } from '../config';

export default class LoginController extends BaseController {
  static login(params: loginParam) {
    return this.post<UserInfo>(API_CONTEXT + '/upms/login/doLogin', params);
  }

  static logout() {
    return this.post(API_CONTEXT + '/upms/login/doLogout', {});
  }
}
