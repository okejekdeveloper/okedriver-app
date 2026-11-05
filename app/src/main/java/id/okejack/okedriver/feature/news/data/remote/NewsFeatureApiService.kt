package id.okejack.okedriver.feature.news.data.remote

import id.okejack.okedriver.feature.news.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsFeatureApiService {
    @GET("news")
    suspend fun getNews(
        @Query("api_token") apiToken: String,
        @Query("page") page: Int
    ): NewsResponseDto

    @GET("news/{id}")
    suspend fun getNewsDetail(
        @Path("id") id: Int,
        @Query("api_token") apiToken: String
    ): NewsResponseDto
}
