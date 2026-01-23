<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const id = route.params.id

console.log('Publication ID:', id)
</script>

<template>
  <div>
    <h1>Publication {{ id }}</h1>
  </div>
</template>
