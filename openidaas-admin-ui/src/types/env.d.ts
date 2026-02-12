/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_API_TIMEOUT: string
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_SHORT_NAME: string
  readonly VITE_JWT_SECRET_KEY: string
  readonly VITE_TOKEN_EXPIRES_IN: string
  readonly VITE_UPLOAD_BASE_URL: string
  readonly VITE_UPLOAD_MAX_SIZE: string
  readonly VITE_APP_DEBUG: string
  readonly VITE_APP_MOCK: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}