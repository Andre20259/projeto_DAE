<template>
  <div class="min-h-screen bg-gray-900">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">PGPC</NuxtLink>
      <div class="ml-auto text-sm">
        <template v-if="username && name && role">
          <button @click="goToProfile" class="text-gray-300 hover:text-white">
            Logged as {{ role }}: {{ name }}
          </button>
        </template>
        <template v-else>
          <NuxtLink to="/login" class="text-gray-300 hover:text-white">Login</NuxtLink>
        </template>
      </div>
    </nav>

    <div class="p-6 max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-white mb-6">Explore Tags</h1>

        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div v-if="isAdminOrResponsible" class="flex-1 flex gap-2">
            <input
                v-model="newTagName"
                type="text"
                placeholder="Create new tag..."
                class="flex-1 px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white text-sm focus:border-blue-500 outline-none"
                @keyup.enter="createTag"
            />
            <button @click="createTag" :disabled="!newTagName.trim() || processing"
                    class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-bold transition disabled:opacity-50">
              Create
            </button>
          </div>

          <div class="flex items-center gap-2">
            <div class="flex bg-gray-800 p-1 rounded-lg border border-gray-700">
              <button @click="changeScope('all')" :class="[viewScope === 'all' ? 'bg-blue-600 text-white' : 'text-gray-400']"
                      class="px-4 py-1 rounded-md text-xs transition-all font-medium">
                All Tags
              </button>
              <button v-if="isAdminOrResponsible" @click="changeScope('hidden')" :class="[viewScope === 'hidden' ? 'bg-red-600 text-white' : 'text-gray-400']"
                      class="px-4 py-1 rounded-md text-xs transition-all font-medium">
                Hidden
              </button>
            </div>
            <button @click="fetchTags" class="p-2 text-gray-400 hover:text-white transition">🔄</button>
          </div>
        </div>

        <div class="relative">
          <input v-model="searchQuery" type="text" placeholder="Search tags..."
                 class="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-xl text-white focus:ring-1 focus:ring-blue-500 outline-none" />
        </div>
      </div>

      <div v-if="loading" class="text-center py-20 text-blue-400 animate-pulse">Loading tags...</div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div v-for="tag in filteredTags" :key="tag.name"
             class="bg-gray-800 border border-gray-700 p-4 rounded-xl flex items-center justify-between hover:border-gray-600 transition shadow-sm">

          <div class="flex flex-col">
            <div class="flex items-center gap-2">
              <span class="text-white font-bold">#{{ tag.name }}</span>
              <span v-if="!tag.visible" class="text-[10px] bg-red-900/30 text-red-400 px-1.5 py-0.5 rounded border border-red-900/50 uppercase">Hidden</span>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <template v-if="token">
              <button v-if="!isSubscribed(tag.name)" @click="subscribe(tag.name)"
                      class="text-xs bg-gray-700 hover:bg-blue-600 text-white px-3 py-1.5 rounded-lg transition">
                Subscribe
              </button>
              <button v-else @click="unsubscribe(tag.name)"
                      class="text-xs bg-blue-600/20 text-blue-400 border border-blue-500/30 px-3 py-1.5 rounded-lg hover:bg-red-900/40 hover:text-red-400 hover:border-red-500/50 transition">
                Subscribed
              </button>
            </template>

            <div v-if="isAdminOrResponsible" class="flex gap-1 ml-2 border-l border-gray-700 pl-2">
              <button @click="toggleVisibility(tag)" class="p-1.5 hover:bg-gray-700 rounded text-gray-400" :title="tag.visible ? 'Hide' : 'Unhide'">
                {{ tag.visible ? '👁️' : '🕶️' }}
              </button>
              <button @click="deleteTag(tag.name)" class="p-1.5 hover:bg-red-900/40 rounded text-red-500" title="Delete">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && filteredTags.length === 0" class="text-center py-20 text-gray-500">
        No tags found.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

// User State
const username = ref('')
const name = ref('')
const role = ref('')
const token = ref('')

// Data State
const tags = ref([])
const loading = ref(false)
const processing = ref(false)
const viewScope = ref('all')
const searchQuery = ref('')
const newTagName = ref('')

// User's Subscriptions (Mock or real depending on if your user profile fetch includes them)
// If the API doesn't tell us which tags we are subbed to in the tag list, we'd need a separate fetch.
const mySubscriptions = ref([])

const isAdminOrResponsible = computed(() => role.value === 'Administrator' || role.value === 'Responsible')

const filteredTags = computed(() => {
  if (!searchQuery.value.trim()) return tags.value
  return tags.value.filter(t => t.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

onMounted(() => {
  if (process.client) {
    username.value = sessionStorage.getItem('username') || ''
    name.value = sessionStorage.getItem('name') || ''
    role.value = sessionStorage.getItem('role') || ''
    token.value = sessionStorage.getItem('auth_token') || ''
  }
  fetchTags()
  // fetchUserSubscriptions() // If applicable
})

async function fetchTags() {
  loading.value = true
  try {
    const endpoint = viewScope.value === 'hidden' ? `${api}/tags/hidden` : `${api}/tags`
    const data = await $fetch(endpoint, {
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    tags.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error(err)
    tags.value = []
  } finally {
    loading.value = false
  }
}

function changeScope(scope) {
  viewScope.value = scope
  fetchTags()
}

async function createTag() {
  if (!newTagName.value.trim()) return
  processing.value = true
  try {
    await $fetch(`${api}/tags`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json' },
      body: { name: newTagName.value.trim() }
    })
    newTagName.value = ''
    fetchTags()
  } catch (err) {
    alert('Failed to create tag. It might already exist.')
  } finally {
    processing.value = false
  }
}

async function toggleVisibility(tag) {
  try {
    const nextStatus = !tag.visible
    await $fetch(`${api}/tags/${tag.name}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json' },
      body: { visible: String(nextStatus) }
    })
    fetchTags()
  } catch (err) { console.error(err) }
}

async function deleteTag(tagName) {
  if (!confirm(`Are you sure you want to delete #${tagName}?`)) return
  try {
    await $fetch(`${api}/tags/${tagName}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    fetchTags()
  } catch (err) { console.error(err) }
}

// Subscription Logic
const isSubscribed = (tagName) => mySubscriptions.value.includes(tagName)

async function subscribe(tagName) {
  try {
    await $fetch(`${api}/tags/${tagName}/subscribe`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    mySubscriptions.value.push(tagName)
  } catch (err) { console.error(err) }
}

async function unsubscribe(tagName) {
  try {
    await $fetch(`${api}/tags/${tagName}/unsubscribe`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    mySubscriptions.value = mySubscriptions.value.filter(t => t !== tagName)
  } catch (err) { console.error(err) }
}

const goToProfile = () => router.push('/me')
</script>