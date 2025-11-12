package com.example.redcurtainapp

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.redcurtainapp.navigation.Screen
import com.example.redcurtainapp.api.TmdbApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import androidx.navigation.NavHostController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.net.URLEncoder
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.redcurtainapp.model.Booking
import com.example.redcurtainapp.model.SeatBooking
import com.example.redcurtainapp.model.Transaction
import com.example.redcurtainapp.model.BookingDao
import com.example.redcurtainapp.model.SeatBookingDao
import com.example.redcurtainapp.model.TransactionDao
import com.example.redcurtainapp.model.UserProfile
import com.example.redcurtainapp.model.UserProfileDao
import org.json.JSONArray
import org.json.JSONObject

// --- Data Model ---
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String,
    @ColumnInfo(name = "overview") val overview: String? = null,
    @ColumnInfo(name = "release_date") val releaseDate: String? = null,
    @ColumnInfo(name = "vote_average") val voteAverage: Double? = null,
    @ColumnInfo(name = "backdrop_url") val backdropUrl: String? = null,
    @ColumnInfo(name = "tmdb_id") val tmdbId: Int? = null
)

// --- Room DAO ---
@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY RANDOM()") // Random order each fetch
    fun getAllMoviesRandom(): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Query("DELETE FROM movies")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getCount(): Int
}

