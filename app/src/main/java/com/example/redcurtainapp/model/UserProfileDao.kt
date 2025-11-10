package com.example.redcurtainapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getUserProfileByEmail(email: String): UserProfile?
    
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    fun getUserProfileByEmailFlow(email: String): Flow<UserProfile?>
    
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
}

