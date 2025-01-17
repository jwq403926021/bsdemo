import { SysCustomWidgetType, OnlineFormEventType } from '@/common/staticDict/index';

const bsShippingOrderInfo = {
  span: {
    name: 'Span',
    widgetType: SysCustomWidgetType.Slider,
    value: 24,
    visible: false,
    min: 1,
    max: 24,
  },
  activeStep: {
    name: 'Active Step',
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
