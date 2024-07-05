/// <reference types="vite/client" />
declare module '*.vue' {
  import { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module '*.svg';
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.bmp';
declare module '*.tiff';
declare module '*.mjs';
declare module 'vue-json-viewer';
declare module 'ejs';
declare module 'bpmn-js/lib/Modeler';
declare module 'xml-js';
declare module 'bpmn-js-token-simulation';

interface ImportMetaEnv {
  VITE_SERVER_HOST: string;
  VITE_PROJECT_NAME: string;
}

interface ImportMeta {
  env: ImportMetaEnv;
}
