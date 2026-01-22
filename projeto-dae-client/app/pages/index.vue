<template>
  <div class="min-h-screen bg-gray-900">
    <nav class="bg-gray-800 px-6 py-3 shadow-md flex items-center">
      <NuxtLink to="/" class="text-white text-lg font-bold hover:text-gray-300">
        PGPC
      </NuxtLink>

      <NuxtLink to="/login" class="ml-auto text-sm text-gray-300 hover:text-white">
        Login
      </NuxtLink>
    </nav>

    <div class="grid place-items-center h-[calc(100vh-52px)]">
      <div class="bg-gray-800 p-8 rounded-xl shadow-lg w-full max-w-lg text-center mx-4">
        <h1 class="text-xl font-bold text-white mb-6">
          Welcome to Plataforma de Gestão de Publicações Científicas
        </h1>

        <button
            @click="goToLogin"
            class="bg-gray-600 text-white px-6 py-2 rounded-md hover:bg-gray-700 transition-colors text-sm"
        >
          Login to Grant Access
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const router = useRouter()
const goToLogin = () => {
  router.push('/login')
}
</script>