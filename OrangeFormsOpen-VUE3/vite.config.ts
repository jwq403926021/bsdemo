import path from 'path';
import { defineConfig } from 'vite';
import AutoImport from 'unplugin-auto-import/vite';
import vue from '@vitejs/plugin-vue';
import eslint from 'vite-plugin-eslint';
// import StylelintPlugin from 'vite-plugin-stylelint';
import postcssPresetEnv from 'postcss-preset-env';
import autoprefixer from 'autoprefixer';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import { VantResolver } from '@vant/auto-import-resolver';
// import { createStyleImportPlugin, VxeTableResolve } from 'vite-plugin-style-import';
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver(), VantResolver()],
      imports: ['vue'], // 需要引入的类型来源
      dts: 'src/types/auto-import.d.ts', // 根据引入来源自动生成的类型声明文件路径
      eslintrc: {
        enabled: true, // 使用 eslint 配置
      },
    }),
    Components({
      resolvers: [
        // 自定义element-plus主题色
        ElementPlusResolver({
          importStyle: 'sass',
        }),
        VantResolver(),
      ],
    }),
    eslint(),
    // StylelintPlugin(),
  ],
  server: {
    host: '0.0.0.0',
    port: 8085,
    open: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  css: {
    // 自定义element-plus主题色
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/skin/orange/index.scss" as *;`,
      },
    },
    postcss: {
      plugins: [autoprefixer, postcssPresetEnv()],
    },
  },
});
