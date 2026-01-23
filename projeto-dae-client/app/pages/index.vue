<template>
  <div class="min-h-screen bg-gray-900">
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

    <!-- Main Content -->
    <div class="grid place-items-center h-[calc(100vh-52px)]">
      <div class="bg-gray-800 p-8 rounded-xl shadow-lg w-full max-w-lg text-center mx-4">
        <h1 class="text-xl font-bold text-white mb-6">
          Welcome to Plataforma de Gestão de Publicações Científicas
        </h1>

        <!-- Conditional main button -->
        <div>
          <button
              v-if="isLoggedIn"
              @click="goToPublications"
              class="bg-gray-600 text-white px-6 py-2 rounded-md hover:bg-gray-700 transition-colors text-sm"
          >
            View Publications
          </button>

          <button
              v-else
              @click="goToLogin"
              class="bg-gray-600 text-white px-6 py-2 rounded-md hover:bg-gray-700 transition-colors text-sm"
          >
            Login to Grant Access
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from '#imports'

const router = useRouter()
const username = ref('')
const name = ref('')
const role = ref('')

// check if user is logged in
const isLoggedIn = computed(() => !!sessionStorage.getItem('auth_token'))

// read session info on mount
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
})

const goToLogin = () => router.push('/login')
const goToProfile = () => router.push('/me')
const goToPublications = () => router.push('/publications')
function logout() {
  sessionStorage.clear()
  router.push('/')
}
</script>
