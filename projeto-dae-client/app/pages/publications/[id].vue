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
        <!-- Left: main info / comments -->
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
              <div>Tags: {{ tags.map(t => t.name).join(', ') }}</div>
            </div>

            <!-- Rating UI -->
            <div class="mb-4">
              <div class="text-sm text-gray-300 mb-1">Rate this publication</div>
              <div class="flex items-center gap-2">
                <template v-for="i in 5" :key="i">
                  <button
                      :aria-label="`Rate ${i} stars`"
                      @click="handleRatingClick(i)"
                      class="text-2xl leading-none focus:outline-none"
                      :class="i <= selectedRating ? 'text-yellow-400' : 'text-gray-500'"
                      :disabled="ratingLoading"
                  >
                    ★
                  </button>
                </template>
                <div class="text-gray-400 text-sm ml-2">
                  (Your selection: {{ selectedRating || '—' }})
                </div>
              </div>
              <p v-if="ratingMessage" class="text-xs mt-1" :class="ratingError ? 'text-red-400' : 'text-green-400'">
                {{ ratingMessage }}
              </p>
            </div>

            <!-- Comments -->
            <div class="mt-4">
              <h3 class="text-white font-semibold mb-3">Comments</h3>

              <div v-if="!(publication.comments && publication.comments.length)" class="text-gray-400 mb-3">
                No comments yet.
              </div>

              <div v-for="c in publication.comments" :key="c.id" class="mb-3 border border-gray-700 p-3 rounded bg-gray-900">
                <div class="flex justify-between items-start">
                  <div>
                    <div class="text-gray-200 font-medium">{{ c.author }}</div>
                    <div class="text-gray-400 text-xs mt-1">{{ formatDateTime(c.created_at) }}</div>
                  </div>

                  <div class="flex items-center gap-2">
                    <!-- Visibility toggle (only for Responsible or Administrator) -->
                    <button
                        v-if="canToggleVisibility"
                        @click="toggleVisibility(c)"
                        :disabled="togglingVisibilityId === c.id"
                        class="text-xs px-2 py-1 rounded border border-gray-600 bg-gray-800 hover:bg-gray-700 text-gray-200"
                        :title="c.visible ? 'Hide comment' : 'Show comment'"
                    >
                      {{ togglingVisibilityId === c.id ? '...' : (c.visible ? 'Visible' : 'Hidden') }}
                    </button>

                    <!-- Edit button (only owner) -->
                    <button
                        v-if="isMyComment(c)"
                        @click="startEdit(c)"
                        class="text-xs text-blue-400 hover:underline"
                    >
                      Edit
                    </button>

                    <!-- Delete button (owner OR Administrator) -->
                    <button
                        v-if="canDeleteComment(c)"
                        @click="deleteComment(c)"
                        :disabled="deletingCommentId === c.id"
                        class="text-xs text-red-400 hover:underline"
                    >
                      {{ deletingCommentId === c.id ? 'Deleting...' : 'Delete' }}
                    </button>
                  </div>
                </div>

                <div class="text-gray-300 mt-3">{{ c.content }}</div>
              </div>

              <!-- Create / Edit comment -->
              <div class="mt-4">
                <textarea v-model="commentText" rows="3"
                          placeholder="Write a comment..."
                          class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-white focus:outline-none"></textarea>
                <div class="flex gap-2 mt-2 items-center">
                  <button @click="submitComment" :disabled="commentLoading"
                          class="bg-blue-600 disabled:opacity-50 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">
                    {{ editingCommentId ? 'Update Comment' : 'Post Comment' }}
                  </button>

                  <button v-if="editingCommentId" @click="cancelEdit"
                          class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700">
                    Cancel
                  </button>

                  <p v-if="commentMessage" class="text-sm" :class="commentMessageError ? 'text-red-400' : 'text-green-400'">
                    {{ commentMessage }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Hidden-comments controls (for Admin/Responsible) -->
            <div v-if="canToggleVisibility" class="mt-6 border-t border-gray-700 pt-4">
              <div class="flex items-center justify-between mb-3">
                <div class="text-sm text-gray-300 font-semibold">Hidden comments</div>
                <div class="flex items-center gap-2">
                  <button @click="fetchHiddenComments" :disabled="hiddenLoading"
                          class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700">
                    {{ hiddenLoading ? 'Loading…' : 'Refresh hidden' }}
                  </button>
                  <button @click="showHidden = !showHidden" class="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
                    {{ showHidden ? 'Hide' : 'Show' }}
                  </button>
                </div>
              </div>

              <div v-if="showHidden">
                <div v-if="hiddenLoading" class="text-gray-400">Loading hidden comments…</div>
                <div v-else-if="hiddenComments.length === 0" class="text-gray-400">No hidden comments.</div>

                <div v-for="hc in hiddenComments" :key="hc.id" class="mb-3 border border-gray-700 p-3 rounded bg-gray-900">
                  <div class="flex justify-between items-start">
                    <div>
                      <div class="text-gray-200 font-medium">{{ hc.author }}</div>
                      <div class="text-gray-400 text-xs mt-1">{{ formatDateTime(hc.created_at) }}</div>
                    </div>

                    <div class="flex items-center gap-2">
                      <button
                          @click="toggleVisibility(hc)"
                          :disabled="togglingVisibilityId === hc.id"
                          class="text-xs px-2 py-1 rounded border border-gray-600 bg-gray-800 hover:bg-gray-700 text-gray-200"
                      >
                        {{ togglingVisibilityId === hc.id ? '...' : (hc.visible ? 'Visible' : 'Hidden') }}
                      </button>

                      <button
                          @click="deleteComment(hc)"
                          :disabled="deletingCommentId === hc.id"
                          class="text-xs text-red-400 hover:underline"
                      >
                        {{ deletingCommentId === hc.id ? 'Deleting...' : 'Delete' }}
                      </button>
                    </div>
                  </div>

                  <div class="text-gray-300 mt-3">{{ hc.content }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-gray-400">Publication not found.</div>
        </div>

        <!-- Right: summary / tags / quick actions -->
        <aside class="w-80">
          <!-- Summary -->
          <div class="bg-gray-800 p-4 rounded-xl shadow-lg sticky top-6">
            <div class="flex justify-between items-center mb-3">
              <div class="text-sm text-gray-300 font-semibold">AI Summary</div>
              <button
                  @click="generateSummary"
                  :disabled="summaryLoading || !publication"
                  class="bg-indigo-600 disabled:opacity-50 text-white px-3 py-1 rounded text-sm hover:bg-indigo-700"
              >
                {{ summaryLoading ? 'Generating...' : 'Generate' }}
              </button>
            </div>

            <div class="text-gray-300 text-sm mb-3">
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

            <!-- Tags management (uses safe `tags` computed) -->
            <div class="mt-2" v-if="publication">
              <div class="text-sm text-gray-300 font-semibold mb-2">Tags</div>

              <div class="flex flex-wrap gap-2 mb-3">
                <template v-for="t in tags" :key="t.name">
                  <span class="inline-flex items-center gap-2 bg-gray-700 text-gray-200 px-2 py-1 rounded text-xs">
                    {{ t.name }}
                    <button
                        @click="removeTag(t.name)"
                        :disabled="tagLoading || !token"
                        class="text-red-400 hover:text-red-200 ml-1 text-xs"
                        title="Remove tag"
                    >
                      ×
                    </button>
                  </span>
                </template>
                <span v-if="tags.length === 0" class="text-gray-400 text-xs">No tags</span>
              </div>

              <div class="flex gap-2">
                <input v-model="tagInput" type="text" placeholder="New tag" class="flex-1 px-2 py-1 bg-gray-700 border border-gray-600 rounded text-white text-sm focus:outline-none" />
                <button @click="addTag" :disabled="!tagInput.trim() || tagLoading || !token" class="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700">
                  {{ tagLoading ? '...' : 'Add' }}
                </button>
              </div>

              <p v-if="tagMessage" class="text-xs mt-2" :class="tagError ? 'text-red-400' : 'text-green-400'">
                {{ tagMessage }}
              </p>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter, useRuntimeConfig } from '#imports'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()
const api = config.public.apiBase

const id = Number(route.params.id)

const username = ref('')
const name = ref('')
const role = ref('')
const token = ref('')

const publication = ref(null)
const loading = ref(true)

/* COMMENTS */
const commentText = ref('')
const editingCommentId = ref(null)
const commentLoading = ref(false)
const commentMessage = ref('')
const commentMessageError = ref(false)

const deletingCommentId = ref(null)
const togglingVisibilityId = ref(null)

/* Hidden comments */
const hiddenComments = ref([])
const hiddenLoading = ref(false)
const showHidden = ref(false)

/* RATING */
const selectedRating = ref(0)
const ratingLoading = ref(false)
const ratingMessage = ref('')
const ratingError = ref(false)

/* SUMMARY */
const summaryLoading = ref(false)

/* TAGS */
const tagInput = ref('')
const tagLoading = ref(false)
const tagMessage = ref('')
const tagError = ref(false)

// safe computed tags array
const tags = computed(() => publication.value?.tags ?? [])

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
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    publication.value = res

    // initialise selectedRating from server-side rating (if any)
    const ur = findUserRating(publication.value)
    selectedRating.value = ur ? (ur.score ?? ur.rating ?? ur.value ?? 0) : 0
  } catch (err) {
    publication.value = null
    console.error('Failed to fetch publication', err)
  } finally {
    loading.value = false
  }
}

