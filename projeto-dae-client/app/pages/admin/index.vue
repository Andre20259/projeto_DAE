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
            <input v-model="searchQuery" type="text" placeholder="Search users..."
                   class="w-64 pl-10 pr-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm focus:border-blue-500 outline-none transition" />
            <span class="absolute left-3 top-2.5 text-gray-500">🔍</span>
          </div>
          <button @click="fetchUsers" class="p-2 bg-gray-800 hover:bg-gray-700 rounded-lg border border-gray-700 transition">🔄</button>
        </div>
      </div>

      <div class="bg-gray-800 rounded-xl shadow-xl border border-gray-700 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
            <tr class="bg-gray-900/50 text-gray-400 text-xs uppercase tracking-wider">
              <th class="px-6 py-4 font-semibold">User</th>
              <th class="px-6 py-4 font-semibold">Role Control</th>
              <th class="px-6 py-4 font-semibold">Status</th>
              <th class="px-6 py-4 font-semibold text-right">Actions</th>
            </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
            <tr v-for="u in filteredUsers" :key="u.username" class="hover:bg-gray-750 transition-colors">
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-gray-700 flex items-center justify-center font-bold text-blue-400 border border-blue-500/20">
                    {{ u.name.charAt(0) }}
                  </div>
                  <div>
                    <div class="text-sm font-bold text-white">{{ u.name }}</div>
                    <div class="text-xs text-gray-500">@{{ u.username }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <select
                    :value="u.role"
                    @change="updateRole(u.username, $event.target.value)"
                    :disabled="u.username === currentAdmin"
                    class="bg-gray-900 border border-gray-700 rounded px-2 py-1 text-xs text-gray-300 focus:border-blue-500 outline-none"
                >
                  <option value="Colaborator">Colaborator</option>
                  <option value="Responsible">Responsible</option>
                  <option value="Administrator">Administrator</option>
                </select>
              </td>
              <td class="px-6 py-4">
                  <span v-if="u.active" class="text-green-400 text-xs flex items-center gap-1.5">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span> Active
                  </span>
                <span v-else class="text-gray-500 text-xs flex items-center gap-1.5">
                    <span class="w-1.5 h-1.5 rounded-full bg-gray-600"></span> Inactive
                  </span>
              </td>
              <td class="px-6 py-4 text-right">
                <div class="flex justify-end gap-2">
                  <button @click="openHistory(u.username)" class="px-3 py-1.5 text-xs bg-blue-600/10 text-blue-400 rounded-md hover:bg-blue-600 hover:text-white transition">History</button>
                  <button @click="confirmDelete(u.username)" :disabled="u.username === currentAdmin" class="px-3 py-1.5 text-xs bg-red-900/20 text-red-500 rounded-md hover:bg-red-600 hover:text-white transition disabled:opacity-30">Delete</button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showHistory" class="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm">
      <div class="w-full max-w-sm bg-gray-800 h-full shadow-2xl border-l border-gray-700 flex flex-col animate-slide-in">
        <div class="p-6 border-b border-gray-700 flex justify-between items-center">
          <h2 class="text-xl font-bold text-white">User Statistics</h2>
          <button @click="showHistory = false" class="text-gray-400 hover:text-white text-2xl">&times;</button>
        </div>

        <div class="p-6 overflow-y-auto">
          <div v-if="loadingHistory" class="flex justify-center py-10"><span class="animate-spin">🌀</span></div>
          <div v-else-if="historyData" class="space-y-6">
            <div class="text-center pb-6 border-b border-gray-700">
              <div class="text-sm text-gray-400 mb-1">Activity summary for</div>
              <div class="text-lg font-bold text-blue-400">@{{ selectedUser }}</div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-900/50 p-4 rounded-xl border border-gray-700 text-center">
                <div class="text-2xl font-bold text-white">{{ historyData.uploads }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Uploads</div>
              </div>
              <div class="bg-gray-900/50 p-4 rounded-xl border border-gray-700 text-center">
                <div class="text-2xl font-bold text-white">{{ historyData.edits }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Edits</div>
              </div>
              <div class="bg-gray-900/50 p-4 rounded-xl border border-gray-700 text-center">
                <div class="text-2xl font-bold text-white">{{ historyData.ratings_given }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Ratings</div>
              </div>
              <div class="bg-gray-900/50 p-4 rounded-xl border border-gray-700 text-center">
                <div class="text-2xl font-bold text-white">{{ historyData.comments_posted }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Comments</div>
              </div>
            </div>

            <div class="mt-4 p-4 bg-blue-900/10 rounded-lg border border-blue-900/20 text-center">
              <div class="text-[10px] text-gray-500 uppercase font-bold mb-1">Last Activity Seen</div>
              <div class="text-xs text-gray-300">{{ formatDateTime(historyData.last_activity) }}</div>
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

const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentAdmin = ref('')
const token = ref('')

const showHistory = ref(false)
const selectedUser = ref('')
const historyData = ref(null)
const loadingHistory = ref(false)

onMounted(() => {
  if (process.client) {
    token.value = sessionStorage.getItem('auth_token')
    currentAdmin.value = sessionStorage.getItem('username')
    if (!token.value) router.push('/login')
    fetchUsers()
  }
})

async function fetchUsers() {
  loading.value = true
  try {
    users.value = await $fetch(`${api}/users`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function updateRole(username, newRole) {
  try {
    await $fetch(`${api}/users/${username}/role`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: { role: newRole.toUpperCase() }
    })
    // Update local state
    const user = users.value.find(u => u.username === username)
    if (user) user.role = newRole
  } catch (err) {
    alert('Failed to update role. Ensure you have permissions.')
  }
}

async function openHistory(username) {
  selectedUser.value = username
  showHistory.value = true
  loadingHistory.value = true
  try {
    historyData.value = await $fetch(`${api}/users/${username}/history`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
  } catch (err) {
    console.error(err)
  } finally {
    loadingHistory.value = false
  }
}

async function confirmDelete(username) {
  if (!confirm(`Delete @${username}?`)) return
  try {
    await $fetch(`${api}/users/${username}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    users.value = users.value.filter(u => u.username !== username)
  } catch (err) { alert('Delete failed') }
}

const filteredUsers = computed(() => {
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => u.username.toLowerCase().includes(q) || u.name.toLowerCase().includes(q))
})

const formatDateTime = (d) => d ? new Date(d).toLocaleString() : 'N/A'
</script>

<style scoped>
@keyframes slideIn { from { transform: translateX(100%); } to { transform: translateX(0); } }
.animate-slide-in { animation: slideIn 0.3s ease-out; }
</style>