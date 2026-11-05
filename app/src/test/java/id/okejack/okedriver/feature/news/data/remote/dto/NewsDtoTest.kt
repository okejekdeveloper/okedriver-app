package id.okejack.okedriver.feature.news.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsDtoTest {
    private val gson = Gson()

    @Test
    fun `maps paginated news response to domain`() {
        val response = gson.fromJson(
            """
            {
              "success": true,
              "data": {
                "news": [
                  {
                    "id": 11,
                    "title": "Informasi Driver",
                    "content": "<p>Isi lengkap berita</p>",
                    "summary": "Ringkasan berita",
                    "image": "https://example.com/news.jpg",
                    "url": "https://example.com/read",
                    "created_at": "2026-07-28 10:00:00",
                    "pinned": true
                  }
                ],
                "pagination": {
                  "current_page": 1,
                  "last_page": 3
                }
              }
            }
            """.trimIndent(),
            NewsResponseDto::class.java
        )

        val page = response.data.toPage(requestedPage = 1)
        val news = page.items.single()

        assertTrue(response.isSuccessful)
        assertEquals(1, page.currentPage)
        assertEquals(3, page.lastPage)
        assertTrue(page.hasMore)
        assertEquals(11, news.id)
        assertEquals("Informasi Driver", news.title)
        assertEquals("https://example.com/news.jpg", news.imageUrl)
        assertTrue(news.isPinned)
    }

    @Test
    fun `maps news detail and detects final page`() {
        val response = gson.fromJson(
            """
            {
              "status": true,
              "data": {
                "single_news": {
                  "id": 12,
                  "title": "Detail Berita",
                  "content": "<p>Konten detail</p>",
                  "summary": "Ringkasan detail",
                  "image": "",
                  "url": "https://example.com/detail",
                  "created_at": "2026-07-28",
                  "pinned": false
                },
                "pagination": {
                  "current_page": 2,
                  "last_page": 2
                }
              }
            }
            """.trimIndent(),
            NewsResponseDto::class.java
        )

        val news = requireNotNull(response.data.singleNews).toDomain()
        val finalPage = response.data.toPage(requestedPage = 2)

        assertTrue(response.isSuccessful)
        assertEquals("<p>Konten detail</p>", news.content)
        assertEquals("https://example.com/detail", news.actionUrl)
        assertFalse(news.isPinned)
        assertFalse(finalPage.hasMore)
    }
}
