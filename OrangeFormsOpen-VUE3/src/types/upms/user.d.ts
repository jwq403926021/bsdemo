export interface OnlineUser {
  deptId: string;
  deviceType: number;
  isAdmin: boolean;
  loginIp: string;
  loginName: string;
  loginTime: string;
  sessionId: string;
  showName: string;
  userId: string;
}

/**
 * 登录返回的用户信息
 */
export type UserInfo = {
  showName: string;
  permCodeList: Array<string>;
  menuList?: Array<MenuItem>;
  userType: number;
  isAdmin: boolean;
  deptName: string;
  tokenData?: string;
  headImageUrl: (string & { downloadUri: string; filename: string }) | null;
};

export interface User {
  userId?: string | number;
  loginName?: string;
  password?: string;
  passwordRepeat?: string;
  showName?: string;
  userRole?: string;
  userType?: number;
  userStatus?: number;
  deptId?: string;
  dataPermIdList?: string[];
  deptPostIdList?: string[];
  roleIdList?: string[];
  sysDataPermUserList?: T[];
  sysUserPostList?: T[];
  sysUserRoleList?: T[];
}

export interface Code {
  id: number;
  countryCode: string;
  groupCode: string;
  groupName: string;
  codeNumber: string;
  english: string;
  korean: string;
  chinese: string;
  japanese: string;
  useStatus: number;
  sort: number | null;
  attr1: string;
  attr1Name: string;
}
