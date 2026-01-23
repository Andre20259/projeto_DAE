<template>
  <div class="min-h-screen bg-gray-900">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">
        PGPC
      </NuxtLink>

      <div class="ml-auto text-sm">
        <template v-if="username && name && role">
          <button @click="goToProfile" class="text-gray-300 hover:text-white">
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

    <div class="p-6 max-w-6xl mx-auto">
      <div class="mb-6">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
          <div class="flex items-center space-x-3">
            <button
                @click="createPublication"
                class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors text-sm"
            >
              + Create
            </button>

            <div class="flex bg-gray-800 p-1 rounded-lg border border-gray-700">
              <button
                  @click="changeScope('all')"
                  :class="[viewScope === 'all' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white']"
                  class="px-3 py-1 rounded-md text-xs transition-all"
              >
                All
              </button>
              <button
                  v-if="username"
                  @click="changeScope('me')"
                  :class="[viewScope === 'me' ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white']"
                  class="px-3 py-1 rounded-md text-xs transition-all"
              >
                My Pubs
              </button>
              <button
                  v-if="role === 'Administrator' || role === 'Responsible'"
                  @click="changeScope('hidden')"
                  :class="[viewScope === 'hidden' ? 'bg-red-600 text-white' : 'text-gray-400 hover:text-white']"
                  class="px-3 py-1 rounded-md text-xs transition-all"
              >
                Hidden
              </button>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <button
                @click="fetchPublications"
                :disabled="loading"
                class="bg-gray-600 text-white px-3 py-2 rounded-md hover:bg-gray-700 transition-colors text-sm"
            >
              🔄 Refresh
            </button>

            <div class="flex items-center gap-2">
              <template v-if="hasActiveFilters">
                <div v-for="f in activeFilterBadges" :key="f" class="bg-gray-700 text-gray-200 px-2 py-1 rounded text-xs">
                  {{ f }}
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="bg-gray-800 p-4 rounded-xl shadow-md grid grid-cols-1 md:grid-cols-3 gap-3">
          <div class="col-span-1 md:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-3">
            <input v-model="filterTitle" type="text" placeholder="Title"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />

            <input v-model="filterAuthor" type="text" placeholder="Author username"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />

            <input v-model="filterTag" type="text" placeholder="Tag"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />

            <input v-model="filterArea" type="text" placeholder="Area (e.g. AI)"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />

            <input v-model="filterDate" type="date"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />
          </div>

          <div class="col-span-1 flex flex-col gap-3">
            <div class="grid grid-cols-2 gap-2">
              <select v-model="sortBy" class="px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none">
                <option value="">Sort By</option>
                <option value="uploadDate">Date</option>
                <option value="title">Title</option>
                <option value="averageRating">Rating</option>
              </select>

              <select v-model="order" class="px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none">
                <option value="">Order</option>
                <option value="asc">Asc</option>
                <option value="desc">Desc</option>
              </select>
            </div>

            <div class="flex gap-2">
              <button @click="applyFilters" :disabled="loading"
                      class="flex-1 bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 text-sm">
                Apply
              </button>
              <button @click="clearFilters" :disabled="loading"
                      class="bg-gray-600 text-white px-3 py-2 rounded hover:bg-gray-700 text-sm">
                Clear
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="grid gap-4">
        <div
            v-for="pub in publications"
            :key="pub.id"
            @click="goToPublication(pub.id)"
            :class="[
            'p-4 rounded-xl shadow-md cursor-pointer transition-colors border',
            isMine(pub) ? 'bg-blue-900/40 border-blue-500 hover:bg-blue-900/60' : 'bg-gray-800 border-transparent hover:bg-gray-750'
          ]"
        >
          <div class="flex justify-between items-start">
            <h2 class="text-white font-bold text-lg mb-1">{{ pub.title }}</h2>
            <span v-if="pub.visible === false" class="bg-red-900 text-red-200 text-[10px] px-2 py-0.5 rounded uppercase font-bold">Hidden</span>
          </div>

          <p class="text-gray-300 mb-2 text-sm">
            {{ truncate(pub.description, 255) || 'No description' }}
          </p>

          <div class="flex flex-wrap text-gray-400 text-xs gap-3 mb-2">
            <span><strong>Area:</strong> {{ pub.area }}</span>
            <span><strong>Authors:</strong> {{ pub.authors.map(a => a.name).join(', ') }}</span>
          </div>

          <div class="flex justify-between items-center text-gray-500 text-xs">
            <span>{{ formatDate(pub.uploadDate) }} | Rating: {{ (pub.averageRating ?? 0).toFixed(1) }} ★</span>
            <span v-if="isMine(pub)" class="text-blue-400 italic">Your publication</span>
          </div>
        </div>

        <div v-if="!loading && publications.length === 0" class="text-gray-500 text-center py-10">
          No publications found in "{{ viewScope }}" view.
        </div>

        <div v-if="loading" class="text-blue-400 text-center py-10 animate-pulse">
          Fetching publications...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter, useRuntimeConfig} from '#imports'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

