package com.example.redcurtainapp

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.net.URL

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
                Movie("1", "Avengers: Endgame", "https://picsum.photos/200/300?random=1"),
                Movie("2", "Inception", "https://picsum.photos/200/300?random=2"),
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

// --- Network Image Loader ---
@Composable
fun NetworkImage(url: String, modifier: Modifier = Modifier) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val stream = URL(url).openStream()
                val bmp = BitmapFactory.decodeStream(stream)
                bitmap = bmp?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    bitmap?.let {
        Image(bitmap = it, contentDescription = null, modifier = modifier)
    }
}

// --- Movie Card ---
@Composable
fun MovieCard(movie: Movie) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            NetworkImage(movie.posterUrl, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

// --- Home Screen ---
@Composable
fun MovieGridScreen() {
    val context = LocalContext.current
    val database = remember { MovieDatabase.getDatabase(context) }
    val repository = remember { MovieRepository(database.movieDao()) }
    val viewModel: MovieViewModel = viewModel(
        factory = MovieViewModelFactory(repository)
    )

    val movies by viewModel.movies.collectAsState()

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
            MovieCard(movie)
        }
    }
}