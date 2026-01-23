<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 pb-12">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center mb-8">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">PGPC</NuxtLink>
      <div class="ml-auto flex items-center gap-4">
        <NuxtLink to="/publications" class="text-gray-300 hover:text-white text-sm font-medium">Publications</NuxtLink>
        <NuxtLink to="/tags" class="text-gray-300 hover:text-white text-sm font-medium">Tags</NuxtLink>
        <button @click="logout" class="text-red-400 hover:text-red-300 text-sm font-medium border border-red-900/50 px-3 py-1 rounded">Logout</button>
      </div>
    </nav>

    <div class="max-w-4xl mx-auto px-4 grid grid-cols-1 md:grid-cols-3 gap-8">

      <div class="md:col-span-2 space-y-6">

        <section class="bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-700">
          <h2 class="text-xl font-bold mb-6 flex items-center gap-2">
            <span class="text-blue-400 text-2xl">👤</span> General Settings
          </h2>

          <div v-if="loadingUser" class="text-gray-500 italic">Loading profile...</div>
          <form v-else @submit.prevent="updateProfile" class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold uppercase text-gray-500 mb-1">Username</label>
                <input v-model="profileForm.username" type="text" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:border-blue-500 outline-none transition" />
              </div>
              <div>
                <label class="block text-xs font-bold uppercase text-gray-500 mb-1">Full Name</label>
                <input v-model="profileForm.name" type="text" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:border-blue-500 outline-none transition" />
              </div>
            </div>

            <div>
              <label class="block text-xs font-bold uppercase text-gray-500 mb-1">Email Address</label>
              <input v-model="profileForm.email" type="email" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:border-blue-500 outline-none transition" />
            </div>

            <div class="flex items-center justify-between pt-2">
              <div class="flex items-center gap-2">
                <span :class="user?.active ? 'bg-green-500' : 'bg-red-500'" class="w-2 h-2 rounded-full"></span>
                <span class="text-xs text-gray-400">Account status: {{ user?.active ? 'Active' : 'Inactive' }}</span>
              </div>
              <button type="submit" :disabled="savingProfile" class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-bold text-sm transition disabled:opacity-50">
                {{ savingProfile ? 'Saving...' : 'Update Profile' }}
              </button>
            </div>
            <p v-if="profileMsg" :class="profileError ? 'text-red-400' : 'text-green-400'" class="text-center text-xs mt-2">{{ profileMsg }}</p>
          </form>
        </section>

        <section class="bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-700">
          <h2 class="text-xl font-bold mb-6 flex items-center gap-2">
            <span class="text-yellow-400 text-2xl">🔐</span> Security
          </h2>
          <form @submit.prevent="changePassword" class="space-y-4">
            <div>
              <label class="block text-xs font-bold uppercase text-gray-500 mb-1">Current Password</label>
              <input v-model="passwordForm.oldPassword" type="password" placeholder="••••••••" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:border-yellow-500 outline-none transition" />
            </div>
            <div>
              <label class="block text-xs font-bold uppercase text-gray-500 mb-1">New Password</label>
              <input v-model="passwordForm.newPassword" type="password" placeholder="Minimum 6 characters" class="w-full bg-gray-900 border border-gray-700 rounded-lg px-4 py-2 focus:border-yellow-500 outline-none transition" />
            </div>
            <div class="flex justify-end">
              <button type="submit" :disabled="changingPass" class="bg-gray-700 hover:bg-gray-600 text-white px-6 py-2 rounded-lg font-bold text-sm transition disabled:opacity-50">
                {{ changingPass ? 'Updating...' : 'Change Password' }}
              </button>
            </div>
            <p v-if="passwordMsg" :class="passwordError ? 'text-red-400' : 'text-green-400'" class="text-center text-xs mt-2">{{ passwordMsg }}</p>
          </form>
        </section>
      </div>

      <div class="space-y-6">
        <div class="bg-gradient-to-br from-gray-800 to-gray-800 border border-gray-700 p-6 rounded-xl shadow-lg">
          <div class="text-center mb-4">
            <div class="w-16 h-16 bg-blue-600/20 text-blue-400 rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-2 border border-blue-500/30">
              {{ user?.name?.charAt(0) || 'U' }}
            </div>
            <h3 class="text-white font-bold">{{ user?.name }}</h3>
            <p class="text-gray-500 text-xs">{{ user?.role }}</p>
          </div>
          <div class="space-y-2 border-t border-gray-700 pt-4">
            <div class="flex justify-between text-xs">
              <span class="text-gray-500 uppercase">Username</span>
              <span class="text-gray-300">{{ user?.username }}</span>
            </div>
            <div class="flex justify-between text-xs">
              <span class="text-gray-500 uppercase">Member Since</span>
              <span class="text-gray-300">Jan 2026</span>
            </div>
          </div>
        </div>

        <section class="bg-gray-800 rounded-xl shadow-lg border border-gray-700 overflow-hidden">
          <div class="p-4 border-b border-gray-700 flex justify-between items-center bg-gray-900/30">
            <h2 class="text-xs font-bold uppercase text-gray-400 tracking-wider">Your Stats</h2>
            <button @click="fetchActivity" class="text-[10px] text-blue-400 hover:underline">Refresh</button>
          </div>

          <div class="p-4">
            <div v-if="loadingActivity" class="text-center py-4 animate-pulse text-gray-500 text-xs">Loading stats...</div>

            <div v-else-if="activities" class="grid grid-cols-2 gap-3">
              <div class="bg-gray-900/50 p-3 rounded-lg border border-gray-700 text-center">
                <div class="text-xl font-bold text-white">{{ activities.uploads || 0 }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Uploads</div>
              </div>
              <div class="bg-gray-900/50 p-3 rounded-lg border border-gray-700 text-center">
                <div class="text-xl font-bold text-white">{{ activities.edits || 0 }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Edits</div>
              </div>
              <div class="bg-gray-900/50 p-3 rounded-lg border border-gray-700 text-center">
                <div class="text-xl font-bold text-white">{{ activities.ratings_given || 0 }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Ratings</div>
              </div>
              <div class="bg-gray-900/50 p-3 rounded-lg border border-gray-700 text-center">
                <div class="text-xl font-bold text-white">{{ activities.comments_posted || 0 }}</div>
                <div class="text-[10px] uppercase text-gray-500 font-bold">Comments</div>
              </div>
            </div>

            <div v-if="activities?.last_activity" class="mt-4 pt-4 border-t border-gray-700 text-center">
              <div class="text-[10px] text-gray-500 uppercase font-bold mb-1">Last Activity</div>
              <div class="text-xs text-gray-300">{{ formatDateTime(activities.last_activity) }}</div>
            </div>
          </div>
        </section>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRuntimeConfig } from '#imports'

const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

// User State
const user = ref(null)
const activities = ref([])
const currentUsername = ref('')
const token = ref('')
// Loading/Feedback States
const loadingUser = ref(true)
const loadingActivity = ref(false)
const savingProfile = ref(false)
const profileMsg = ref('')
const profileError = ref(false)
const changingPass = ref(false)
const passwordMsg = ref('')
const passwordError = ref(false)

// Forms
const profileForm = reactive({ name: '', email: '', username: '', isActive: true })
const passwordForm = reactive({ oldPassword: '', newPassword: '' })

onMounted(() => {
  if (process.client) {
    currentUsername.value = sessionStorage.getItem('username') || ''
    token.value = sessionStorage.getItem('auth_token') || ''

    if (!token.value) {
      router.push('/login')
      return
    }
    fetchUser()
  }
})

// --- API ACTIONS ---

async function fetchUser() {
  loadingUser.value = true
  try {
    const data = await $fetch(`${api}/users/${currentUsername.value}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    user.value = data
    // Fill profile form
    profileForm.name = data.name
    profileForm.email = data.email
    profileForm.username = data.username
    profileForm.isActive = data.active

    // Fetch activity once we have the user
    fetchActivity()
  } catch (err) {
    console.error(err)
    router.push('/login')
  } finally {
    loadingUser.value = false
  }
}

async function updateProfile() {
  savingProfile.value = true
  profileMsg.value = ''
  try {
    await $fetch(`${api}/users/${currentUsername.value}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: {
        name: profileForm.name,
        email: profileForm.email,
        username: profileForm.username,
        isActive: profileForm.isActive
      }
    })

    // Update local session if username changed
    if (profileForm.username !== currentUsername.value) {
      sessionStorage.setItem('username', profileForm.username)
      currentUsername.value = profileForm.username
    }
    sessionStorage.setItem('name', profileForm.name)

    profileMsg.value = 'Profile updated successfully'
    profileError.value = false
    fetchUser()
  } catch (err) {
    profileMsg.value = 'Error updating profile'
    profileError.value = true
  } finally {
    savingProfile.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) return
  changingPass.value = true
  passwordMsg.value = ''
  try {
    await $fetch(`${api}/users/${currentUsername.value}/change-password`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: passwordForm
    })
    passwordMsg.value = 'Password changed successfully'
    passwordError.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (err) {
    passwordMsg.value = 'Failed to change password'
    passwordError.value = true
  } finally {
    changingPass.value = false
  }
}

// Update the fetch function:
async function fetchActivity() {
  loadingActivity.value = true
  try {
    const data = await $fetch(`${api}/users/${currentUsername.value}/activity`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    // data is now {"uploads":1, "edits":0, ...}
    activities.value = data
  } catch (err) {
    console.error('Activity load failed', err)
  } finally {
    loadingActivity.value = false
  }
}

// --- HELPERS ---

function logout() {
  sessionStorage.clear()
  router.push('/')
}

const formatDateTime = (d) => {
  if (!d) return 'Recently'
  return new Date(d).toLocaleString([], {dateStyle: 'short', timeStyle: 'short'})
}
</script>

<style scoped>
/* Custom scrollbar for activity log */
.max-h-80::-webkit-scrollbar {
  width: 4px;
}

.max-h-80::-webkit-scrollbar-track {
  background: transparent;
}

.max-h-80::-webkit-scrollbar-thumb {
  background: #374151;
  border-radius: 10px;
}
</style>