package example.controller.response

interface Connection<T> {
    val totalCount: Int
    val responses: List<T>
}

data class DefaultConnection<T>(
    override val totalCount: Int,
    override val responses: List<T>
) : Connection<T> {
    companion object {
        fun <T> of(items: List<T>, totalCount: Int): Connection<T> {
            return DefaultConnection(
                totalCount = totalCount,
                responses = items
            )
        }
    }
}
