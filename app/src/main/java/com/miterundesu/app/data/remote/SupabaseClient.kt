package com.miterundesu.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://gtxoniuzwhmdwnhegwnz.supabase.co"
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0eG9uaXV6d2htZHduaGVnd256Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI5NTY3OTcsImV4cCI6MjA3ODUzMjc5N30.MZPcs9O0xaWcQPRDhn7pIrv9JbFZRU6AI_sQH0dERC8"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
        }
    }
}
