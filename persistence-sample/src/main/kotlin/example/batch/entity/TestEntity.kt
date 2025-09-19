package example.batch.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "test_entity")
data class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false)
    var amount: Int,
    
    @Column(nullable = false)
    var description: String,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)