package com.example.redcurtainapp.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import java.net.SocketTimeoutException

data class TmdbMovie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double? = null,
    val backdropUrl: String? = null
)

data class TmdbMovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Double,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<String>,
    val runtime: Int?,
    val status: String
)

object TmdbApi {
    private const val BASE_URL = "https://api.themoviedb.org/3"
    private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w500"
    private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"
    
    suspend fun fetchNowPlayingMovies(): List<TmdbMovie> = withContext(Dispatchers.IO) {
        try {
            // Try TMDB API first
            val v4Token = com.example.redcurtainapp.BuildConfig.TMDB_V4_TOKEN.trim()
            val v3Key = com.example.redcurtainapp.BuildConfig.TMDB_API_KEY.trim()
            
            Log.d("TmdbApi", "TMDB_V4_TOKEN: ${if (v4Token.isBlank()) "EMPTY" else "SET (length: ${v4Token.length})"}")
            Log.d("TmdbApi", "TMDB_API_KEY: ${if (v3Key.isBlank()) "EMPTY" else "SET (length: ${v3Key.length})"}")
            
            // Check if we have API credentials
            if (v3Key.isBlank() && v4Token.isBlank()) {
                Log.w("TmdbApi", "No API key or token configured. Add TMDB_API_KEY or TMDB_V4_TOKEN to local.properties")
                Log.d("TmdbApi", "Using fallback movies (no API key)")
                return@withContext getCurrentCinemaMovies()
            }
            
            // Build URL with proper authentication
            val urlString = if (v4Token.isNotBlank()) {
                "$BASE_URL/movie/now_playing?language=en-US&page=1&region=US"
            } else {
                "$BASE_URL/movie/now_playing?language=en-US&page=1&region=US&api_key=$v3Key"
            }
            
            val url = URL(urlString)
            Log.d("TmdbApi", "Fetching URL: ${urlString.replace(v3Key, "***").replace(v4Token, "***")}")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            if (v4Token.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $v4Token")
            }
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            Log.d("TmdbApi", "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                
                if (!jsonObject.has("results")) {
                    Log.e("TmdbApi", "API response missing 'results' field")
                    return@withContext getCurrentCinemaMovies()
                }
                
                val results = jsonObject.getJSONArray("results")
                val movies = mutableListOf<TmdbMovie>()
                
                for (i in 0 until results.length()) {
                    val movieJson = results.getJSONObject(i)
                    val movie = TmdbMovie(
                        id = movieJson.getInt("id"),
                        title = movieJson.getString("title"),
                        posterUrl = if (movieJson.isNull("poster_path")) null
                                   else "$IMAGE_BASE${movieJson.getString("poster_path")}",
                        overview = if (movieJson.isNull("overview")) null else movieJson.getString("overview"),
                        releaseDate = if (movieJson.isNull("release_date")) null else movieJson.getString("release_date"),
                        voteAverage = if (movieJson.isNull("vote_average")) null else movieJson.getDouble("vote_average"),
                        backdropUrl = if (movieJson.isNull("backdrop_path")) null 
                                    else "$BACKDROP_BASE${movieJson.getString("backdrop_path")}"
                    )
                    movies.add(movie)
                }
                
                Log.d("TmdbApi", "Successfully fetched ${movies.size} movies from API")
                if (movies.isNotEmpty()) {
                    return@withContext movies
                } else {
                    Log.w("TmdbApi", "API returned empty results, using fallback")
                    return@withContext getCurrentCinemaMovies()
                }
            } else {
                val error = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message"
                } catch (e: Exception) {
                    "Error reading error stream: ${e.message}"
                }
                Log.e("TmdbApi", "HTTP $responseCode while fetching now_playing. Error: $error")
                Log.d("TmdbApi", "Using fallback movies due to API error")
                return@withContext getCurrentCinemaMovies()
            }
            
        } catch (e: SocketTimeoutException) {
            Log.e("TmdbApi", "Timeout fetching now_playing (15s)", e)
            return@withContext getCurrentCinemaMovies()
        } catch (e: java.net.UnknownHostException) {
            Log.e("TmdbApi", "Network error - no internet connection", e)
            return@withContext getCurrentCinemaMovies()
        } catch (e: Exception) {
            Log.e("TmdbApi", "Exception fetching now_playing", e)
            e.printStackTrace()
            return@withContext getCurrentCinemaMovies()
        }
    }
    
    private fun getCurrentCinemaMovies(): List<TmdbMovie> {
        return listOf(
            TmdbMovie(
                id = 872585,
                title = "Oppenheimer",
                posterUrl = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                overview = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                releaseDate = "2023-07-21",
                voteAverage = 8.1,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsgDH1HpjyqK6r3dGfH.jpg"
            ),
            TmdbMovie(
                id = 346698,
                title = "Barbie",
                posterUrl = "https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg",
                overview = "Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land.",
                releaseDate = "2023-07-21",
                voteAverage = 6.9,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/nHf61UzkfFno5X1ofIhugCPus2R.jpg"
            ),
            TmdbMovie(
                id = 335787,
                title = "Uncharted",
                posterUrl = "https://image.tmdb.org/t/p/w500/rJHC1RUORuUhtfNb4Npclx0xnOf.jpg",
                overview = "A young street-smart, Nathan Drake and his wisecracking partner Victor 'Sully' Sullivan embark on a dangerous pursuit of 'the greatest treasure never found'.",
                releaseDate = "2022-02-10",
                voteAverage = 6.3,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            ),
            TmdbMovie(
                id = 634649,
                title = "Spider-Man: No Way Home",
                posterUrl = "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                overview = "Peter Parker is unmasked and no longer able to separate his normal life from the high-stakes of being a super-hero.",
                releaseDate = "2021-12-15",
                voteAverage = 8.2,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg"
            ),
            TmdbMovie(
                id = 508947,
                title = "Turning Red",
                posterUrl = "https://image.tmdb.org/t/p/w500/qsdjk9oAKSQMWs0Vt5Pyfh6O4GZ.jpg",
                overview = "Thirteen-year-old Mei is experiencing the awkwardness of being a teenager with a twist – when she gets too excited, she transforms into a giant red panda.",
                releaseDate = "2022-03-10",
                voteAverage = 7.0,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/4nKoB6wMVXfsYgRZK5lAhG4jYkS.jpg"
            ),
            TmdbMovie(
                id = 524434,
                title = "Eternals",
                posterUrl = "https://image.tmdb.org/t/p/w500/6AdXwFTRTAzggD2QUTt5B7JFGKL.jpg",
                overview = "The Eternals are a team of ancient aliens who have been living on Earth in secret for thousands of years.",
                releaseDate = "2021-11-03",
                voteAverage = 6.3,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/1uegR4uAxGxiqO1w2l3Y9f3nF3F.jpg"
            ),
            TmdbMovie(
                id = 566525,
                title = "Shang-Chi and the Legend of the Ten Rings",
                posterUrl = "https://image.tmdb.org/t/p/w500/1BIoJGKbX0FDA6Gkmf7y7pr3Qr3.jpg",
                overview = "Shang-Chi, the master of weaponry-based Kung Fu, is forced to confront his past after being drawn into the Ten Rings organization.",
                releaseDate = "2021-09-01",
                voteAverage = 7.4,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/9GvhICFMiRQA82vS6ydkXxeEkud.jpg"
            ),
            TmdbMovie(
                id = 580489,
                title = "Venom: Let There Be Carnage",
                posterUrl = "https://image.tmdb.org/t/p/w500/1M876Kj8Vg5Qz8KdCvQOjR8Yt8A.jpg",
                overview = "Eddie Brock is still struggling to coexist with the shape-shifting extraterrestrial Venom.",
                releaseDate = "2021-09-30",
                voteAverage = 5.9,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            ),
            TmdbMovie(
                id = 550988,
                title = "Free Guy",
                posterUrl = "https://image.tmdb.org/t/p/w500/xmbU4JTUm8rsdtn7Y3Fcm30GpeT.jpg",
                overview = "A bank teller discovers he is actually a background player in an open-world video game.",
                releaseDate = "2021-08-11",
                voteAverage = 7.2,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            ),
            TmdbMovie(
                id = 508442,
                title = "Soul",
                posterUrl = "https://image.tmdb.org/t/p/w500/hm58Jw4Lw8OIeECIq5qyPYhAeRg.jpg",
                overview = "Joe Gardner is a middle-school band teacher whose life hasn't quite gone the way he expected.",
                releaseDate = "2020-12-25",
                voteAverage = 8.1,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            ),
            TmdbMovie(
                id = 508947,
                title = "Encanto",
                posterUrl = "https://image.tmdb.org/t/p/w500/4j0PNHkMr5ax3o8Ek3X0jz7Feqo.jpg",
                overview = "The tale of an extraordinary family, the Madrigals, who live hidden in the mountains of Colombia.",
                releaseDate = "2021-11-24",
                voteAverage = 7.3,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            ),
            TmdbMovie(
                id = 566525,
                title = "Black Widow",
                posterUrl = "https://image.tmdb.org/t/p/w500/qAZ0pzat6k9TPEfVQY3uXFAUNSS.jpg",
                overview = "Natasha Romanoff confronts the darker parts of her ledger when a dangerous conspiracy with ties to her past arises.",
                releaseDate = "2021-07-07",
                voteAverage = 7.2,
                backdropUrl = "https://image.tmdb.org/t/p/w1280/7WJjFviFBffEJvtA52mX8WJjz8G.jpg"
            )
        )
    }

    suspend fun searchMovies(query: String): List<TmdbMovie> = withContext(Dispatchers.IO) {
        try {
            val v4Token = com.example.redcurtainapp.BuildConfig.TMDB_V4_TOKEN
            val v3Key = com.example.redcurtainapp.BuildConfig.TMDB_API_KEY
            
            if ((v3Key.isNotBlank() || v4Token.isNotBlank()) && query.isNotBlank()) {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = URL("$BASE_URL/search/movie?query=$encodedQuery&language=en-US&page=1" + if (v4Token.isBlank()) "&api_key=$v3Key" else "")
                Log.d("TmdbApi", "Searching URL: $url")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                if (v4Token.isNotBlank()) {
                    connection.setRequestProperty("Authorization", "Bearer $v4Token")
                }
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val results = jsonObject.getJSONArray("results")
                    
                    val movies = mutableListOf<TmdbMovie>()
                    for (i in 0 until results.length()) {
                        val movieJson = results.getJSONObject(i)
                        val movie = TmdbMovie(
                            id = movieJson.getInt("id"),
                            title = movieJson.getString("title"),
                            posterUrl = if (movieJson.isNull("poster_path")) null
                                       else "$IMAGE_BASE${movieJson.getString("poster_path")}",
                            overview = if (movieJson.isNull("overview")) null else movieJson.getString("overview"),
                            releaseDate = if (movieJson.isNull("release_date")) null else movieJson.getString("release_date"),
                            voteAverage = if (movieJson.isNull("vote_average")) null else movieJson.getDouble("vote_average"),
                            backdropUrl = if (movieJson.isNull("backdrop_path")) null 
                                        else "$BACKDROP_BASE${movieJson.getString("backdrop_path")}"
                        )
                        movies.add(movie)
                    }
                    Log.d("TmdbApi", "Search results: ${movies.size} items for query: $query")
                    return@withContext movies
                } else {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("TmdbApi", "HTTP $responseCode while searching movies. Error: $error")
                }
            }
            
            // Return empty list if search fails or query is blank
            Log.d("TmdbApi", "Search failed or query blank, returning empty list")
            emptyList()
            
        } catch (e: SocketTimeoutException) {
            Log.e("TmdbApi", "Timeout searching movies", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("TmdbApi", "Exception searching movies", e)
            emptyList()
        }
    }

    suspend fun fetchMovieDetails(movieId: Int): TmdbMovieDetail? = withContext(Dispatchers.IO) {
        try {
            val v4Token = com.example.redcurtainapp.BuildConfig.TMDB_V4_TOKEN.trim()
            val v3Key = com.example.redcurtainapp.BuildConfig.TMDB_API_KEY.trim()
            
            if (v3Key.isBlank() && v4Token.isBlank()) {
                Log.w("TmdbApi", "No API key or token configured for movie details. Add TMDB_API_KEY or TMDB_V4_TOKEN to local.properties")
                return@withContext null
            }
            
            val url = URL("$BASE_URL/movie/$movieId?language=en-US" + if (v4Token.isBlank()) "&api_key=$v3Key" else "")
            Log.d("TmdbApi", "Fetching movie details for ID: $movieId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            if (v4Token.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $v4Token")
            }
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            val responseCode = connection.responseCode
            Log.d("TmdbApi", "Movie details response code: $responseCode for movie ID: $movieId")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val movieJson = JSONObject(response)
                
                val genres = mutableListOf<String>()
                if (!movieJson.isNull("genres")) {
                    val genresArray = movieJson.getJSONArray("genres")
                    for (i in 0 until genresArray.length()) {
                        val genre = genresArray.getJSONObject(i)
                        genres.add(genre.getString("name"))
                    }
                }
                
                return@withContext TmdbMovieDetail(
                    id = movieJson.getInt("id"),
                    title = movieJson.getString("title"),
                    overview = movieJson.getString("overview"),
                    releaseDate = movieJson.getString("release_date"),
                    voteAverage = movieJson.getDouble("vote_average"),
                    posterUrl = if (movieJson.isNull("poster_path")) null
                               else "$IMAGE_BASE${movieJson.getString("poster_path")}",
                    backdropUrl = if (movieJson.isNull("backdrop_path")) null 
                                else "$BACKDROP_BASE${movieJson.getString("backdrop_path")}",
                    genres = genres,
                    runtime = if (movieJson.isNull("runtime")) null else movieJson.getInt("runtime"),
                    status = movieJson.getString("status")
                )
            } else {
                val error = try { 
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message" 
                } catch (e: Exception) { 
                    "Error reading error stream: ${e.message}" 
                }
                Log.e("TmdbApi", "HTTP $responseCode while fetching movie details for ID $movieId. Error: $error")
            }
            
            null
            
        } catch (e: SocketTimeoutException) {
            Log.e("TmdbApi", "Timeout fetching movie details for ID $movieId (15s)", e)
            null
        } catch (e: java.net.UnknownHostException) {
            Log.e("TmdbApi", "Network error - no internet connection for movie details", e)
            null
        } catch (e: Exception) {
            Log.e("TmdbApi", "Exception fetching movie details for ID $movieId", e)
            e.printStackTrace()
            null
        }
    }
    
    private fun getMockMovieDetails(movieId: Int): TmdbMovieDetail? {
        return when (movieId) {
            872585 -> TmdbMovieDetail(
                id = 872585,
                title = "Oppenheimer",
                overview = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb. This biographical thriller follows Oppenheimer's journey from brilliant physicist to the man who changed the world forever.",
                releaseDate = "2023-07-21",
                voteAverage = 8.1,
                posterUrl = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsgDH1HpjyqK6r3dGfH.jpg",
                genres = listOf("Drama", "History", "Thriller"),
                runtime = 180,
                status = "Released"
            )
            346698 -> TmdbMovieDetail(
                id = 346698,
                title = "Barbie",
                overview = "Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land. However, when they get a chance to go to the real world, they soon discover the joys and perils of living among humans.",
                releaseDate = "2023-07-21",
                voteAverage = 6.9,
                posterUrl = "https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/nHf61UzkfFno5X1ofIhugCPus2R.jpg",
                genres = listOf("Comedy", "Adventure", "Fantasy"),
                runtime = 114,
                status = "Released"
            )
            634649 -> TmdbMovieDetail(
                id = 634649,
                title = "Spider-Man: No Way Home",
                overview = "Peter Parker is unmasked and no longer able to separate his normal life from the high-stakes of being a super-hero. When he asks Doctor Strange for help, the spell goes wrong and opens up the multiverse.",
                releaseDate = "2021-12-15",
                voteAverage = 8.2,
                posterUrl = "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg",
                genres = listOf("Action", "Adventure", "Fantasy"),
                runtime = 148,
                status = "Released"
            )
            else -> TmdbMovieDetail(
                id = movieId,
                title = "Current Cinema Movie",
                overview = "This is a current movie playing in cinemas. Experience the latest blockbuster with stunning visuals, compelling storylines, and top-notch performances from your favorite actors.",
                releaseDate = "2024-01-01",
                voteAverage = 7.5,
                posterUrl = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsgDH1HpjyqK6r3dGfH.jpg",
                genres = listOf("Action", "Drama", "Thriller"),
                runtime = 120,
                status = "Released"
            )
        }
    }
}
