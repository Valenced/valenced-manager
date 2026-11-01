package com.aliucord.manager.network.services

import com.aliucord.manager.network.models.RNATrackerIndex
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RNATrackerService(
    private val httpService: HttpService,
) {
    suspend fun getLatestDiscordVersions() = withContext(Dispatchers.IO) {
        httpService.request<RNATrackerIndex> {
            url("${BASE_URL}/tracker/index")
        }
    }

    companion object {
        const val BASE_URL = "https://tracker.vendetta.rocks"
    }
}
