package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAnnouncement(announcement: ModuleAnnouncement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAnnouncements(moduleAnnouncements: List<ModuleAnnouncement>)

    @Query(
        "SELECT moduleAnnouncements.*, admins.firstName AS authorName, admins.profileImageLink AS profileImageLink " +
                "FROM moduleAnnouncements INNER JOIN admins ON moduleAnnouncements.authorID = admins.id " +
                "WHERE moduleAnnouncements.moduleID = :moduleID"
    )
    suspend fun getModuleAnnouncements(moduleID: String): List<ModuleAnnouncementsWithAuthor>

    @Query("DELETE FROM moduleAnnouncements WHERE moduleID = :moduleID")
    suspend fun clearAnnouncements(moduleID: String)

}