/* COMMENTS helpers */
function isMyComment(c) {
  return c && c.author === username.value
}

function canDeleteComment(c) {
  return isMyComment(c) || role.value === 'Administrator'
}

const canToggleVisibility = computed(() => {
  return role.value === 'Administrator' || role.value === 'Responsible'
})

function startEdit(c) {
  editingCommentId.value = c.id
  commentText.value = c.content
}

function cancelEdit() {
  editingCommentId.value = null
  commentText.value = ''
  commentMessage.value = ''
  commentMessageError.value = false
}

async function submitComment() {
  if (!commentText.value.trim()) {
    commentMessage.value = 'Comment cannot be empty'
    commentMessageError.value = true
    return
  }

  commentLoading.value = true
  commentMessage.value = ''
  commentMessageError.value = false

  try {
    if (editingCommentId.value) {
      // UPDATE
      await $fetch(`${api}/publications/${id}/comments/${editingCommentId.value}`, {
        method: 'PUT',
        headers: {Authorization: `Bearer ${token.value}`},
        body: {content: commentText.value}
      })
      commentMessage.value = 'Comment updated'
    } else {
      // CREATE
      await $fetch(`${api}/publications/${id}/comments`, {
        method: 'POST',
        headers: {Authorization: `Bearer ${token.value}`},
        body: {content: commentText.value}
      })
      commentMessage.value = 'Comment posted'
    }

    // refresh list and reset editor
    await fetchPublication()
    cancelEdit()
  } catch (err) {
    console.error(err)
    commentMessage.value = 'Failed to submit comment'
    commentMessageError.value = true
  } finally {
    commentLoading.value = false
  }
}

