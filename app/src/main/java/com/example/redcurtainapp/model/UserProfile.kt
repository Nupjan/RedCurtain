package com.example.redcurtainapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * UserProfile entity - stores user details that can be synced with server
 * Server-ready with sync flags and server ID
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    @ColumnInfo(name = "email")
    val email: String, // Email is the primary key (unique identifier)
    
    @ColumnInfo(name = "first_name")
    val firstName: String? = null,
    
    @ColumnInfo(name = "last_name")
    val lastName: String? = null,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,
    
    @ColumnInfo(name = "address")
    val address: String? = null,
    
    @ColumnInfo(name = "city")
    val city: String? = null,
    
    @ColumnInfo(name = "state")
    val state: String? = null,
    
    @ColumnInfo(name = "zip_code")
    val zipCode: String? = null,
    
    @ColumnInfo(name = "country")
    val country: String? = null,
    
    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: String? = null, // Stored as String (YYYY-MM-DD format)
    
    // Loyalty
    @ColumnInfo(name = "loyalty_points")
    val loyaltyPoints: Int = 0,
    
    // Server-related fields
    @ColumnInfo(name = "server_id")
    val serverId: String? = null, // ID from server (null if not synced)
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true, // True if needs to be synced with server
    
    @ColumnInfo(name = "last_synced")
    val lastSynced: Long? = null, // Timestamp of last successful sync
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

