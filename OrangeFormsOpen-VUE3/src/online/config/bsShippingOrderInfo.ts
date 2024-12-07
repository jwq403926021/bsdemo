import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsShippingOrderInfo = {
  span: {
    name: '组件宽度',
    widgetType: SysCustomWidgetType.Slider,
    value: 24,
    visible: false,
    min: 1,
    max: 24,
  },
  activeStep: {
    name: '归属',
    widgetType: SysCustomWidgetType.NumberInput,
    visible: false,
    value: '',
  },
};

const bsShippingOrderInfoConfig = {
  widgetType: SysCustomWidgetType.BsShippingOrderInfo,
  icon: 'online-icon icon-dept',
  attribute: bsShippingOrderInfo,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsShippingOrderInfoConfig;
