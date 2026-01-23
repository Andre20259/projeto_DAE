<template>
  <div class="min-h-screen bg-gray-900 text-gray-100">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center border-b border-gray-700">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">
        PGPC
      </NuxtLink>

      <div v-if="username" class="ml-8 flex gap-6 text-sm font-medium">
        <NuxtLink to="/publications" class="text-gray-400 hover:text-white transition">
          Publications
        </NuxtLink>
        <NuxtLink to="/tags" class="text-gray-400 hover:text-white transition">
          Tags
        </NuxtLink>
        <NuxtLink
            v-if="role === 'Administrator' || role === 'Responsible'"
            to="/admin"
            class="text-blue-400 hover:text-blue-300 transition"
        >
          Admin Panel
        </NuxtLink>
      </div>

      <div class="ml-auto flex items-center gap-4 text-sm">
        <template v-if="username && name">
          <button
              @click="goToProfile"
              class="text-gray-300 hover:text-white font-medium border-r border-gray-700 pr-4"
          >
            <span class="text-gray-500 text-xs mr-1">{{ role }}:</span> {{ name }}
          </button>

          <button
              @click="logout"
              class="text-red-400 hover:text-red-300 font-medium"
          >
            Logout
          </button>
        </template>

        <template v-else>
          <NuxtLink to="/login" class="text-gray-300 hover:text-white font-medium">
            Login
          </NuxtLink>
        </template>
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
                  <button @click="openEditModal(u)" class="px-3 py-1.5 text-xs bg-amber-600/10 text-amber-500 rounded-md hover:bg-amber-600 hover:text-white transition">
                    Edit
                  </button>

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
  <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
    <div class="bg-gray-800 border border-gray-700 rounded-xl w-full max-w-md overflow-hidden shadow-2xl">
      <div class="p-6 border-b border-gray-700 flex justify-between items-center">
        <h2 class="text-xl font-bold text-white">Edit User: {{ editingUser.username }}</h2>
        <button @click="showEditModal = false" class="text-gray-400 hover:text-white">&times;</button>
      </div>

      <form @submit.prevent="updateUser" class="p-6 space-y-4">
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase mb-1">Full Name</label>
          <input v-model="editingUser.name" type="text" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 text-white focus:border-blue-500 outline-none" required />
        </div>

        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase mb-1">Email Address</label>
          <input v-model="editingUser.email" type="email" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 text-white focus:border-blue-500 outline-none" required />
        </div>

        <div class="flex items-center gap-3 pt-2">
          <input type="checkbox" v-model="editingUser.isActive" id="isActive" class="w-4 h-4 rounded border-gray-700 bg-gray-900 text-blue-600" />
          <label for="isActive" class="text-sm text-gray-300">Account Active</label>
        </div>

        <div class="flex gap-3 pt-4">
          <button type="button" @click="showEditModal = false" class="flex-1 px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg transition">Cancel</button>
          <button type="submit" class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-lg transition">Save Changes</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const config = useRuntimeConfig()
const api = config.public.apiBase
const router = useRouter()

// Identity state (assuming these are populated onMounted from sessionStorage)
const username = ref('')
const name = ref('')
const role = ref('')

const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentAdmin = ref('')
const token = ref('')

const showHistory = ref(false)
const selectedUser = ref('')
const historyData = ref(null)
const loadingHistory = ref(false)
// ... existing refs ...
const showEditModal = ref(false)
const editingUser = ref({ name: '', email: '', username: '', isActive: true })

// Function to open the modal and populate data
function openEditModal(user) {
  editingUser.value = {
    name: user.name,
    email: user.email || '',
    username: user.username,
    isActive: user.active // mapping 'active' from your list to 'isActive' for your API
  }
  showEditModal.value = true
}

// The PUT implementation
async function updateUser() {
  try {
    await $fetch(`${api}/users/${editingUser.value.username}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: editingUser.value
    })

    // Refresh the list to show updated info
    await fetchUsers()
    showEditModal.value = false
    alert('User updated successfully!')
  } catch (err) {
    console.error(err)
    alert('Failed to update user.')
  }
}

onMounted(() => {
  if (process.client) {
    username.value = sessionStorage.getItem('username') || ''
    name.value = sessionStorage.getItem('name') || ''
    role.value = sessionStorage.getItem('role') || ''
    token.value = sessionStorage.getItem('auth_token') || ''
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

function logout() {
  sessionStorage.clear()
  router.push('/')
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