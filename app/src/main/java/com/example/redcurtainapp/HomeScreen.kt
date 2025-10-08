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

// --- Data Model ---
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String
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
@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                ).build()
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
                Movie("1", "Avengers: Endgame", "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/ulzhLuWrPK07P1YkdWQLZnQh1JL.jpg"),
                Movie("2", "Inception", "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/ljsZTbVsrQSqZgWeep2B1QiDKuh.jpg"),
                Movie("3", "Spider-Man: No Way Home", "https://picsum.photos/200/300?random=3"),
                Movie("4", "The Dark Knight", "https://picsum.photos/200/300?random=4"),
                Movie("5", "Interstellar", "https://picsum.photos/200/300?random=5"),
                Movie("6", "Pulp Fiction", "https://picsum.photos/200/300?random=6"),
                Movie("7", "The Matrix", "https://picsum.photos/200/300?random=7"),
                Movie("8", "Forrest Gump", "https://picsum.photos/200/300?random=8"),
                Movie("9", "Fight Club", "https://picsum.photos/200/300?random=9"),
                Movie("10", "The Shawshank Redemption", "https://picsum.photos/200/300?random=10"),
                Movie("11", "Goodfellas", "https://picsum.photos/200/300?random=11"),
                Movie("12", "The Godfather", "https://picsum.photos/200/300?random=12")
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

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // For testing: add a random movie (remove in production)

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
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun RemoteImage(url: String, modifier: Modifier = Modifier) {
    var bmp by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val stream = java.net.URL(url).openStream()
                val decoded = BitmapFactory.decodeStream(stream)
                bmp = decoded?.asImageBitmap()
            } catch (_: Exception) { bmp = null }
        }
    }
    if (bmp != null) {
        Image(bitmap = bmp!!, contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
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

    // Refresh local DB from TMDB now playing
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val now = TmdbApi.fetchNowPlayingMovies()
                if (now.isNotEmpty()) {
                    val mapped = now.map { tmdbMovie ->
                        Movie(id = tmdbMovie.id.toString(), title = tmdbMovie.title, posterUrl = tmdbMovie.posterUrl ?: "")
                    }
                    val movieDao = database.movieDao()
                    movieDao.deleteAll()
                    movieDao.insertMovies(mapped)
                }
            } catch (_: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top nav bar with placeholder username and settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painter = painterResource(id = R.drawable.logo_transparent), contentDescription = null, modifier = Modifier.size(24.dp))
                Text(text = "Hi, Alex", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(Screen.Search.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "🔍",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(Screen.Settings.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "⚙",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

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
                    val encoded = URLEncoder.encode(movie.title, "UTF-8")
                    navController.navigate("movieDetail/$encoded")
                }
            }
        }
    }
}