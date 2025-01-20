import { BaseController } from '@/api/BaseController';
import { RequestOption } from '@/common/http/types';
import { ANY_OBJECT } from '@/types/generic';
import { TableData } from '@/common/types/table';
import { API_CONTEXT } from '../config';

export default class TaskAndRequestController extends BaseController {
  // 获取 request search bar中的divisions list
  static getDivisions(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.get<ANY_OBJECT[]>('/order/divisions', params, httpOptions);
  }
  // 获取所有my request
  static listMyRequest(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.post<TableData<ANY_OBJECT>>(
      API_CONTEXT + '/myRequest/order/list',
      params,
      httpOptions,
    );
  }
  // 获取单条request order detail
  static getMyRequestOrderDetail(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.get<ANY_OBJECT>(API_CONTEXT + '/myRequest/order/detail', params, httpOptions);
  }
  static getApprovalUserList(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.get<ANY_OBJECT>(API_CONTEXT + '/flow/flowEntry/approvalUserList', params, httpOptions);
  }
  // 获取task search bar中的request type list
  static getRequestTypeList(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.get<TableData<ANY_OBJECT>>('/order/orderType', params, httpOptions);
  }
  // 获取所有 my task
  static listMyTask(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.post<TableData<ANY_OBJECT>>(
      API_CONTEXT + '/order/myTask/listTask',
      params,
      httpOptions,
    );
  }
  // 获取approval recor列表
  static listFlowTaskComment(params: ANY_OBJECT, httpOptions?: RequestOption) {
    return super.get<TableData<ANY_OBJECT>>(
      '/flow/task/comment/listFlowTaskComment',
      params,
      httpOptions,
    );
  }
}
