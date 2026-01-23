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

    <div class="p-6 max-w-6xl mx-auto">
      <div class="flex gap-6">
        <div class="flex-1">
          <div v-if="loading" class="text-gray-400">Loading publication...</div>

          <div v-else-if="publication" class="bg-gray-800 p-6 rounded-xl shadow-lg">

            <div class="mb-6 p-4 bg-gray-900/50 border border-gray-700 rounded-lg flex flex-wrap items-center justify-between gap-4">
              <div class="flex items-center gap-4">
                <span class="text-xs font-bold uppercase text-gray-500 tracking-wider">Management</span>

                <button v-if="token" @click="toggleEditMode"
                        class="px-3 py-1 text-sm bg-blue-600 hover:bg-blue-700 text-white rounded transition shadow-sm">
                  {{ isEditingPub ? 'Cancel' : 'Edit Publication' }}
                </button>

                <button v-if="canToggleVisibility" @click="togglePubVisibility" :disabled="updatingPub"
                        class="px-3 py-1 text-sm border border-gray-600 hover:bg-gray-700 text-white rounded transition disabled:opacity-50">
                  {{ updatingPub ? '...' : (publication.isVisible ? 'Set Private' : 'Set Public') }}
                </button>
              </div>

              <div v-if="!publication.isVisible" class="text-xs text-orange-400 font-medium px-2 py-1 bg-orange-950/30 rounded border border-orange-900/50">
                Status: Hidden from Public
              </div>
            </div>

            <div v-if="isEditingPub" class="mb-8 p-6 bg-gray-700/30 rounded-xl border border-blue-500/30">
              <h2 class="text-white font-bold mb-4">Edit Details</h2>
              <div class="space-y-4">
                <div>
                  <label class="block text-xs text-gray-400 mb-1">Title</label>
                  <input v-model="editForm.title" type="text" class="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white focus:ring-1 focus:ring-blue-500 outline-none" />
                </div>
                <div>
                  <label class="block text-xs text-gray-400 mb-1">Area</label>
                  <input v-model="editForm.area" type="text" class="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white focus:ring-1 focus:ring-blue-500 outline-none" />
                </div>
                <div>
                  <label class="block text-xs text-gray-400 mb-1">Description</label>
                  <textarea v-model="editForm.description" rows="4" class="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white focus:ring-1 focus:ring-blue-500 outline-none"></textarea>
                </div>
                <div class="flex items-center gap-3">
                  <button @click="updatePublication" :disabled="updatingPub" class="bg-green-600 hover:bg-green-700 text-white px-4 py-1.5 rounded text-sm font-bold">
                    {{ updatingPub ? 'Saving...' : 'Save Changes' }}
                  </button>
                  <p v-if="pubMessage" :class="pubMessageError ? 'text-red-400' : 'text-green-400'" class="text-xs">{{ pubMessage }}</p>
                </div>
              </div>
            </div>

            <div v-else>
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
                  <div class="text-yellow-300 font-semibold text-lg">{{ publication.averageRating?.toFixed(1) || '0.0' }}</div>
                </div>
              </div>
              <p class="text-gray-300 my-4 whitespace-pre-wrap">{{ publication.description || 'No description' }}</p>
              <div class="flex flex-wrap gap-3 text-xs text-gray-400 mb-3">
                <div>Authors: {{ publication.authors?.map(a => a.name).join(', ') }}</div>
                <div>Tags: {{ tags.map(t => t.name).join(', ') }}</div>
              </div>
            </div>

            <hr class="border-gray-700 my-6" />

            <div class="mb-4">
              <div class="text-sm text-gray-300 mb-1">Rate this publication</div>
              <div class="flex items-center gap-2">
                <template v-for="i in 5" :key="i">
                  <button @click="handleRatingClick(i)" class="text-2xl leading-none focus:outline-none" :class="i <= selectedRating ? 'text-yellow-400' : 'text-gray-500'" :disabled="ratingLoading">★</button>
                </template>
                <div class="text-gray-400 text-sm ml-2">(Your selection: {{ selectedRating || '—' }})</div>
              </div>
              <p v-if="ratingMessage" class="text-xs mt-1" :class="ratingError ? 'text-red-400' : 'text-green-400'">{{ ratingMessage }}</p>
            </div>

            <div class="mt-8">
              <h3 class="text-white font-semibold mb-3">Comments</h3>
              <div v-if="!(publication.comments && publication.comments.length)" class="text-gray-400 mb-3">No public comments yet.</div>
              <div v-for="c in publication.comments" :key="c.id" class="mb-3 border border-gray-700 p-3 rounded bg-gray-900">
                <div class="flex justify-between items-start">
                  <div>
                    <div class="text-gray-200 font-medium">{{ c.author }}</div>
                    <div class="text-gray-400 text-xs mt-1">{{ formatDateTime(c.created_at) }}</div>
                  </div>
                  <div class="flex items-center gap-2">
                    <button v-if="canToggleVisibility" @click="toggleCommentVisibility(c)" :disabled="togglingVisibilityId === c.id" class="text-xs px-2 py-1 rounded border border-gray-600 bg-gray-800 text-gray-200">
                      {{ togglingVisibilityId === c.id ? '...' : (c.visible ? 'Hide' : 'Unhide') }}
                    </button>
                    <button v-if="c.author === username" @click="startCommentEdit(c)" class="text-xs text-blue-400 hover:underline">Edit</button>
                    <button v-if="c.author === username || role === 'Administrator'" @click="deleteComment(c)" class="text-xs text-red-400 hover:underline">Delete</button>
                  </div>
                </div>
                <div class="text-gray-300 mt-3">{{ c.content }}</div>
              </div>

              <div class="mt-4">
                <textarea v-model="commentText" rows="3" placeholder="Write a comment..." class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"></textarea>
                <div class="flex gap-2 mt-2 items-center">
                  <button @click="submitComment" :disabled="commentLoading" class="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">
                    {{ editingCommentId ? 'Update Comment' : 'Post Comment' }}
                  </button>
                  <button v-if="editingCommentId" @click="cancelCommentEdit" class="bg-gray-600 text-white px-3 py-1 rounded text-sm">Cancel</button>
                </div>
              </div>
            </div>

            <div v-if="canToggleVisibility" class="mt-10 border-t border-gray-700 pt-6">
              <div class="flex items-center justify-between mb-4">
                <div class="text-sm text-gray-300 font-semibold uppercase tracking-wider">Hidden Comments Log</div>
                <div class="flex gap-2">
                  <button @click="fetchHiddenComments" :disabled="hiddenLoading" class="bg-gray-700 text-white px-3 py-1 rounded text-xs hover:bg-gray-600">
                    {{ hiddenLoading ? 'Loading...' : 'Refresh' }}
                  </button>
                  <button @click="showHidden = !showHidden" class="bg-gray-600 text-white px-3 py-1 rounded text-xs hover:bg-gray-500">
                    {{ showHidden ? 'Hide Section' : 'Show Section' }}
                  </button>
                </div>
              </div>

              <div v-if="showHidden">
                <div v-if="hiddenComments.length === 0" class="text-gray-500 italic text-sm">No hidden comments found.</div>
                <div v-for="hc in hiddenComments" :key="hc.id" class="mb-3 border border-dashed border-gray-600 p-3 rounded bg-gray-900/40">
                  <div class="flex justify-between items-start">
                    <div>
                      <div class="text-gray-400 font-medium">{{ hc.author }} <span class="text-[10px] bg-red-900/20 text-red-400 px-1 ml-2 rounded">HIDDEN</span></div>
                      <div class="text-gray-500 text-[10px] mt-1">{{ formatDateTime(hc.created_at) }}</div>
                    </div>
                    <div class="flex items-center gap-2">
                      <button @click="toggleCommentVisibility(hc)" :disabled="togglingVisibilityId === hc.id" class="text-xs px-2 py-1 bg-green-900/20 text-green-400 rounded border border-green-900/50 hover:bg-green-900/40">Unhide</button>
                      <button @click="deleteComment(hc)" class="text-xs text-red-400 hover:underline">Delete Permanently</button>
                    </div>
                  </div>
                  <div class="text-gray-400 mt-3 italic">{{ hc.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <aside class="w-80">
          <div class="bg-gray-800 p-4 rounded-xl shadow-lg sticky top-6">
            <div class="flex justify-between items-center mb-3">
              <div class="text-sm text-gray-300 font-semibold">AI Summary</div>
              <button
                  @click="generateSummary"
                  :disabled="summaryLoading"
                  class="bg-indigo-600 text-white px-3 py-1 rounded text-sm hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                <span v-if="summaryLoading" class="animate-spin text-xs">🌀</span>
                {{ summaryLoading ? 'Thinking...' : 'Generate' }}
              </button>
            </div>
            <div class="text-gray-300 text-sm mb-3">
              <div v-if="summaryLoading" class="text-gray-400 italic animate-pulse">
                Generating summary...
              </div>
              <div v-else-if="publication?.summary" class="whitespace-pre-wrap bg-gray-900/30 p-2 rounded border border-indigo-500/20">
                {{ publication.summary }}
              </div>
              <div v-else class="text-gray-400 italic">
                No summary available.
              </div>
            </div>
            <div class="mt-4" v-if="publication">
              <div class="text-sm text-gray-300 font-semibold mb-2">Tags</div>
              <div class="flex flex-wrap gap-2 mb-3">
                <span v-for="t in tags" :key="t.name" class="bg-gray-700 text-gray-200 px-2 py-1 rounded text-xs flex items-center">
                  {{ t.name }}
                  <button @click="removeTag(t.name)" class="ml-1 text-red-400">×</button>
                </span>
              </div>
              <div class="flex gap-2">
                <input v-model="tagInput" type="text" placeholder="Add tag" class="flex-1 px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm outline-none" />
                <button @click="addTag" class="bg-green-600 text-white px-2 py-1 rounded text-sm">Add</button>
              </div>
            </div>
          </div>
          <div class="mt-4 space-y-2">
            <button @click="downloadFile" class="w-full bg-gray-800 p-3 rounded-xl text-white text-sm hover:bg-gray-700 transition">Download File</button>
            <button @click="goBack" class="w-full bg-gray-800 p-3 rounded-xl text-white text-sm hover:bg-gray-700 transition">Back to List</button>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import {useRoute, useRouter, useRuntimeConfig} from '#imports'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase
const id = Number(route.params.id)

// User / Session
const username = ref('')
const name = ref('')
const role = ref('')
const token = ref('')

// Publication Data
const publication = ref(null)
const loading = ref(true)

// Publication Actions (Edit & Visibility)
const isEditingPub = ref(false)
const updatingPub = ref(false)
const pubMessage = ref('')
const pubMessageError = ref(false)
const editForm = reactive({title: '', description: '', area: ''})

// Comments State
const commentText = ref('')
const editingCommentId = ref(null)
const commentLoading = ref(false)
const togglingVisibilityId = ref(null)
const hiddenComments = ref([])
const hiddenLoading = ref(false)
const showHidden = ref(false)

// Other State
const tagInput = ref('')
const tagLoading = ref(false)
const selectedRating = ref(0)
const ratingLoading = ref(false)
const ratingMessage = ref('')
const ratingError = ref(false)
const summaryLoading = ref(false)

// Computed
const tags = computed(() => publication.value?.tags ?? [])
const canToggleVisibility = computed(() => role.value === 'Administrator' || role.value === 'Responsible')

onMounted(() => {
  if (process.client) {
    username.value = sessionStorage.getItem('username') || ''
    name.value = sessionStorage.getItem('name') || ''
    role.value = sessionStorage.getItem('role') || ''
    token.value = sessionStorage.getItem('auth_token') || ''
  }
  fetchPublication()
})

async function fetchPublication() {
  loading.value = true
  try {
    const res = await $fetch(`${api}/publications/${id}`, {
      headers: token.value ? {Authorization: `Bearer ${token.value}`} : {}
    })
    publication.value = res
    // Load edit form
    editForm.title = res.title
    editForm.description = res.description
    editForm.area = res.area
    // Load rating
    const ur = res.ratings?.find(r => r.author === username.value)
    selectedRating.value = ur ? (ur.score ?? 0) : 0
  } catch (err) {
    console.error('Fetch failed', err)
  } finally {
    loading.value = false
  }
}

// 1. UPDATE PUBLICATION (Available to all logged-in users)
function toggleEditMode() {
  isEditingPub.value = !isEditingPub.value
  pubMessage.value = ''
}

async function updatePublication() {
  updatingPub.value = true
  pubMessage.value = ''
  try {
    await $fetch(`${api}/publications/${id}`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json'},
      body: {
        title: editForm.title,
        description: editForm.description,
        area: editForm.area,
        authors: publication.value.authors.map(a => a.username),
        tags: tags.value.map(t => t.name)
      }
    })
    pubMessage.value = 'Publication updated'
    pubMessageError.value = false
    await fetchPublication()
    isEditingPub.value = false
  } catch (err) {
    pubMessage.value = 'Error updating publication'
    pubMessageError.value = true
  } finally {
    updatingPub.value = false
  }
}

// 2. VISIBILITY (Admins/Responsible Only)
async function togglePubVisibility() {
  updatingPub.value = true
  try {
    const nextStatus = !publication.value.isVisible
    await $fetch(`${api}/publications/${id}`, {
      method: 'PATCH',
      headers: {Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json'},
      body: {visibility: String(nextStatus)}
    })
    await fetchPublication()
  } catch (err) {
    console.error('PATCH visibility failed', err)
  } finally {
    updatingPub.value = false
  }
}

// 3. COMMENTS & HIDDEN MANAGEMENT
async function fetchHiddenComments() {
  if (!canToggleVisibility.value) return
  hiddenLoading.value = true
  try {
    hiddenComments.value = await $fetch(`${api}/publications/${id}/comments/hidden`, {
      headers: {Authorization: `Bearer ${token.value}`}
    })
  } catch (err) {
    console.error('Hidden fetch failed', err)
  } finally {
    hiddenLoading.value = false
  }
}

async function toggleCommentVisibility(c) {
  togglingVisibilityId.value = c.id
  try {
    await $fetch(`${api}/publications/${id}/comments/${c.id}/visibility`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {visible: !c.visible}
    })
    await fetchPublication()
    if (showHidden.value) await fetchHiddenComments()
  } finally {
    togglingVisibilityId.value = null
  }
}

function startCommentEdit(c) {
  editingCommentId.value = c.id
  commentText.value = c.content
}

function cancelCommentEdit() {
  editingCommentId.value = null
  commentText.value = ''
}

async function submitComment() {
  if (!commentText.value.trim()) return
  commentLoading.value = true
  try {
    const url = editingCommentId.value ? `${api}/publications/${id}/comments/${editingCommentId.value}` : `${api}/publications/${id}/comments`
    await $fetch(url, {
      method: editingCommentId.value ? 'PUT' : 'POST',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {content: commentText.value}
    })
    await fetchPublication()
    cancelCommentEdit()
  } finally {
    commentLoading.value = false
  }
}

async function deleteComment(c) {
  if (!confirm('Permanently delete this comment?')) return
  try {
    await $fetch(`${api}/publications/${id}/comments/${c.id}`, {
      method: 'DELETE',
      headers: {Authorization: `Bearer ${token.value}`}
    })
    await fetchPublication()
    if (showHidden.value) await fetchHiddenComments()
  } catch (err) {
    console.error('Delete failed', err)
  }
}

// 4. RATINGS / TAGS / SUMMARY
async function handleRatingClick(score) {
  ratingLoading.value = true
  ratingMessage.value = ''
  try {
    const ur = publication.value.ratings?.find(r => r.author === username.value)
    if (!ur) {
      await $fetch(`${api}/publications/${id}/ratings`, {
        method: 'POST',
        headers: {Authorization: `Bearer ${token.value}`},
        body: {score}
      })
    } else if (ur.score === score) {
      await $fetch(`${api}/publications/${id}/ratings/${ur.id}`, {
        method: 'DELETE',
        headers: {Authorization: `Bearer ${token.value}`}
      })
    } else {
      await $fetch(`${api}/publications/${id}/ratings/${ur.id}`, {
        method: 'PUT',
        headers: {Authorization: `Bearer ${token.value}`},
        body: {score}
      })
    }
    await fetchPublication()
  } finally {
    ratingLoading.value = false
  }
}

async function addTag() {
  if (!tagInput.value.trim()) return
  tagLoading.value = true
  try {
    await $fetch(`${api}/publications/${id}/tags`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {name: tagInput.value, action: 'add'}
    })
    tagInput.value = ''
    await fetchPublication()
  } finally {
    tagLoading.value = false
  }
}

async function removeTag(tagName) {
  try {
    await $fetch(`${api}/publications/${id}/tags`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {name: tagName, action: 'remove'}
    })
    await fetchPublication()
  } catch (err) {
    console.error(err)
  }
}

async function generateSummary() {
  if (summaryLoading.value) return; // Prevent double-clicks

  summaryLoading.value = true;
  try {
    // We use method: 'GET' (or POST if your API requires it)
    // and ensure we handle the response correctly
    const dto = await $fetch(`${api}/publications/${id}/summary`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token.value}` }
    });

    if (dto && dto.summary) {
      publication.value.summary = dto.summary;
    }
  } catch (err) {
    console.error('Summary generation failed:', err);
    // Optional: Show an error message to the user
  } finally {
    // This ensures the button re-enables regardless of success/fail
    summaryLoading.value = false;
  }
}

// Helpers
const formatDate = d => d ? new Date(d).toLocaleDateString() : ''
const formatDateTime = d => d ? new Date(d).toLocaleString() : ''
const goBack = () => router.push('/publications')
const goToProfile = () => router.push('/me')
const downloadFile = () => window.open(`${api}/publications/download/${id}`, '_blank')
</script>