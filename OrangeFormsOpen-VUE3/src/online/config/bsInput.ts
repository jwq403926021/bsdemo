import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsInput = {
  span: {
    name: '组件宽度',
    widgetType: SysCustomWidgetType.Slider,
    value: 24,
    visible: function (formConfig: ANY_OBJECT) {
      return (
        formConfig &&
        formConfig.form.formType !== SysOnlineFormType.QUERY &&
        formConfig.activeMode === 'pc'
      );
    },
    disabled: false,
    min: 1,
    max: 24,
  },
  placeholder: {
    name: '占位文本',
    widgetType: SysCustomWidgetType.Input,
    value: '',
  },
  activeStep: {
    name: '归属',
    visible: false,
    value: ''
  }
};

const bsInputConfig = {
  widgetType: SysCustomWidgetType.Input,
  icon: 'online-icon icon-input',
  attribute: bsInput,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsInputConfig;
