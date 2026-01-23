<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <a href="/" class="text-white text-lg font-bold hover:text-gray-300">PGPC</a>
    </nav>

    <!-- Main Content -->
    <div class="flex items-center justify-center mt-12">
      <div class="bg-gray-800 p-6 rounded-xl shadow-lg w-full max-w-sm">
        <h1 class="text-xl font-bold text-center text-white mb-4">Sign Up</h1>

        <form @submit.prevent="handleSignup" class="space-y-3">
          <!-- Username -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="username">Username</label>
            <input
                id="username"
                v-model="signupForm.username"
                type="text"
                placeholder="Enter your username"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Name -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="name">Name</label>
            <input
                id="name"
                v-model="signupForm.name"
                type="text"
                placeholder="Enter your full name"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="email">Email</label>
            <input
                id="email"
                v-model="signupForm.email"
                type="email"
                placeholder="Enter your email"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium mb-1 text-gray-300" for="password">Password</label>
            <input
                id="password"
                v-model="signupForm.password"
                type="password"
                placeholder="Enter your password"
                class="w-full px-3 py-1.5 border border-gray-600 rounded-md bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-500"
                required
            />
          </div>

          <!-- Messages -->
          <p v-if="errorMessage" class="text-red-400 text-sm text-center">
            {{ errorMessage }}
          </p>
          <p v-if="successMessage" class="text-green-400 text-sm text-center">
            {{ successMessage }}
          </p>

          <!-- Signup Button -->
          <button
              type="submit"
              class="w-full bg-gray-600 text-white py-1.5 rounded-md hover:bg-gray-700 transition-colors text-sm"
          >
            Sign Up
          </button>
        </form>

        <p class="text-center text-xs text-gray-400 mt-3">
          Already have an account? <a href="/login" class="text-gray-200 hover:underline">Log in</a>
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

const signupForm = reactive({
  name: '',
  email: '',
  password: '',
  username: ''
})

const errorMessage = ref('')
const successMessage = ref('')

async function handleSignup() {
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const data = await $fetch(`${api}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: signupForm
    })

    // show success message
    successMessage.value = 'Signup successful! Redirecting to login...'

    // optional: delay to let user see message
    setTimeout(() => {
      router.push('/login')
    }, 1200)

  } catch (err) {
    errorMessage.value =
        err?.data?.message || 'Signup failed. Please check your inputs.'
  }
}
</script>
