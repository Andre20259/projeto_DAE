<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <a href="/" class="text-white text-lg font-bold hover:text-gray-300">PGPC</a>
    </nav>

    <!-- Main Content -->
    <div class="flex items-center justify-center mt-12">
      <div class="bg-gray-800 p-6 rounded-xl shadow-lg w-full max-w-sm">
        <h1 class="text-xl font-bold text-center text-white mb-4">Login</h1>

        <form @submit.prevent="login" class="space-y-3">
          <!-- Username -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="username">Username</label>
            <input
                id="username"
                v-model="loginForm.username"
                type="text"
                placeholder="Enter your username"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="password">Password</label>
            <input
                id="password"
                v-model="loginForm.password"
                type="password"
                placeholder="Enter your password"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Error Message -->
          <p v-if="errorMessage" class="text-red-400 text-sm text-center">
            {{ errorMessage }}
          </p>

          <!-- Login Button -->
          <button
              type="submit"
              class="w-full bg-gray-600 text-white py-1.5 rounded-md hover:bg-gray-700 transition-colors text-sm"
          >
            Log In
          </button>
        </form>

        <p class="text-center text-xs text-gray-400 mt-3">
          Don't have an account? <a href="signup" class="text-gray-200 hover:underline">Sign up</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

// Login form reactive object (matches template style)
const loginForm = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')

async function login() {
  errorMessage.value = ''

  const requestOptions = {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: loginForm
  }

  // identical to your template project
  const { data, error } = await useFetch(`${api}/auth/login`, requestOptions)

  if (!error.value) {
    const token = typeof data.value === 'string' ? data.value : data.value.token
    sessionStorage.setItem('auth_token', token)
    router.push('/')
    return
  }

  errorMessage.value = error.value
}
</script>