import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsRecipient = {
  span: {
    name: '组件宽度',
    widgetType: SysCustomWidgetType.Slider,
    value: 13,
    visible: false,
    min: 1,
    max: 24,
  },
  depend: {
    name: '依赖于',
    widgetType: SysCustomWidgetType.Input,
    value: '',
  },
  activeStep: {
    name: '归属',
    widgetType: SysCustomWidgetType.NumberInput,
    visible: false,
    value: '',
  },
  tableName: {
    name: '默认表',
    visible: false,
    value: 'zz_test_order_first',
  },
  columnName: {
    name: '默认列',
    visible: false,
    value: 'recipient',
  },
};

const bsRecipientConfig = {
  widgetType: SysCustomWidgetType.BsRecipient,
  icon: 'online-icon icon-dept',
  attribute: bsRecipient,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsRecipientConfig;
