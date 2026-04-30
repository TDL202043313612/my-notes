package hf.inner.notebook.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import hf.inner.notebook.bean.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag WHERE tag IS NOT NULL AND tag != '' AND is_city_tag IS 0 order by update_time desc")
    fun queryAll(): Flow<List<Tag>>

    @Query("SELECT * FROM Tag order by update_time desc")
    fun queryAllTagList(): List<Tag>

    @Query("SELECT * FROM Tag WHERE tag =:name LIMIT 1")
    fun getByName(name: String): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tag: Tag): Long

    @Update
    fun update(tag: Tag)

    fun insertOrUpdate(tag: Tag) {
        val oldTag = getByName(tag.tag)
        if (oldTag == null) {
            tag.count = 1
            insert(tag)
        } else {
            tag.count = ++oldTag.count
            update(tag)
        }
    }

    @Delete
    fun delete(note: Tag)

    fun deleteOrUpdate(tag: Tag) {
        tag.count = -- tag.count
        if (tag.count <= 0) {
            delete(tag)
        } else {
            update(tag)
        }
    }
}