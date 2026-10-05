import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// 导入对应包

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import ElementPlus from 'unplugin-element-plus/vite'

/**
 * 启动 dev server 后用 Microsoft Edge 打开页面。
 *
 * 为什么不用 Vite 自带的 server.open：
 *   Vite 4 的 server.open 只能"用系统默认浏览器"打开，且不认 BROWSER 环境变量，
 *   无法指定 Edge。这里改用 open 包并显式指定 app。
 *
 * 注意：open 是 ESM-only 包，而 Vite 4 会把配置文件打包成 CJS，
 * 因此这里用**动态 import** 延迟加载，避免 "ESM file cannot be loaded by require"。
 *
 * 各平台使用的浏览器名：
 *   win32  -> 'msedge'（open 内置别名，经 App Paths\msedge.exe 解析为
 *             C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe）
 *   darwin -> 'Microsoft Edge'
 *   其他   -> 'microsoft-edge'
 */
function openInEdge() {
  const appName = process.platform === 'win32'
    ? 'msedge'
    : (process.platform === 'darwin' ? 'Microsoft Edge' : 'microsoft-edge')

  return {
    name: 'open-in-edge',
    apply: 'serve',
    configureServer(server) {
      const originalPrintUrls = server.printUrls.bind(server)
      server.printUrls = () => {
        originalPrintUrls()
        const url = server.resolvedUrls?.local?.[0]
        if (!url) return
        import('open')
          .then(({ default: open }) => open(url, { app: { name: appName } }))
          .catch((err) => {
            // 打不开不应影响开发服务器启动
            server.config.logger.warn(
              `[open-in-edge] 无法用 Edge 打开 ${url}：${err.message}\n` +
              `  请确认已安装 Microsoft Edge，或直接手动访问该地址。`
            )
          })
      }
    },
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    openInEdge(),
    AutoImport({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),
    Components({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),

    // 按需定制主题配置
    ElementPlus({
      useSource: true,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 自动导入定制化样式文件进行样式覆盖
        additionalData: `
          @use "@/assets/css/index.scss" as *;
        `,
      }
    }
  }
})
