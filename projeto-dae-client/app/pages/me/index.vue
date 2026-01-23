<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">
        PGPC
      </NuxtLink>

      <div class="ml-auto text-sm">
        <template v-if="username">
          <button
              @click="logout"
              class="text-gray-300 hover:text-white"
          >
            Logout
          </button>
        </template>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="grid place-items-center h-[calc(100vh-52px)]">
      <div class="bg-gray-800 p-8 rounded-xl shadow-lg w-full max-w-lg text-center mx-4">
        <h1 class="text-xl font-bold text-white mb-6">Your Profile</h1>

        <template v-if="user">
          <p class="text-gray-200 mb-2"><strong>Username:</strong> {{ user.username }}</p>
          <p class="text-gray-200 mb-2"><strong>Name:</strong> {{ user.name }}</p>
          <p class="text-gray-200 mb-2"><strong>Email:</strong> {{ user.email }}</p>
          <p class="text-gray-200 mb-2"><strong>Role:</strong> {{ user.role }}</p>
          <p class="text-gray-200 mb-4"><strong>Active:</strong> {{ user.active ? 'Yes' : 'No' }}</p>
        </template>

        <template v-else>
          <p class="text-gray-400">Loading user info...</p>
        </template>

        <button
            @click="logout"
            class="bg-red-600 text-white px-6 py-2 rounded-md hover:bg-red-700 transition-colors text-sm mt-4"
        >
          Logout
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

const username = ref(sessionStorage.getItem('username') || '')
const token = sessionStorage.getItem('auth_token') || ''
const user = ref(null)

async function fetchUser() {
  if (!username.value || !token) {
    router.push('/login')
    return
  }

  try {
    user.value = await $fetch(`${api}/users/${username.value}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
  } catch (err) {
    console.error(err)
    router.push('/login')
  }
}

function logout() {
  sessionStorage.removeItem('auth_token')
  sessionStorage.removeItem('username')
  router.push('/')
}

onMounted(() => {
  fetchUser()
})
</script>
