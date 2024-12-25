package example.example01

import java.time.LocalDate

data class Example01PersonModel(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate,
    val occupation: String?
)
