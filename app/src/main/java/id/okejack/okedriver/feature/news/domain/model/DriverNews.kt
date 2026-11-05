package id.okejack.okedriver.feature.news.domain.model

data class DriverNews(
    val id: Int,
    val title: String,
    val content: String,
    val summary: String,
    val imageUrl: String,
    val actionUrl: String,
    val createdAt: String,
    val isPinned: Boolean
)

data class NewsPage(
    val items: List<DriverNews>,
    val currentPage: Int,
    val lastPage: Int
) {
    val hasMore: Boolean
        get() = if (lastPage > 0) currentPage < lastPage else items.isNotEmpty()
}
