package example.controller.response

import org.springframework.data.domain.Pageable

interface Connection<T> {
    val totalCount: Int
    val edges: List<Edge<T>>
}

interface Edge<T> {
    val cursor: String
    val node: T
}

data class DefaultConnection<T>(
    override val totalCount: Int,
    override val edges: List<Edge<T>>
) : Connection<T> {
    companion object {
        fun <T> of(items: List<T>, totalCount: Int): Connection<T> {
            return DefaultConnection(
                totalCount = totalCount,
                edges = items.map {
                    DefaultEdge(cursor = "", node = it)
                }
            )
        }
    }
}

data class DefaultEdge<T>(
    override val cursor: String,
    override val node: T,
) : Edge<T>
