package com.example.redcurtainapp.model

enum class SeatType {
    AVAILABLE,
    SELECTED,
    OCCUPIED,
    PREMIUM,
    DISABLED
}

data class Seat(
    val id: String,
    val row: String,
    val number: Int,
    val type: SeatType = SeatType.AVAILABLE,
    val price: Double = 0.0
) {
    val isSelectable: Boolean
        get() = type == SeatType.AVAILABLE || type == SeatType.PREMIUM
    
    val isSelected: Boolean
        get() = type == SeatType.SELECTED
}

data class CinemaHall(
    val id: String,
    val name: String,
    val rows: List<String>,
    val seatsPerRow: Int,
    val premiumRows: List<String> = emptyList(),
    val basePrice: Double = 12.0,
    val premiumPrice: Double = 18.0
)

data class BookingSession(
    val movieId: Int,
    val movieTitle: String,
    val showTime: String,
    val cinemaHall: CinemaHall,
    val selectedSeats: List<Seat> = emptyList(),
    val totalPrice: Double = 0.0
)