async function deleteComment(c) {
  const ok = confirm('Delete this comment?')
  if (!ok) return

  deletingCommentId.value = c.id
  commentMessage.value = ''
  commentMessageError.value = false

  try {
    await $fetch(`${api}/publications/${id}/comments/${c.id}`, {
      method: 'DELETE',
      headers: {Authorization: `Bearer ${token.value}`}
    })
    commentMessage.value = 'Comment deleted'
    commentMessageError.value = false
    // refresh both lists
    await fetchPublication()
    if (showHidden.value) await fetchHiddenComments()
  } catch (err) {
    console.error('Delete failed', err)
    commentMessage.value = 'Failed to delete comment'
    commentMessageError.value = true
  } finally {
    deletingCommentId.value = null
  }
}

async function toggleVisibility(c) {
  if (!canToggleVisibility.value) return

  togglingVisibilityId.value = c.id
  commentMessage.value = ''
  commentMessageError.value = false

  try {
    const newVisible = !c.visible
    await $fetch(`${api}/publications/${id}/comments/${c.id}/visibility`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {visible: newVisible}
    })
    commentMessage.value = `Comment ${newVisible ? 'made visible' : 'hidden'}`
    commentMessageError.value = false
    await fetchPublication()
    if (showHidden.value) await fetchHiddenComments()
  } catch (err) {
    console.error('Toggle visibility failed', err)
    commentMessage.value = 'Failed to toggle visibility'
    commentMessageError.value = true
  } finally {
    togglingVisibilityId.value = null
  }
}

/* Hidden comments: fetch endpoint only for privileged roles */
async function fetchHiddenComments() {
  if (!canToggleVisibility.value) return
  hiddenLoading.value = true
  try {
    hiddenComments.value = await $fetch(`${api}/publications/${id}/comments/hidden`, {
      headers: token.value ? {Authorization: `Bearer ${token.value}`} : {}
    })
  } catch (err) {
    console.error('Failed to fetch hidden comments', err)
    hiddenComments.value = []
  } finally {
    hiddenLoading.value = false
  }
}

/* RATING helpers */
// find user's rating object
function findUserRating(pub) {
  if (!pub || !Array.isArray(pub.ratings)) return null
  return pub.ratings.find(r => r && (r.author === username.value || r.username === username.value || r.user === username.value)) || null
}

