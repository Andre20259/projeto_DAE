export const useAuth = () => {
    const config = useRuntimeConfig()

    const login = async (username: string, password: string) => {
        const response = await $fetch('/auth/login', {
            baseURL: config.public.apiBase,
            method: 'POST',
            body: { username, password }
        })

        const token = typeof response === 'string'
            ? response
            : response.token

        sessionStorage.setItem('auth_token', token)
        return token
    }

    const logout = () => {
        sessionStorage.removeItem('auth_token')
    }

    const token = () => sessionStorage.getItem('auth_token')

    return { login, logout, token }
}
