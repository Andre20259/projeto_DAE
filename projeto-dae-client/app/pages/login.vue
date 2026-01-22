<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="bg-white p-8 rounded-xl shadow-md w-full max-w-sm">
      <h1 class="text-2xl font-bold text-center mb-6">Login</h1>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1" for="username">Username</label>
          <input
              id="username"
              v-model="username"
              type="text"
              placeholder="Enter your username"
              class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-1" for="password">Password</label>
          <input
              id="password"
              v-model="password"
              type="password"
              placeholder="Enter your password"
              class="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
          />
        </div>

        <button
            type="submit"
            class="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 transition-colors"
        >
          Log In
        </button>
      </form>

      <p class="text-center text-sm text-gray-500 mt-4">
        Don't have an account? <a href="#" class="text-blue-500 hover:underline">Sign up</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const router = useRouter()

const handleLogin = () => {
  // Example: replace with real auth
  useCookie('loggedIn').value = true

  router.push('/')
}

const logout = () => {
  useCookie('loggedIn').value = false
  router.push('/login')
}

</script>