const username = ref('')
const name = ref('')
const role = ref('')

const publications = ref([])
const loading = ref(false)

// New state for switching between endpoints
const viewScope = ref('all') // 'all' | 'me' | 'hidden'

const filterTitle = ref('')
const filterAuthor = ref('')
const filterTag = ref('')
const filterArea = ref('')
const filterDate = ref('')
const sortBy = ref('')
const order = ref('')

onMounted(() => {
  if (process.client) {
    username.value = sessionStorage.getItem('username') || ''
    name.value = sessionStorage.getItem('name') || ''
    role.value = sessionStorage.getItem('role') || ''
  }
  fetchPublications()
})

function buildQuery() {
  const params = new URLSearchParams()
  if (filterTitle.value.trim()) params.set('title', filterTitle.value.trim())
  if (filterAuthor.value.trim()) params.set('author', filterAuthor.value.trim())
  if (filterTag.value.trim()) params.set('tag', filterTag.value.trim())
  if (filterArea.value.trim()) params.set('area', filterArea.value.trim())
  if (filterDate.value) params.set('date', filterDate.value)
  if (sortBy.value) params.set('sortBy', sortBy.value)
  if (order.value) params.set('order', order.value)
  return params.toString()
}

async function fetchPublications() {
  loading.value = true
  try {
    const token = process.client ? sessionStorage.getItem('auth_token') : null

    // Determine base endpoint based on selected scope
    let endpoint = `${api}/publications`
    if (viewScope.value === 'me') endpoint += '/me'
    if (viewScope.value === 'hidden') endpoint += '/hidden'

    const query = buildQuery()
    const url = query ? `${endpoint}?${query}` : endpoint

    const data = await $fetch(url, {
      headers: token ? {Authorization: `Bearer ${token}`} : {}
    })
    publications.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to fetch publications:', err)
    publications.value = []
  } finally {
    loading.value = false
  }
}

function changeScope(newScope) {
  viewScope.value = newScope
  fetchPublications()
}

function applyFilters() {
  fetchPublications()
}

function clearFilters() {
  filterTitle.value = ''
  filterAuthor.value = ''
  filterTag.value = ''
  filterArea.value = ''
  filterDate.value = ''
  sortBy.value = ''
  order.value = ''
  fetchPublications()
}

const isMine = (pub) => pub.authors?.some(a => a.username === username.value)
const truncate = (text, max) => (text?.length > max ? text.slice(0, max) + '…' : text)
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString() : ''
const goToProfile = () => router.push('/me')
const createPublication = () => router.push('/publications/create')
const goToPublication = (id) => router.push(`/publications/${id}`)

const activeFilterBadges = computed(() => {
  const badges = []
  if (filterTitle.value) badges.push(`Title: ${filterTitle.value}`)
  if (filterAuthor.value) badges.push(`Author: ${filterAuthor.value}`)
  if (viewScope.value !== 'all') badges.push(`Scope: ${viewScope.value}`)
  return badges
})
const hasActiveFilters = computed(() => activeFilterBadges.value.length > 0 || viewScope.value !== 'all')
</script>