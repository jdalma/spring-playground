package example.config

import graphql.ErrorClassification
import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorHelper
import graphql.language.SourceLocation
import java.io.PrintWriter
import java.io.StringWriter

class GraphQLErrorResponse (
    private val exception: Throwable,
    private val locations: List<SourceLocation>? = null,
    private val path: List<Any>? = null,
) : GraphQLError {

    override fun getErrorType(): ErrorClassification = ErrorType.DataFetchingException

    override fun getMessage(): String = exception.message
        ?: "Exception while fetching data (${path?.joinToString("/").orEmpty()})"

    override fun getLocations(): List<SourceLocation>? = locations

    override fun getPath(): List<Any>? = path

    override fun getExtensions(): Map<String, Any> = when {
        exception is GraphQLError && exception.extensions != null -> exception.extensions
        else -> buildExtensions(exception)
    }

    override fun toSpecification(): Map<String, Any> {
        return GraphqlErrorHelper.toSpecification(this)
    }

    private fun buildExtensions(exception: Throwable): Map<String, Any> {
        val data = HashMap<String, Any>()
        val exceptionData = HashMap<String, Any>()

        data["exception"] = exceptionData
        exceptionData["name"] = exception::class.java.simpleName

        val sw = StringWriter()
        exception.printStackTrace(PrintWriter(sw))
        exceptionData["stacktrace"] = sw.toString()
            .replace("\t", "  ")
            .split("\n")
            .take(10)

        return data;
    }
}
