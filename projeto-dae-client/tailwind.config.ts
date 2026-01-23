import { defineConfig } from 'tailwindcss'

export default defineConfig({
    content: [
        './app/**/*.{vue,ts,js}',      // include all app files
        './components/**/*.{vue,ts,js}', // include components
        './pages/**/*.{vue,ts,js}',     // optional if you have pages
    ],
    theme: {
        extend: {},
    },
    plugins: [],
})
