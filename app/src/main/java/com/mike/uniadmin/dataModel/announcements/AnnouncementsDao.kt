package com.mike.uniadmin.dataModel.announcements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface AnnouncementsDao{
    @Query("SELECT * FROM announcements")
    suspend fun getAnnouncements(): List<AnnouncementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements WHERE id = :announcementId")
    suspend fun deleteAnnouncement(announcementId: String)


}