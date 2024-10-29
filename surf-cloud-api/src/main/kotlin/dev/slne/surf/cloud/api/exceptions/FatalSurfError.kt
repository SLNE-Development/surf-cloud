package dev.slne.surf.cloud.api.exceptions

import lombok.Builder
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.WordUtils
import org.springframework.boot.ExitCodeGenerator
import java.io.Serial
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private const val LINE_WIDTH = 80

@Builder
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

    override fun getExitCode() = exitCode

    companion object {
        @Serial
        private val serialVersionUID = -5282893569683975781L

        @JvmStatic
        fun builder() = Builder()
    }

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

@OptIn(ExperimentalContracts::class)
fun FatalSurfError(builder: FatalSurfError.Builder.() -> Unit): FatalSurfError {
    contract { callsInPlace(builder, kotlin.contracts.InvocationKind.EXACTLY_ONCE) }
    return FatalSurfError.Builder().apply(builder).build()
}

object ExitCodes {
    const val NORMAL = 0
    const val UNKNOWN_ERROR = 1
    const val UNABLE_TO_CONNECT_TO_DATABASE = 10
    const val CONNECTION_FAILURE = 11
}
