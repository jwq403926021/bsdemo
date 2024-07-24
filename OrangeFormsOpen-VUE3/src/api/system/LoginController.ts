import { loginParam, LoginUserInfo } from '@/types/upms/login';
import { UserInfo } from '@/types/upms/user';
import { BaseController } from '@/api/BaseController';
import { API_CONTEXT } from '../config';

export default class LoginController extends BaseController {
  static login(params: loginParam) {
    return super.post<UserInfo>(API_CONTEXT + '/upms/login/doLogin', params);
  }

  static logout() {
    return super.post(API_CONTEXT + '/upms/login/doLogout', {});
  }
}
