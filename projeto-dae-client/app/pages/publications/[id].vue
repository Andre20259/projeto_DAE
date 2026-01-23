<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Navbar -->
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
          <NuxtLink to="/login" class="text-gray-300 hover:text-white">Login</NuxtLink>
        </template>
      </div>
    </nav>

    <!-- Content -->
    <div class="p-6 max-w-6xl mx-auto">
      <div class="flex gap-6">
        <!-- Left: main info -->
        <div class="flex-1">
          <div v-if="loading" class="text-gray-400">Loading publication...</div>

          <div v-else-if="publication" class="bg-gray-800 p-6 rounded-xl shadow-lg">
            <div class="flex justify-between items-start">
              <div>
                <h1 class="text-2xl text-white font-bold mb-2">{{ publication.title }}</h1>
                <div class="text-gray-400 text-sm mb-3">
                  Area: <span class="text-gray-200">{{ publication.area }}</span>
                  • Uploaded: {{ formatDate(publication.uploadDate) }}
                </div>
              </div>

              <div class="text-right">
                <div class="text-gray-300 text-sm">Rating</div>
                <div class="text-yellow-300 font-semibold text-lg">
                  {{ publication.averageRating?.toFixed(1) || '0.0' }}
                </div>
              </div>
            </div>

            <p class="text-gray-300 my-4">{{ publication.description || 'No description' }}</p>

            <div class="flex flex-wrap gap-3 text-xs text-gray-400 mb-3">
              <div>Authors: {{ publication.authors.map(a => a.name).join(', ') }}</div>
              <div>Tags: {{ publication.tags.map(t => t.name).join(', ') }}</div>
            </div>

            <!-- Rating UI -->
            <div class="mb-4">
              <div class="text-sm text-gray-300 mb-1">Rate this publication</div>
              <div class="flex items-center gap-2">
                <template v-for="i in 5" :key="i">
                  <button
                      :aria-label="`Rate ${i} stars`"
                      @click="submitRating(i)"
                      class="text-yellow-400 text-2xl leading-none focus:outline-none"
                      :class="{'opacity-100': i <= selectedRating, 'opacity-40': i > selectedRating}"
                  >
                    ★
                  </button>
                </template>
                <div class="text-gray-400 text-sm ml-2">
                  (Your selection: {{ selectedRating || '—' }})
                </div>
              </div>
              <p v-if="ratingMessage" class="text-xs mt-1" :class="ratingMessageError ? 'text-red-400' : 'text-green-400'">
                {{ ratingMessage }}
              </p>
            </div>

            <!-- Comments -->
            <div class="mt-4">
              <h3 class="text-white font-semibold mb-2">Comments</h3>

              <div v-if="publication.comments?.length === 0" class="text-gray-400 mb-3">No comments yet.</div>

              <div v-for="c in publication.comments" :key="c.id" class="mb-3 border border-gray-700 p-3 rounded bg-gray-900">
                <div class="flex justify-between items-center">
                  <div class="text-gray-200 font-medium">{{ c.authorName || c.author || 'Unknown' }}</div>
                  <div class="text-gray-400 text-xs">{{ formatDateTime(c.timestamp) }}</div>
                </div>
                <div class="text-gray-300 mt-1">{{ c.content }}</div>
              </div>

              <!-- Add comment -->
              <div class="mt-4">
                <textarea v-model="newComment" rows="3"
                          placeholder="Write a comment..."
                          class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"></textarea>
                <div class="flex gap-2 mt-2 items-center">
                  <button @click="submitComment" :disabled="commentLoading"
                          class="bg-blue-600 disabled:opacity-50 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">
                    {{ commentLoading ? 'Posting...' : 'Post Comment' }}
                  </button>
                  <button @click="newComment = ''" class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700">Clear</button>
                  <p v-if="commentMessage" class="text-sm" :class="commentMessageError ? 'text-red-400' : 'text-green-400'">
                    {{ commentMessage }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-gray-400">Publication not found.</div>
        </div>

        <!-- Right: summary / actions panel -->
        <aside class="w-80">
          <div class="bg-gray-800 p-4 rounded-xl shadow-lg sticky top-6">
            <div class="flex justify-between items-center mb-3">
              <div class="text-sm text-gray-300 font-semibold">AI Summary</div>
              <button
                  @click="generateSummary"
                  :disabled="summaryLoading"
                  class="bg-indigo-600 disabled:opacity-50 text-white px-3 py-1 rounded text-sm hover:bg-indigo-700"
              >
                {{ summaryLoading ? 'Generating...' : 'Generate' }}
              </button>
            </div>

            <div class="text-gray-300 text-sm">
              <template v-if="publication?.summary">
                <div class="mb-2 text-gray-200 font-medium">Summary</div>
                <div class="text-gray-300 whitespace-pre-wrap">{{ publication.summary }}</div>
              </template>

              <template v-else-if="summaryLoading">
                <div class="text-gray-400">Summary is being generated — this can take a while...</div>
              </template>

              <template v-else>
                <div class="text-gray-400">No summary yet. Click Generate to create an AI summary.</div>
              </template>
            </div>
          </div>

          <!-- Quick actions -->
          <div class="mt-4">
            <div class="bg-gray-800 p-3 rounded-xl text-sm text-gray-300">
              <div class="mb-2">Quick actions</div>
              <button @click="downloadFile" class="w-full mb-2 bg-gray-700 hover:bg-gray-600 rounded py-2 text-white text-sm">Download file</button>
              <button @click="goBack" class="w-full bg-gray-600 hover:bg-gray-500 rounded py-2 text-white text-sm">Back to list</button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, useRuntimeConfig } from '#imports'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

