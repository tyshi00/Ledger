package com.tyshi00.ledger.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "entries")
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val categoryId: String,
    val type: String,
    val frequency: String = "MONTHLY",
    val note: String? = null,
    val year: Int,
    val month: Int,
    val day: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey val id: String,
    val label: String,
    val parentGroup: String,
    val type: String,
    val sortOrder: Int = 0,
)

@Entity(tableName = "budget_targets")
data class BudgetTarget(
    @PrimaryKey val id: String,
    val groupName: String,
    val type: String,
    val amountCents: Long,
    val year: Int,
    val month: Int,
)

@Entity(tableName = "preferences")
data class PrefEntry(
    @PrimaryKey val key: String,
    val value: String,
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE year = :year AND month = :month ORDER BY day DESC, createdAt DESC")
    suspend fun getByMonth(year: Int, month: Int): List<LedgerEntry>

    @Query("SELECT * FROM entries WHERE year = :year AND month = :month AND type = :type ORDER BY day DESC, createdAt DESC")
    suspend fun getByMonthAndType(year: Int, month: Int, type: String): List<LedgerEntry>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM entries WHERE year = :year AND month = :month AND type = :type")
    suspend fun sumByType(year: Int, month: Int, type: String): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM entries WHERE year = :year AND month = :month AND categoryId = :categoryId")
    suspend fun sumByCategory(year: Int, month: Int, categoryId: String): Long

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): LedgerEntry?

    @Insert
    suspend fun insert(entry: LedgerEntry): Long

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM entries ORDER BY year DESC, month DESC, day DESC, createdAt DESC")
    suspend fun getAll(): List<LedgerEntry>

    @Query("DELETE FROM entries")
    suspend fun deleteAll()
}

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories WHERE parentGroup = :group ORDER BY sortOrder ASC")
    suspend fun getByGroup(group: String): List<CustomCategory>

    @Query("SELECT * FROM custom_categories ORDER BY parentGroup, sortOrder ASC")
    suspend fun getAll(): List<CustomCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CustomCategory)

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAll()
}

@Dao
interface BudgetTargetDao {
    @Query("SELECT * FROM budget_targets WHERE year = :year AND month = :month")
    suspend fun getByMonth(year: Int, month: Int): List<BudgetTarget>

    @Query("SELECT * FROM budget_targets WHERE year = :year AND month = :month AND groupName = :group")
    suspend fun getByGroupAndMonth(group: String, year: Int, month: Int): BudgetTarget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(target: BudgetTarget)

    @Query("SELECT * FROM budget_targets ORDER BY year DESC, month DESC")
    suspend fun getAll(): List<BudgetTarget>

    @Query("DELETE FROM budget_targets")
    suspend fun deleteAll()
}

@Dao
interface PrefDao {
    @Query("SELECT * FROM preferences WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): PrefEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entry: PrefEntry)

    @Query("SELECT * FROM preferences")
    suspend fun getAll(): List<PrefEntry>

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()
}

@Database(
    entities = [
        LedgerEntry::class,
        CustomCategory::class,
        BudgetTarget::class,
        PrefEntry::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun customCategoryDao(): CustomCategoryDao
    abstract fun budgetTargetDao(): BudgetTargetDao
    abstract fun prefDao(): PrefDao
}
