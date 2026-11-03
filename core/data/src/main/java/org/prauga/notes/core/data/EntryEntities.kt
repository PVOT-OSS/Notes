// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int?,
)

data class TimeColumns(
    val value: String,
    val zone: String?,
    @ColumnInfo(name = "is_date") val isDate: Boolean,
)

@Entity(
    tableName = "entries",
    foreignKeys =
        [
            ForeignKey(
                entity = CollectionEntity::class,
                parentColumns = ["id"],
                childColumns = ["collection_id"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices =
        [Index("uid", unique = true), Index("collection_id"), Index("component", "last_modified")],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val component: String,
    @ColumnInfo(name = "collection_id") val collectionId: Long,
    val summary: String,
    val description: String,
    val color: Int?,
    @Embedded(prefix = "start_") val start: TimeColumns?,
    @Embedded(prefix = "due_") val due: TimeColumns?,
    val completed: Long?,
    val status: String?,
    val classification: String?,
    val priority: Int?,
    @ColumnInfo(name = "percent_complete") val percentComplete: Int?,
    val created: Long,
    @ColumnInfo(name = "last_modified") val lastModified: Long,
    val sequence: Int,
)

@Fts4(contentEntity = EntryEntity::class)
@Entity(tableName = "entries_fts")
data class EntryFts(val summary: String, val description: String)

@Entity(
    tableName = "categories",
    primaryKeys = ["entry_id", "name"],
    foreignKeys =
        [
            ForeignKey(
                entity = EntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["entry_id"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index("name")],
)
data class CategoryEntity(
    @ColumnInfo(name = "entry_id") val entryId: Long,
    val name: String,
    val position: Int,
)

data class EntryWithCategories(
    @Embedded val entry: EntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entry_id") val categories: List<CategoryEntity>,
)

data class CategoryCount(val name: String, val count: Int)

data class CollectionCount(val id: Long, val name: String, val color: Int?, val count: Int)
