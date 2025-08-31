package example.config

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseStatus

/*
@ControllerAdvice
class GraphQLExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @GraphQlExceptionHandler(Throwable::class)
    fun handleException() : String {
        println("graphql handleException")
        return ""
    }
}
*/
