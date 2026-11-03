// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY id") fun observeAll(): Flow<List<CollectionEntity>>

    @Query(
        """
        SELECT collections.id AS id, collections.name AS name, collections.color AS color,
               COUNT(entries.id) AS count
        FROM collections LEFT JOIN entries
          ON entries.collection_id = collections.id AND entries.component = :component
        GROUP BY collections.id ORDER BY collections.id
        """
    )
    fun observeWithCounts(component: String): Flow<List<CollectionCount>>

    @Query("SELECT * FROM collections ORDER BY id LIMIT 1") suspend fun first(): CollectionEntity?

    @Insert suspend fun insert(collection: CollectionEntity): Long

    @Update suspend fun update(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id") suspend fun delete(id: Long)
}