// session
const username = ref('')
const name = ref('')
const role = ref('')
const token = ref('')

// publication state
const publication = ref(null)
const loading = ref(true)

// comments / rating state
const newComment = ref('')
const commentLoading = ref(false)
const commentMessage = ref('')
const commentMessageError = ref(false)

const selectedRating = ref(0)
const ratingMessage = ref('')
const ratingMessageError = ref(false)

// summary
const summaryLoading = ref(false)

onMounted(() => {
  if (process.client) {
    username.value = sessionStorage.getItem('username') || ''
    name.value = sessionStorage.getItem('name') || ''
    role.value = sessionStorage.getItem('role') || ''
    token.value = sessionStorage.getItem('auth_token') || ''
  }

  fetchPublication()
})

const id = Number(route.params.id)

// fetch publication
async function fetchPublication() {
  loading.value = true
  try {
    const res = await $fetch(`${api}/publications/${id}`, {
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    publication.value = res
  } catch (err) {
    console.error('Failed to fetch publication', err)
    publication.value = null
  } finally {
    loading.value = false
  }
}

// submit comment
async function submitComment() {
  if (!newComment.value.trim()) {
    commentMessage.value = 'Comment cannot be empty'
    commentMessageError.value = true
    return
  }
  commentLoading.value = true
  commentMessage.value = ''
  commentMessageError.value = false
  try {
    await $fetch(`${api}/publications/${id}/comments`, {
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      body: { content: newComment.value }
    })
    commentMessage.value = 'Comment posted'
    commentMessageError.value = false
    newComment.value = ''
    await fetchPublication()
  } catch (err) {
    console.error(err)
    commentMessage.value = 'Failed to post comment'
    commentMessageError.value = true
  } finally {
    commentLoading.value = false
  }
}

// submit rating (1-5)
async function submitRating(score) {
  selectedRating.value = score
  ratingMessage.value = ''
  ratingMessageError.value = false
  try {
    await $fetch(`${api}/publications/${id}/ratings`, {
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      body: { score }
    })
    ratingMessage.value = 'Rating submitted'
    // refresh to get updated average and ratings
    await fetchPublication()
  } catch (err) {
    console.error(err)
    ratingMessage.value = 'Failed to submit rating'
    ratingMessageError.value = true
  }
}

// generate summary (GET /publications/{id}/summary)
async function generateSummary() {
  if (summaryLoading.value) return
  summaryLoading.value = true
  try {
    const dto = await $fetch(`${api}/publications/${id}/summary`, {
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    // dto should contain { id, summary }
    if (dto && typeof dto.summary === 'string') {
      // update publication.summary locally and show
      if (!publication.value) publication.value = {}
      publication.value.summary = dto.summary
    }
  } catch (err) {
    console.error('Summary generation failed', err)
  } finally {
    summaryLoading.value = false
  }
}

// helper actions
function goBack() {
  router.push('/publications')
}
function goToProfile() {
  router.push('/me')
}
function createPublication() {
  router.push('/publications/create')
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}
function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}
function downloadFile() {
  // If your backend has a download endpoint, use it; otherwise try to open the filename route
  // Example: GET /publications/{id}/file (adjust if needed)
  const url = `${api}/publications/${id}/file`
  // open in new tab (browser will need correct headers & auth)
  window.open(url, '_blank')
}
</script>

<style scoped>
/* small nicety: make stars cursor pointer */
button[aria-label^="Rate"] {
  cursor: pointer;
  background: transparent;
  border: none;
}
</style>
