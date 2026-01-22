export default defineNuxtRouteMiddleware((to, from) => {
    const isLoggedIn = useCookie('loggedIn')?.value || false

    // If user is not logged in and is not already on /login, redirect
    if (!isLoggedIn && to.path !== '/login') {
        return navigateTo('/login')
    }

    // Optional: prevent logged-in users from going back to login
    if (isLoggedIn && to.path === '/login') {
        return navigateTo('/')
    }
})
