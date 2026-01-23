<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">
        PGPC
      </NuxtLink>

      <div class="ml-auto text-sm">
        <template v-if="username && name && role">
          <button
              @click="goToProfile"
              class="text-gray-300 hover:text-white"
          >
            Logged as {{ role }}: {{ name }}
          </button>
        </template>
        <template v-else>
          <NuxtLink to="/login" class="text-gray-300 hover:text-white">
            Login
          </NuxtLink>
        </template>
      </div>
    </nav>

    <!-- Page Content -->
    <div class="p-6 max-w-6xl mx-auto">
      <!-- Top Controls: Create + Filters -->
      <div class="flex items-center justify-between mb-6">
        <button
            @click="createPublication"
            class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors text-sm"
        >
          Create Publication
        </button>

        <!-- Filters placeholder -->
        <div class="flex space-x-4">
          <span class="text-gray-400 text-sm">Filters placeholder</span>
        </div>
      </div>

      <!-- Publications List -->
      <div class="grid gap-4">
        <div
            v-for="pub in publications"
            :key="pub.id"
            @click="goToPublication(pub.id)"
            class="bg-gray-800 p-4 rounded-xl shadow-md hover:bg-gray-700 cursor-pointer transition-colors"
        >
          <h2 class="text-white font-bold text-lg mb-1">{{ pub.title }}</h2>
          <p class="text-gray-300 mb-2">
            {{ truncate(pub.description, 255) || 'No description' }}
          </p>
          <div class="flex flex-wrap text-gray-400 text-sm gap-2 mb-1">
            <span>Area: {{ pub.area }}</span>
            <span>Authors: {{ pub.authors.map(a => a.name).join(', ') }}</span>
            <span>Tags: {{ pub.tags.map(t => t.name).join(', ') }}</span>
          </div>
          <div class="text-gray-400 text-sm">
            Uploaded: {{ formatDate(pub.uploadDate) }} | Rating: {{ pub.averageRating.toFixed(1) }}
          </div>
        </div>

        <p v-if="publications.length === 0" class="text-gray-400 text-center mt-4">
          No publications found.
        </p>
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

const username = ref('')
const name = ref('')
const role = ref('')

// Publications state
const publications = ref([])

// read session info
onMounted(() => {
  const token = sessionStorage.getItem('auth_token')
  const storedUsername = sessionStorage.getItem('username')
  const storedName = sessionStorage.getItem('name')
  const storedRole = sessionStorage.getItem('role')

  if (token && storedUsername && storedName && storedRole) {
    username.value = storedUsername
    name.value = storedName
    role.value = storedRole
  }

  fetchPublications()
})

// Fetch publications
async function fetchPublications() {
  try {
    const token = sessionStorage.getItem('auth_token')
    const data = await $fetch(`${api}/publications`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    publications.value = data
  } catch (err) {
    console.error('Failed to fetch publications:', err)
  }
}

// Handlers
const goToProfile = () => router.push('/me')
const createPublication = () => alert('Create Publication clicked (to be implemented)')
const goToPublication = (id) => router.push(`/publications/${id}`)

// Helpers
const truncate = (text, max) => text?.length > max ? text.slice(0, max) + '…' : text
const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString()
</script>
