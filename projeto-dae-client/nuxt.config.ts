// https://nuxt.com/docs/api/configuration/nuxt-config
import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  css: ['@/assets/tailwind.css'], // use @/ instead of ~/
  postcss: {
    plugins: {
      '@tailwindcss/postcss': {}, // this is mandatory for Tailwind 4
      autoprefixer: {},
    },
  },
  vite: {
    // Optional but helps with path resolution on Windows
    resolve: {
      alias: {
        '~/': `${__dirname}/app/`,
        '@': `${__dirname}/app/`,
      },
    },
  },
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8081/Projeto_DAE/api'
    }
  }
})


