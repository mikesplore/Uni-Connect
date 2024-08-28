package com.mike.uniadmin.backEnd.announcements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface AnnouncementsDao {
    @Query(
        "SELECT announcements.*, admins.firstName AS authorName, admins.profileImageLink AS profileImageLink " +
                "FROM announcements INNER JOIN admins ON announcements.authorId = admins.id"
    )
    suspend fun getAnnouncements(): List<AnnouncementsWithAuthor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :announcementId")
    suspend fun deleteAnnouncement(announcementId: String)


}