package dev.slne.surf.cloud.api.common.exceptions

import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.WordUtils
import org.springframework.boot.ExitCodeGenerator
import java.io.Serial
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val LINE_WIDTH = 80

/**
 * Represents a fatal error in the Surf Cloud application. Provides detailed error messages,
 * additional information, possible solutions, and an associated exit code.
 *
 * @property simpleErrorMessage A brief description of the error.
 * @property detailedErrorMessage A detailed explanation of the error.
 * @property cause The root cause of the error, if any.
 * @property additionalInformation Additional information about the error.
 * @property possibleSolutions Suggestions for resolving the error.
 * @property exitCode The exit code to return when terminating the application.
 */
class FatalSurfError private constructor(
    private val simpleErrorMessage: String?,
    private val detailedErrorMessage: String?,
    override val cause: Throwable?,
    private val additionalInformation: List<String>,
    private val possibleSolutions: List<String>,
    private val exitCode: Int
) : Error(), ExitCodeGenerator {

    /**
     * Builds a formatted message detailing the fatal error.
     *
     * @return The formatted error message.
     */
    fun buildMessage() = buildString {
        append("╔")
        append(StringUtils.repeat("═", LINE_WIDTH))
        append("╗")
        appendLine()
        appendIndentedLine("Fatal Error Occurred: ", simpleErrorMessage)
        appendIndentedLine("Detailed Error Message: ", detailedErrorMessage)
        appendIndentedLine("Cause: ", cause?.message)

        if (additionalInformation.isNotEmpty()) {
            appendClosedLine("Additional Information: ")
            for (info in additionalInformation) {
                appendIndentedLine(" - ", info)
            }
        }

        if (possibleSolutions.isNotEmpty()) {
            appendClosedLine("Possible Solutions: ")
            for (solution in possibleSolutions) {
                appendIndentedLine(" - ", solution)
            }
        }

        append("╚")
        append(StringUtils.repeat("═", LINE_WIDTH))
        append("╝")
    }


    private fun StringBuilder.appendClosedLine(line: String) {
        append("║ ")
        append(line)
        append(StringUtils.repeat(" ", LINE_WIDTH - line.length - 1)) // Padding for border
        append("║")
        appendLine()
    }

    private fun StringBuilder.appendIndentedLine(
        indentPrefix: String,
        line: String?
    ) {
        line ?: return

        val indentLength = indentPrefix.length
        val wrappedLines = WordUtils.wrap(line, LINE_WIDTH - indentLength - 3).lineSequence()

        appendClosedLine(indentPrefix + wrappedLines.first())

        for (wrappedLine in wrappedLines.drop(1)) {
            appendClosedLine(StringUtils.repeat(" ", indentLength) + wrappedLine)
        }
    }

    /**
     * Retrieves the exit code associated with the error.
     *
     * @return The exit code.
     */
    override fun getExitCode() = exitCode

    companion object {
        @Serial
        private val serialVersionUID = -5282893569683975781L

        /**
         * Creates a new [Builder] instance for constructing [FatalSurfError] objects.
         *
         * @return A new [Builder].
         */
        @JvmStatic
        fun builder() = Builder()
    }

    /**
     * Builder class for constructing [FatalSurfError] instances.
     */
    class Builder {
        private var simpleErrorMessage: String? = null
        private var detailedErrorMessage: String? = null
        private var cause: Throwable? = null
        private var additionalInformation: MutableList<String> = mutableListOf()
        private var possibleSolutions: MutableList<String> = mutableListOf()
        private var exitCode: Int = 0

        fun simpleErrorMessage(simpleErrorMessage: String?) = apply {
            this.simpleErrorMessage = simpleErrorMessage
        }

        fun detailedErrorMessage(detailedErrorMessage: String?) = apply {
            this.detailedErrorMessage = detailedErrorMessage
        }

        fun cause(cause: Throwable?) = apply {
            this.cause = cause
        }

        fun additionalInformation(additionalInformation: List<String>) = apply {
            this.additionalInformation += additionalInformation
        }

        fun possibleSolutions(possibleSolutions: List<String>) = apply {
            this.possibleSolutions += possibleSolutions
        }

        fun additionalInformation(additionalInformation: String) = apply {
            this.additionalInformation.add(additionalInformation)
        }

        fun possibleSolution(possibleSolutions: String) = apply {
            this.possibleSolutions.add(possibleSolutions)
        }

        fun exitCode(exitCode: Int) = apply {
            this.exitCode = exitCode
        }

        /**
         * Builds a new [FatalSurfError] instance with the configured properties.
         *
         * @return A new [FatalSurfError].
         */
        fun build(): FatalSurfError {
            return FatalSurfError(
                simpleErrorMessage,
                detailedErrorMessage,
                cause,
                additionalInformation,
                possibleSolutions,
                exitCode
            )
        }
    }
}

/**
 * DSL function for creating a [FatalSurfError] using a builder.
 *
 * @param builder A lambda to configure the [FatalSurfError.Builder].
 * @return A new [FatalSurfError].
 */
@OptIn(ExperimentalContracts::class)
fun FatalSurfError(builder: FatalSurfError.Builder.() -> Unit): FatalSurfError {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    return FatalSurfError.Builder().apply(builder).build()
}

/**
 * Contains predefined exit codes for the Surf Cloud application.
 */
object ExitCodes {
    const val NORMAL = 0
    const val UNKNOWN_ERROR = 1
    const val CLIENT_DISCONNECTED_BEFORE_RUNNING = 2
    const val TIMEOUT = 3
    const val UNABLE_TO_CONNECT_TO_DATABASE = 10
    const val CONNECTION_FAILURE = 11

    const val CLIENT_COULD_NOT_CONNECT_TO_SERVER = 20
    const val CLIENT_COULD_NOT_FETCH_SERVER_ID = 21
}
