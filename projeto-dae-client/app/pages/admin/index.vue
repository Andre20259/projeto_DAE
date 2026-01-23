<template>
  <div class="min-h-screen bg-gray-900 text-gray-100">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">PGPC Admin</NuxtLink>
      <div class="ml-auto flex gap-4 text-sm">
        <NuxtLink to="/publications" class="text-gray-400 hover:text-white">Publications</NuxtLink>
        <NuxtLink to="/me" class="text-gray-400 hover:text-white">My Profile</NuxtLink>
      </div>
    </nav>

    <div class="p-6 max-w-7xl mx-auto">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 class="text-2xl font-bold text-white">User Management</h1>
          <p class="text-gray-400 text-sm">Manage platform access and monitor user activity</p>
        </div>

        <div class="flex items-center gap-3">
          <div class="relative">
            <input v-model="searchQuery" type="text" placeholder="Search by username or email..."
                   class="w-64 pl-10 pr-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm focus:border-blue-500 outline-none transition" />
            <span class="absolute left-3 top-2.5 text-gray-500">🔍</span>
          </div>
          <button @click="fetchUsers" class="p-2 bg-gray-800 hover:bg-gray-700 rounded-lg border border-gray-700 transition">
            🔄
          </button>
        </div>
      </div>

      <div class="bg-gray-800 rounded-xl shadow-xl border border-gray-700 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
            <tr class="bg-gray-900/50 text-gray-400 text-xs uppercase tracking-wider">
              <th class="px-6 py-4 font-semibold">User</th>
              <th class="px-6 py-4 font-semibold">Role</th>
              <th class="px-6 py-4 font-semibold">Status</th>
              <th class="px-6 py-4 font-semibold text-right">Actions</th>
            </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
            <tr v-if="loading" v-for="i in 3" :key="i" class="animate-pulse">
              <td colspan="4" class="px-6 py-4 bg-gray-800/50 h-16"></td>
            </tr>

            <tr v-for="u in filteredUsers" :key="u.username" class="hover:bg-gray-750 transition-colors group">
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-gray-700 flex items-center justify-center font-bold text-blue-400">
                    {{ u.name.charAt(0) }}
                  </div>
                  <div>
                    <div class="text-sm font-bold text-white">{{ u.name }}</div>
                    <div class="text-xs text-gray-500">{{ u.username }} • {{ u.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                  <span :class="roleClass(u.role)" class="text-[10px] px-2 py-1 rounded-full font-bold uppercase tracking-tighter">
                    {{ u.role }}
                  </span>
              </td>
              <td class="px-6 py-4">
                  <span v-if="u.active" class="flex items-center gap-1.5 text-green-400 text-xs">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span> Active
                  </span>
                <span v-else class="flex items-center gap-1.5 text-gray-500 text-xs">
                    <span class="w-1.5 h-1.5 rounded-full bg-gray-600"></span> Inactive
                  </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex justify-end gap-2">
                  <button @click="openHistory(u.username)" class="px-3 py-1.5 text-xs bg-blue-600/10 text-blue-400 rounded-md hover:bg-blue-600 hover:text-white transition">
                    History
                  </button>
                  <button @click="confirmDelete(u.username)" :disabled="u.username === currentAdmin"
                          class="px-3 py-1.5 text-xs bg-red-900/20 text-red-500 rounded-md hover:bg-red-600 hover:text-white transition disabled:opacity-30">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        <div v-if="!loading && filteredUsers.length === 0" class="p-12 text-center text-gray-500 italic">
          No users found matching your search.
        </div>
      </div>
    </div>

    <div v-if="showHistory" class="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm">
      <div class="w-full max-w-md bg-gray-800 h-full shadow-2xl border-l border-gray-700 flex flex-col animate-slide-in">
        <div class="p-6 border-b border-gray-700 flex justify-between items-center">
          <div>
            <h2 class="text-xl font-bold text-white">Activity History</h2>
            <p class="text-xs text-gray-500">Log for @{{ selectedUser }}</p>
          </div>
          <button @click="showHistory = false" class="text-gray-400 hover:text-white text-2xl">&times;</button>
        </div>

        <div class="flex-1 overflow-y-auto p-6">
          <div v-if="loadingHistory" class="space-y-4">
            <div v-for="i in 5" :key="i" class="h-12 bg-gray-700/50 rounded animate-pulse"></div>
          </div>
          <div v-else-if="historyData.length === 0" class="text-center py-20 text-gray-600 italic">
            No activity history found for this user.
          </div>
          <div v-else class="space-y-4">
            <div v-for="(log, idx) in historyData" :key="idx" class="p-4 rounded-lg bg-gray-900/50 border border-gray-700">
              <p class="text-sm text-gray-300">{{ log.description || log }}</p>
              <p class="text-[10px] text-gray-500 mt-2 uppercase">{{ formatDateTime(log.timestamp || log.created_at) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const config = useRuntimeConfig()
const api = config.public.apiBase
const router = useRouter()

// State
const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentAdmin = ref('')
const token = ref('')

// History Modal State
const showHistory = ref(false)
const selectedUser = ref('')
const historyData = ref([])
const loadingHistory = ref(false)

onMounted(() => {
  if (process.client) {
    token.value = sessionStorage.getItem('auth_token')
    currentAdmin.value = sessionStorage.getItem('username')
    const role = sessionStorage.getItem('role')

    if (!token.value || (role !== 'Administrator' && role !== 'Responsible')) {
      router.push('/')
      return
    }
    fetchUsers()
  }
})

async function fetchUsers() {
  loading.value = true
  try {
    const data = await $fetch(`${api}/users`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    users.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to fetch users', err)
  } finally {
    loading.value = false
  }
}

const filteredUsers = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return users.value
  return users.value.filter(u =>
      u.username.toLowerCase().includes(query) ||
      u.email.toLowerCase().includes(query) ||
      u.name.toLowerCase().includes(query)
  )
})

async function confirmDelete(username) {
  if (!confirm(`Are you sure you want to permanently delete user @${username}? This action cannot be undone.`)) return

  try {
    await $fetch(`${api}/users/${username}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    users.value = users.value.filter(u => u.username !== username)
  } catch (err) {
    alert('Failed to delete user.')
  }
}

async function openHistory(username) {
  selectedUser.value = username
  showHistory.value = true
  loadingHistory.value = true
  try {
    const data = await $fetch(`${api}/users/${username}/history`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    historyData.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('History fetch failed', err)
    historyData.value = []
  } finally {
    loadingHistory.value = false
  }
}

// Helpers
const roleClass = (role) => {
  switch (role) {
    case 'Administrator': return 'bg-red-900/30 text-red-400 border border-red-900/50'
    case 'Responsible': return 'bg-blue-900/30 text-blue-400 border border-blue-900/50'
    default: return 'bg-green-900/30 text-green-400 border border-green-900/50'
  }
}

const formatDateTime = (d) => d ? new Date(d).toLocaleString() : 'Recent'
</script>

<style scoped>
@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}
.animate-slide-in {
  animation: slideIn 0.3s ease-out;
}
</style>