package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategorySum(
    val category: String,
    val total: Double,
    val count: Int
)

data class DailySum(
    val dayTimestamp: Long,
    val total: Double
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteTransaction(id: Long, userId: Long)

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getTransactionById(id: Long, userId: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateEpochMillis DESC, id DESC")
    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateEpochMillis DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis 
        ORDER BY dateEpochMillis DESC, id DESC
    """)
    fun getTransactionsInRange(userId: Long, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis 
        ORDER BY dateEpochMillis DESC, id DESC
    """)
    suspend fun getTransactionsInRangeSync(userId: Long, startMillis: Long, endMillis: Long): List<TransactionEntity>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE userId = :userId AND type = :type
    """)
    fun getTotalByType(userId: Long, type: String): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE userId = :userId 
          AND type = :type 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis
    """)
    fun getSumByTypeInRange(userId: Long, type: String, startMillis: Long, endMillis: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE userId = :userId 
          AND type = :type 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis
    """)
    suspend fun getSumByTypeInRangeSync(userId: Long, type: String, startMillis: Long, endMillis: Long): Double

    @Query("""
        SELECT category, SUM(amount) as total, COUNT(*) as count 
        FROM transactions 
        WHERE userId = :userId 
          AND type = 'EXPENSE' 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun getCategorySpending(userId: Long, startMillis: Long, endMillis: Long): Flow<List<CategorySum>>

    @Query("""
        SELECT category, SUM(amount) as total, COUNT(*) as count 
        FROM transactions 
        WHERE userId = :userId 
          AND type = 'EXPENSE' 
          AND dateEpochMillis >= :startMillis 
          AND dateEpochMillis <= :endMillis 
        GROUP BY category 
        ORDER BY total DESC
    """)
    suspend fun getCategorySpendingSync(userId: Long, startMillis: Long, endMillis: Long): List<CategorySum>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateEpochMillis DESC")
    suspend fun getAllTransactionsSync(userId: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