// handle click on a star (optimistic and correct behavior)
async function handleRatingClick(score) {
  if (ratingLoading.value) return
  ratingLoading.value = true
  ratingMessage.value = ''
  ratingError.value = false

  const prevSelection = selectedRating.value
  selectedRating.value = score // optimistic

  try {
    const ur = findUserRating(publication.value)

    if (!ur) {
      // No existing rating -> create
      await $fetch(`${api}/publications/${id}/ratings`, {
        method: 'POST',
        headers: {Authorization: `Bearer ${token.value}`},
        body: {score}
      })
      ratingMessage.value = 'Rating created'
    } else {
      const ratingId = ur.id
      const existingScore = ur.score ?? ur.rating ?? ur.value ?? null

      if (existingScore === score) {
        // same score clicked -> delete rating
        await $fetch(`${api}/publications/${id}/ratings/${ratingId}`, {
          method: 'DELETE',
          headers: {Authorization: `Bearer ${token.value}`}
        })
        selectedRating.value = 0
        ratingMessage.value = 'Rating removed'
      } else {
        // different score -> update
        await $fetch(`${api}/publications/${id}/ratings/${ratingId}`, {
          method: 'PUT',
          headers: {Authorization: `Bearer ${token.value}`},
          body: {score}
        })
        selectedRating.value = score
        ratingMessage.value = 'Rating updated'
      }
    }

    // refresh server state (avg rating, ratings list)
    await fetchPublication()
  } catch (err) {
    console.error('Rating action failed', err)
    ratingMessage.value = 'Failed to submit rating'
    ratingError.value = true
    // revert optimistic selection to last known server value
    const ur2 = findUserRating(publication.value)
    selectedRating.value = ur2 ? (ur2.score ?? ur2.rating ?? ur2.value ?? 0) : prevSelection
  } finally {
    ratingLoading.value = false
  }
}

/* TAGS: add/remove */
async function addTag() {
  const name = tagInput.value.trim()
  if (!name) return
  if (!token.value) {
    tagMessage.value = 'You must be logged in to edit tags'
    tagError.value = true
    return
  }
  tagLoading.value = true
  tagMessage.value = ''
  tagError.value = false
  try {
    await $fetch(`${api}/publications/${id}/tags`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {name, action: 'add'}
    })
    tagMessage.value = `Tag "${name}" added`
    tagInput.value = ''
    await fetchPublication()
  } catch (err) {
    console.error('Add tag failed', err)
    tagMessage.value = 'Failed to add tag'
    tagError.value = true
  } finally {
    tagLoading.value = false
  }
}

async function removeTag(tagName) {
  if (!confirm(`Remove tag "${tagName}"?`)) return
  if (!token.value) {
    tagMessage.value = 'You must be logged in to edit tags'
    tagError.value = true
    return
  }
  tagLoading.value = true
  tagMessage.value = ''
  tagError.value = false
  try {
    await $fetch(`${api}/publications/${id}/tags`, {
      method: 'PUT',
      headers: {Authorization: `Bearer ${token.value}`},
      body: {name: tagName, action: 'remove'}
    })
    tagMessage.value = `Tag "${tagName}" removed`
    await fetchPublication()
  } catch (err) {
    console.error('Remove tag failed', err)
    tagMessage.value = 'Failed to remove tag'
    tagError.value = true
  } finally {
    tagLoading.value = false
  }
}

/* SUMMARY */
async function generateSummary() {
  if (summaryLoading.value) return
  summaryLoading.value = true
  try {
    const dto = await $fetch(`${api}/publications/${id}/summary`, {
      headers: token.value ? {Authorization: `Bearer ${token.value}`} : {}
    })
    if (dto && typeof dto.summary === 'string') {
      if (!publication.value) publication.value = {}
      publication.value.summary = dto.summary
    }
  } catch (err) {
    console.error('Summary generation failed', err)
  } finally {
    summaryLoading.value = false
  }
}

/* HELPERS */
const formatDate = d => d ? new Date(d).toLocaleDateString() : ''
const formatDateTime = d => d ? new Date(d).toLocaleString() : ''
const goBack = () => router.push('/publications')
const goToProfile = () => router.push('/me')
const downloadFile = () => {
  const url = `${api}/publications/${id}/file`
  if (process.client) window.open(url, '_blank')
}
</script>

<style scoped>
button[aria-label^="Rate"] {
  cursor: pointer;
  background: transparent;
  border: none;
}
</style>
