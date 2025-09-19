package example.batch

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

data class TestData(
    val name: String,
    val amount: Int,
    val description: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=none"
])
class JdbcOnlyPerformanceTest {
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS test_entity")
        jdbcTemplate.execute("""
            CREATE TABLE test_entity (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                amount INT NOT NULL,
                description VARCHAR(500) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """)
    }
    
    private fun generateTestData(count: Int): List<TestData> {
        return (1..count).map { i ->
            TestData(
                name = "Name_$i",
                amount = i,
                description = "This is a sample description for entity number $i with some additional text to simulate realistic data size"
            )
        }
    }
    
    @Test
    @Transactional
    fun testBatchInsertPerformance() {
        val batchSizes = listOf(100, 500, 1000, 2500, 5000)
        
        println("\n" + "=".repeat(80))
        println(" JDBC BATCH INSERT PERFORMANCE TEST ")
        println("=".repeat(80))
        
        for (size in batchSizes) {
            println("\n--- Batch Size: $size ---")
            
            // Single Insert
            jdbcTemplate.execute("DELETE FROM test_entity")
            val singleInsertTime = testSingleInsert(size)
            println(String.format("Single Insert    : %6d ms | %8.2f ops/sec", 
                singleInsertTime, if (singleInsertTime > 0) (size * 1000.0) / singleInsertTime else 0.0))
            
            // Batch Insert
            jdbcTemplate.execute("DELETE FROM test_entity")
            val batchInsertTime = testBatchInsert(size)
            println(String.format("Batch Insert     : %6d ms | %8.2f ops/sec", 
                batchInsertTime, if (batchInsertTime > 0) (size * 1000.0) / batchInsertTime else 0.0))
            
            // Improvement
            if (singleInsertTime > 0 && batchInsertTime > 0) {
                val improvement = singleInsertTime.toDouble() / batchInsertTime
                println(String.format("Improvement      : %.2fx faster with batch", improvement))
            }
        }
    }
    
    @Test 
    @Transactional
    fun testBatchUpdatePerformance() {
        val batchSizes = listOf(100, 500, 1000, 2500, 5000)
        
        println("\n" + "=".repeat(80))
        println(" JDBC BATCH UPDATE PERFORMANCE TEST ")
        println("=".repeat(80))
        
        for (size in batchSizes) {
            println("\n--- Batch Size: $size ---")
            
            // Prepare data
            prepareDataForUpdate(size)
            val singleUpdateTime = testSingleUpdate(size)
            println(String.format("Single Update    : %6d ms | %8.2f ops/sec", 
                singleUpdateTime, if (singleUpdateTime > 0) (size * 1000.0) / singleUpdateTime else 0.0))
            
            prepareDataForUpdate(size)
            val batchUpdateTime = testBatchUpdate(size)
            println(String.format("Batch Update     : %6d ms | %8.2f ops/sec", 
                batchUpdateTime, if (batchUpdateTime > 0) (size * 1000.0) / batchUpdateTime else 0.0))
            
            if (singleUpdateTime > 0 && batchUpdateTime > 0) {
                val improvement = singleUpdateTime.toDouble() / batchUpdateTime
                println(String.format("Improvement      : %.2fx faster with batch", improvement))
            }
        }
    }
    
    private fun prepareDataForUpdate(size: Int) {
        jdbcTemplate.execute("DELETE FROM test_entity")
        val data = generateTestData(size)
        jdbcTemplate.batchUpdate(
            "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
            data.map { arrayOf(it.name, it.amount, it.description, it.createdAt, it.updatedAt) }
        )
    }
    
    private fun testSingleInsert(count: Int): Long {
        val data = generateTestData(count)
        
        return measureTimeMillis {
            data.forEach { item ->
                jdbcTemplate.update(
                    "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                    item.name, item.amount, item.description, item.createdAt, item.updatedAt
                )
            }
        }
    }
    
    private fun testBatchInsert(count: Int): Long {
        val data = generateTestData(count)
        
        return measureTimeMillis {
            jdbcTemplate.batchUpdate(
                "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                object : BatchPreparedStatementSetter {
                    override fun setValues(ps: PreparedStatement, i: Int) {
                        val item = data[i]
                        ps.setString(1, item.name)
                        ps.setInt(2, item.amount)
                        ps.setString(3, item.description)
                        ps.setTimestamp(4, Timestamp.valueOf(item.createdAt))
                        ps.setTimestamp(5, Timestamp.valueOf(item.updatedAt))
                    }
                    
                    override fun getBatchSize(): Int = data.size
                }
            )
        }
    }
    
    private fun testSingleUpdate(count: Int): Long {
        return measureTimeMillis {
            (1..count).forEach { i ->
                jdbcTemplate.update(
                    "UPDATE test_entity SET name = ?, amount = ?, description = ?, updated_at = ? WHERE id = ?",
                    "UpdatedName_$i", i * 2, "Updated description for entity $i", LocalDateTime.now(), i
                )
            }
        }
    }
    
    private fun testBatchUpdate(count: Int): Long {
        return measureTimeMillis {
            jdbcTemplate.batchUpdate(
                "UPDATE test_entity SET name = ?, amount = ?, description = ?, updated_at = ? WHERE id = ?",
                (1..count).map { i ->
                    arrayOf("UpdatedName_$i", i * 2, "Updated description for entity $i", LocalDateTime.now(), i)
                }
            )
        }
    }
}