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

    <!-- Content -->
    <div class="p-6 max-w-3xl mx-auto">
      <div class="bg-gray-800 p-6 rounded-xl shadow-lg">
        <h1 class="text-xl font-bold text-white mb-4">Create Publication</h1>

        <form @submit.prevent="submit" class="space-y-4">
          <!-- Title -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">Title</label>
            <input v-model="form.title" type="text" required
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"/>
          </div>

          <!-- Description -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">Description</label>
            <textarea v-model="form.description" rows="4"
                      class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"></textarea>
            <p class="text-xs text-gray-400 mt-1">Max 4000 chars (backend may enforce limits).</p>
          </div>

          <!-- Area -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">Area</label>
            <input v-model="form.area" type="text" placeholder="e.g. AI" required
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"/>
          </div>

          <!-- Authors (comma separated) -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">Authors (comma separated usernames)</label>
            <input v-model="authorsInput" type="text" placeholder="admin, alice" required
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"/>
            <p class="text-xs text-gray-400 mt-1">Enter author usernames. They must exist in the system.</p>
          </div>

          <!-- Tags (comma separated) -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">Tags (comma separated)</label>
            <input v-model="tagsInput" type="text" placeholder="AI, ML"
                   class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"/>
          </div>

          <!-- File input -->
          <div>
            <label class="block text-sm text-gray-300 mb-1">File</label>
            <input ref="fileInput" type="file" @change="onFileChange" required
                   class="w-full text-sm text-gray-300"/>
            <p v-if="fileName" class="text-xs text-gray-400 mt-1">Selected: {{ fileName }}</p>
          </div>

          <!-- Messages -->
          <p v-if="errorMessage" class="text-red-400 text-sm text-center">{{ errorMessage }}</p>
          <p v-if="successMessage" class="text-green-400 text-sm text-center">{{ successMessage }}</p>

          <!-- Controls -->
          <div class="flex items-center gap-3">
            <button type="submit" :disabled="loading"
                    class="bg-green-600 disabled:opacity-50 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors text-sm">
              {{ loading ? 'Uploading...' : 'Create Publication' }}
            </button>

            <button type="button" @click="goBack"
                    class="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors text-sm">
              Cancel
            </button>

            <!-- placeholder area for future filters / extras -->
            <div class="ml-auto text-xs text-gray-400">Filters placeholder</div>
          </div>
        </form>
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

const name = ref('')
const role = ref('')

onMounted(() => {
  if (!process.client) return

  name.value = sessionStorage.getItem('name') || ''
  role.value = sessionStorage.getItem('role') || ''
})


// form state
const form = reactive({
  title: '',
  description: '',
  area: ''
})
const authorsInput = ref('') // comma separated usernames
const tagsInput = ref('') // comma separated tags
const fileInput = ref(null)
const fileName = ref('')
const selectedFile = ref(null)

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

onMounted(() => {
  // ensure user is logged in
  const token = sessionStorage.getItem('auth_token')
  const username = sessionStorage.getItem('username')
  if (!token || !username) {
    router.push('/login')
  }
})

// handle file selection
function onFileChange(e) {
  const f = e.target.files?.[0] || null
  selectedFile.value = f
  fileName.value = f ? f.name : ''
}

// go back
function goBack() {
  router.push('/publications')
}

async function submit() {
  errorMessage.value = ''
  successMessage.value = ''

  // validation
  if (!form.title || !form.area) {
    errorMessage.value = 'Title and area are required.'
    return
  }
  if (!authorsInput.value.trim()) {
    errorMessage.value = 'At least one author is required.'
    return
  }
  if (!selectedFile.value) {
    errorMessage.value = 'Please select a file to upload.'
    return
  }

  loading.value = true

  // prepare publication DTO
  const publicationDto = {
    title: form.title,
    description: form.description,
    area: form.area,
    authors: authorsInput.value.split(',').map(s => s.trim()).filter(Boolean),
    tags: tagsInput.value.split(',').map(s => s.trim()).filter(Boolean)
  }

  try {
    const token = sessionStorage.getItem('auth_token')
    if (!token) throw new Error('No auth token')

    const fd = new FormData()
    fd.append('file', selectedFile.value, selectedFile.value.name)
    fd.append(
        'publication',
        new Blob([JSON.stringify(publicationDto)], {
          type: 'application/json'
        })
    )

    const res = await fetch(`${api}/publications`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`
        // Note: do not set Content-Type; the browser will set the multipart boundary
      },
      body: fd
    })

    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(text || `Upload failed: ${res.status}`)
    }

    const created = await res.json()
    successMessage.value = 'Publication created! Redirecting...'

    // Optionally store or use created object
    setTimeout(() => {
      router.push('/publications')
    }, 900)

  } catch (err) {
    console.error(err)
    errorMessage.value = err?.message || 'Upload failed'
  } finally {
    loading.value = false
  }
}
function logout() {
  sessionStorage.clear()
  router.push('/')
}
</script>