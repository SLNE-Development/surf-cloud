package dev.slne.surf.cloud.api.exceptions;

import java.io.Serial;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.ExitCodeGenerator;

@Builder
@AllArgsConstructor
public final class FatalSurfError extends Error implements ExitCodeGenerator {

  @Serial
  private static final long serialVersionUID = -5282893569683975781L;

  private static final int LINE_WIDTH = 80;

  private final @NonNull String simpleErrorMessage;
  private final @Nullable String detailedErrorMessage;
  private final @Nullable Throwable cause;
  private final @Singular("additionalInformation") List<String> additionalInformation;
  private final @Singular("possibleSolution") List<String> possibleSolutions;

  @Getter
  @Accessors(fluent = true)
  private final int exitCode;

  /**
   * Builds a formatted message detailing the fatal error.
   *
   * @return The formatted error message.
   */
  public String buildMessage() {
    final StringBuilder builder = new StringBuilder();

    builder.append("╔")
        .append(StringUtils.repeat("═", LINE_WIDTH))
        .append("╗")
        .append(System.lineSeparator());

    appendIndentedLine(builder, "Fatal Error Occurred: ", simpleErrorMessage);

    if (detailedErrorMessage != null) {
      appendIndentedLine(builder, "Detailed Error Message: ", detailedErrorMessage);
    }

    if (cause != null) {
      appendIndentedLine(builder, "Cause: ", cause.getMessage());
    }

    if (!additionalInformation.isEmpty()) {
      appendClosedLine(builder, "Additional Information: ");
      additionalInformation.forEach(info -> appendIndentedLine(builder, " - ", info));
    }

    if (!possibleSolutions.isEmpty()) {
      appendClosedLine(builder, "Possible Solutions: ");
      possibleSolutions.forEach(solution -> appendIndentedLine(builder, " - ", solution));
    }

    builder.append("╚")
        .append(StringUtils.repeat("═", LINE_WIDTH))
        .append("╝");

    return builder.toString();
  }

  private void appendClosedLine(StringBuilder builder, String line) {
    builder.append("║ ")
        .append(line)
        .append(StringUtils.repeat(" ", LINE_WIDTH - line.length() - 1)) // Padding for border
        .append("║")
        .append(System.lineSeparator());
  }

  private void appendIndentedLine(
      @NotNull StringBuilder builder,
      @NotNull String indentPrefix,
      String line
  ) {
    final int indentLength = indentPrefix.length();
    final List<String> wrappedLines = WordUtils.wrap(line, LINE_WIDTH - indentLength - 3)
        .lines()
        .toList();

    appendClosedLine(builder, indentPrefix + wrappedLines.getFirst());

    for (int i = 1; i < wrappedLines.size(); i++) {
      appendClosedLine(builder, StringUtils.repeat(" ", indentLength) + wrappedLines.get(i));
    }
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  @UtilityClass
  public static class ExitCodes {

    public final int UNABLE_TO_CONNECT_TO_DATABASE = 10;
  }
}
