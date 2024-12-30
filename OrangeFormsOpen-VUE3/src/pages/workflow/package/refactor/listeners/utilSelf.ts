// 初始化表单数据
export function initListenerForm(listener) {
  let self = {
    ...listener,
  };
  if (listener.script) {
    self = {
      ...listener,
      ...listener.script,
      scriptType: listener.script.resource ? 'externalScript' : 'inlineScript',
    };
  }
  if (listener.event === 'timeout' && listener.eventDefinitions) {
    if (listener.eventDefinitions.length) {
      let k = '';
      for (const key in listener.eventDefinitions[0]) {
        if (key.indexOf('time') !== -1) {
          k = key;
          self.eventDefinitionType = key.replace('time', '').toLowerCase();
        }
      }
      self.eventTimeDefinitions = listener.eventDefinitions[0][k].body;
    }
    if (listener.fields) {
      self.fields = listener.fields.map(field => {
        return {
          ...field,
          fieldType: field.string ? 'string' : 'expression',
        };
      });
    }
  }
  return self;
}

export function initListenerType(listener) {
  let listenerType;
  if (listener.class) listenerType = 'classListener';
  if (listener.expression) listenerType = 'expressionListener';
  if (listener.delegateExpression) listenerType = 'delegateExpressionListener';
  if (listener.script) listenerType = 'scriptListener';
  return {
    id: (listener.$attrs || {}).id,
    ...JSON.parse(JSON.stringify(listener)),
    ...(listener.script ?? {}),
    listenerType: listenerType,
  };
}

export const listenerType = {
  classListener: 'Java Class',
  expressionListener: 'Expression',
  delegateExpressionListener: 'Delegate Expression',
  scriptListener: 'Script',
};

export const eventType = {
  create: 'Create',
  assignment: 'Assignment',
  complete: 'Complete',
  delete: 'Delete',
  // update: "更新",
  // timeout: "超时"
};

export const fieldType = {
  string: 'String',
  expression: 'Expression',
};
