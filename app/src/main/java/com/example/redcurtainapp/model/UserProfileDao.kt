package com.example.redcurtainapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getUserProfileByEmail(email: String): UserProfile?
    
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    fun getUserProfileByEmailFlow(email: String): Flow<UserProfile?>
    
    @Query("SELECT loyalty_points FROM user_profiles WHERE email = :email")
    suspend fun getLoyaltyPoints(email: String): Int?
    
    @Query("UPDATE user_profiles SET loyalty_points = :points, updated_at = :updatedAt, needs_sync = 1 WHERE email = :email")
    suspend fun setLoyaltyPoints(email: String, points: Int, updatedAt: Long)
    
    @Query("UPDATE user_profiles SET loyalty_points = COALESCE(loyalty_points, 0) + :delta, updated_at = :updatedAt, needs_sync = 1 WHERE email = :email")
    suspend fun addLoyaltyPoints(email: String, delta: Int, updatedAt: Long)
    
    @Query("SELECT * FROM user_profiles WHERE needs_sync = 1")
    suspend fun getProfilesNeedingSync(): List<UserProfile>
    
    @Query("SELECT * FROM user_profiles WHERE server_id = :serverId")
    suspend fun getUserProfileByServerId(serverId: String): UserProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
    
    @Update
    suspend fun updateUserProfile(profile: UserProfile)
    
    @Query("UPDATE user_profiles SET needs_sync = :needsSync, last_synced = :lastSynced WHERE email = :email")
    suspend fun updateSyncStatus(email: String, needsSync: Boolean, lastSynced: Long?)
    
    @Query("UPDATE user_profiles SET server_id = :serverId, needs_sync = 0, last_synced = :lastSynced WHERE email = :email")
    suspend fun markAsSynced(email: String, serverId: String, lastSynced: Long)
    
    @Delete
    suspend fun deleteUserProfile(profile: UserProfile)
    
    @Query("DELETE FROM user_profiles WHERE email = :email")
    suspend fun deleteUserProfileByEmail(email: String)
    
    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getUsersCount(): Int
    
    @Query("SELECT COALESCE(SUM(loyalty_points), 0) FROM user_profiles")
    suspend fun getTotalLoyaltyPoints(): Int
    
    @Query("SELECT * FROM user_profiles ORDER BY email")
    suspend fun getAllUsers(): List<UserProfile>
}

