package example.config

import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class ExtendDataFetcherExceptionHandler : DataFetcherExceptionHandler {
    override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters): CompletableFuture<DataFetcherExceptionHandlerResult> {
        val exception: Throwable = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path

        val error: GraphQLError = GraphQLErrorResponse(
            exception = exception,
            locations = listOf(sourceLocation),
            path = path.toList()
        )

        val result = DataFetcherExceptionHandlerResult.newResult(error).build()

        return CompletableFuture.completedFuture(result)
    }
}