// --- Room Database ---
@Database(
    entities = [
        Movie::class,
        Booking::class,
        SeatBooking::class,
        Transaction::class,
        UserProfile::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun bookingDao(): BookingDao
    abstract fun seatBookingDao(): SeatBookingDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).fallbackToDestructiveMigration() // For development - handles schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository ---
class MovieRepository(private val movieDao: MovieDao) {
    val allMovies: Flow<List<Movie>> = movieDao.getAllMoviesRandom()

    suspend fun seedDatabaseIfEmpty() {
        if (movieDao.getCount() == 0) {
            val sampleMovies = listOf(
                Movie("1", "Avengers: Endgame", "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/ulzhLuWrPK07P1YkdWQLZnQh1JL.jpg", 
                    "After the devastating events of Avengers: Infinity War, the universe is in ruins.", "2019-04-24", 8.4, null, 299534),
                Movie("2", "Inception", "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/ljsZTbVsrQSqZgWeep2B1QiDKuh.jpg",
                    "A thief who steals corporate secrets through dream-sharing technology.", "2010-07-15", 8.4, null, 27205),
                Movie("3", "Spider-Man: No Way Home", "https://picsum.photos/200/300?random=3",
                    "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help.", "2021-12-15", 8.2, null, 634649),
                Movie("4", "The Dark Knight", "https://picsum.photos/200/300?random=4",
                    "Batman raises the stakes in his war on crime.", "2008-07-16", 9.0, null, 155),
                Movie("5", "Interstellar", "https://picsum.photos/200/300?random=5",
                    "A team of explorers travel through a wormhole in space.", "2014-11-05", 8.6, null, 157336),
                Movie("6", "Pulp Fiction", "https://picsum.photos/200/300?random=6",
                    "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine.", "1994-09-10", 8.9, null, 680),
                Movie("7", "The Matrix", "https://picsum.photos/200/300?random=7",
                    "A computer hacker learns about the true nature of reality.", "1999-03-30", 8.7, null, 603),
                Movie("8", "Forrest Gump", "https://picsum.photos/200/300?random=8",
                    "The presidencies of Kennedy and Johnson, the Vietnam War.", "1994-06-23", 8.8, null, 13),
                Movie("9", "Fight Club", "https://picsum.photos/200/300?random=9",
                    "An insomniac office worker and a devil-may-care soap maker form an underground fight club.", "1999-10-15", 8.8, null, 550),
                Movie("10", "The Shawshank Redemption", "https://picsum.photos/200/300?random=10",
                    "Two imprisoned men bond over a number of years.", "1994-09-23", 9.3, null, 278),
                Movie("11", "Goodfellas", "https://picsum.photos/200/300?random=11",
                    "The story of Henry Hill and his life in the mob.", "1990-09-12", 8.7, null, 769),
                Movie("12", "The Godfather", "https://picsum.photos/200/300?random=12",
                    "The aging patriarch of an organized crime dynasty transfers control.", "1972-03-14", 9.2, null, 238)
            )
            movieDao.insertMovies(sampleMovies)
        }
    }

    suspend fun addMovie(movie: Movie) {
        movieDao.insertMovies(listOf(movie))
    }
}

// --- ViewModel ---
class MovieViewModel(private val repository: MovieRepository) : ViewModel() {
    val movies: StateFlow<List<Movie>> = repository.allMovies
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

// --- ViewModel Factory ---
class MovieViewModelFactory(private val repository: MovieRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Movie Card ---
@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column {
            RemoteImage(url = movie.posterUrl, modifier = Modifier
                .fillMaxWidth()
                .weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (movie.voteAverage != null) {
                        Text(
                            text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (movie.releaseDate != null) {
                        Text(
                            text = movie.releaseDate.take(4), // Show only year
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteImage(url: String, modifier: Modifier = Modifier) {
    var bmp by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(url) {
        isLoading = true
        hasError = false
        withContext(Dispatchers.IO) {
            try {
                val stream = java.net.URL(url).openStream()
                val decoded = BitmapFactory.decodeStream(stream)
                bmp = decoded?.asImageBitmap()
                hasError = decoded == null
            } catch (e: Exception) { 
                bmp = null
                hasError = true
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(modifier = modifier) {
        if (bmp != null) {
            Image(bitmap = bmp!!, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎬",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Home Screen ---
@Composable
fun MovieGridScreen(navController: NavHostController) {
    val context = LocalContext.current
    val database = remember(context) { MovieDatabase.getDatabase(context) }
    val repository = remember(database) { MovieRepository(database.movieDao()) }
    val viewModel: MovieViewModel = viewModel(
        factory = MovieViewModelFactory(repository)
    )

    val movies by viewModel.movies.collectAsState()
    var isLoadingMovies by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Refresh trigger state
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Refresh local DB from TMDB now playing (ONLY current cinema movies)
    LaunchedEffect(refreshTrigger) {
        isLoadingMovies = true
        hasError = false
        withContext(Dispatchers.IO) {
            val movieDao = database.movieDao()
            try {
                val now = TmdbApi.fetchNowPlayingMovies()
                if (now.isNotEmpty()) {
                    movieDao.deleteAll()
                    val mapped = now.map { tmdbMovie ->
                        Movie(
                            id = tmdbMovie.id.toString(), 
                            title = tmdbMovie.title, 
                            posterUrl = tmdbMovie.posterUrl ?: "",
                            overview = tmdbMovie.overview,
                            releaseDate = tmdbMovie.releaseDate,
                            voteAverage = tmdbMovie.voteAverage,
                            backdropUrl = tmdbMovie.backdropUrl,
                            tmdbId = tmdbMovie.id
                        )
                    }
                    movieDao.insertMovies(mapped)
                    // Persist a lightweight cache of the last successful API response
                    saveMoviesCache(context, mapped)
                } else {
                    // API returned empty; fallback to cached movies if DB is empty
                    if (movieDao.getCount() == 0) {
                        val cached = loadMoviesCache(context)
                        if (cached.isNotEmpty()) {
                            movieDao.insertMovies(cached)
                        } else {
                            val sampleMovies = listOf(
                                Movie("s1", "The Red Curtain", "https://picsum.photos/200/300?random=21"),
                                Movie("s2", "Midnight Premiere", "https://picsum.photos/200/300?random=22"),
                                Movie("s3", "Cinema Classics", "https://picsum.photos/200/300?random=23")
                            )
                            movieDao.insertMovies(sampleMovies)
                        }
                    }
                }
            } catch (e: Exception) {
                hasError = true
                // Keep existing if any; otherwise seed fallback
                if (movieDao.getCount() == 0) {
                    val cached = loadMoviesCache(context)
                    if (cached.isNotEmpty()) {
                        movieDao.insertMovies(cached)
                    } else {
                        val sampleMovies = listOf(
                            Movie("s1", "The Red Curtain", "https://picsum.photos/200/300?random=21"),
                            Movie("s2", "Midnight Premiere", "https://picsum.photos/200/300?random=22"),
                            Movie("s3", "Cinema Classics", "https://picsum.photos/200/300?random=23")
                        )
                        movieDao.insertMovies(sampleMovies)
                    }
                }
            } finally {
                isLoadingMovies = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A0000))
    ) {
        // Top bar matching provided design: logo, greeting, rounded icon buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_transparent),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                val context = LocalContext.current
                val email = remember { AuthManager.getUserEmail(context) }
                val database = remember { MovieDatabase.getDatabase(context) }
                var displayName by remember { mutableStateOf<String>("") }
                LaunchedEffect(email) {
                    if (email != null) {
                        val profile = withContext(Dispatchers.IO) { database.userProfileDao().getUserProfileByEmail(email) }
                        displayName = when {
                            profile?.firstName?.isNotBlank() == true -> profile.firstName!!
                            else -> email.substringBefore("@")
                        }
                    }
                }
                Text(text = "Hi, ${if (displayName.isNotBlank()) displayName else "there"}", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { navController.navigate(Screen.Search.route) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE9B9C7)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "🔍",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2D0000)
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { navController.navigate(Screen.Profile.route) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE9B9C7)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "⚙",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2D0000)
                        )
                    }
                }
            }
        }

        when {
            isLoadingMovies -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading movies...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            hasError && movies.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Failed to load movies",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Check your internet connection or TMDB API key\n\nTo use the API:\n1. Get a free API key from https://www.themoviedb.org/settings/api\n2. Add it to local.properties:\n   TMDB_API_KEY=your_key_here\n   OR\n   TMDB_V4_TOKEN=your_token_here\n3. Rebuild the app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                // Force refresh by incrementing trigger
                                refreshTrigger++
                            }
                        ) {
                            Text("🔄 Retry")
                        }
                    }
                }
            }
            movies.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🎬",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "No current cinema movies available",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "We only show movies currently playing in cinemas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies, key = { it.id }) { movie ->
                        MovieCard(movie) {
                            // Pass the TMDB ID if available, otherwise use the local ID
                            val movieId = movie.tmdbId ?: movie.id
                            navController.navigate("movieDetail/$movieId")
                        }
                    }
                }
            }
        }
    }
}

// --- Simple persistent cache for last successful movies API ---
private const val MOVIES_CACHE_PREF = "movies_cache_pref"
private const val MOVIES_CACHE_KEY = "movies_cache_key"

private fun saveMoviesCache(context: android.content.Context, movies: List<Movie>) {
    try {
        val arr = JSONArray()
        movies.forEach { m ->
            val obj = JSONObject()
                .put("id", m.id)
                .put("title", m.title)
                .put("posterUrl", m.posterUrl)
                .put("overview", m.overview)
                .put("releaseDate", m.releaseDate)
                .put("voteAverage", m.voteAverage)
                .put("backdropUrl", m.backdropUrl)
                .put("tmdbId", m.tmdbId)
            arr.put(obj)
        }
        val prefs = context.getSharedPreferences(MOVIES_CACHE_PREF, android.content.Context.MODE_PRIVATE)
        prefs.edit().putString(MOVIES_CACHE_KEY, arr.toString()).apply()
    } catch (_: Exception) { }
}

private fun loadMoviesCache(context: android.content.Context): List<Movie> {
    return try {
        val prefs = context.getSharedPreferences(MOVIES_CACHE_PREF, android.content.Context.MODE_PRIVATE)
        val json = prefs.getString(MOVIES_CACHE_KEY, null) ?: return emptyList()
        val arr = JSONArray(json)
        val list = mutableListOf<Movie>();
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Movie(
                    id = obj.optString("id"),
                    title = obj.optString("title"),
                    posterUrl = obj.optString("posterUrl"),
                    overview = obj.optString("overview").takeIf { it.isNotBlank() },
                    releaseDate = obj.optString("releaseDate").takeIf { it.isNotBlank() },
                    voteAverage = if (obj.has("voteAverage") && !obj.isNull("voteAverage")) obj.getDouble("voteAverage") else null,
                    backdropUrl = obj.optString("backdropUrl").takeIf { it.isNotBlank() },
                    tmdbId = if (obj.has("tmdbId") && !obj.isNull("tmdbId")) obj.getInt("tmdbId") else null
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}
