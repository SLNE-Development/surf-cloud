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
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.ExitCodeGenerator;

@Builder
@AllArgsConstructor
public final class FatalSurfError extends
    Error implements ExitCodeGenerator { // TODO: 18.09.2024 13:04 - actually shutdown server on this error

  @Serial
  private static final long serialVersionUID = -5282893569683975781L;

  private static final int LINE_WIDTH = 70;

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

    appendLine(builder, "Fatal Error Occurred: ", simpleErrorMessage);

    if (detailedErrorMessage != null) {
      appendLine(builder, "Detailed Error Message: ", detailedErrorMessage);
    }

    if (cause != null) {
      appendLine(builder, "Cause: ", cause.getMessage());
    }

    if (!additionalInformation.isEmpty()) {
      builder.append("║ Additional Information: ")
          .append(System.lineSeparator());
      additionalInformation.forEach(info -> appendLine(builder, " - ", info));
    }

    if (!possibleSolutions.isEmpty()) {
      builder.append("║ Possible Solutions: ")
          .append(System.lineSeparator());
      possibleSolutions.forEach(solution -> appendLine(builder, " - ", solution));
    }

    builder.append("╚")
        .append(StringUtils.repeat("═", LINE_WIDTH))
        .append("╝");

    return builder.toString();
  }

  private void appendLine(StringBuilder builder, String prefix, String line) {
    List<String> wrappedLines = WordUtils.wrap(line, LINE_WIDTH - prefix.length()).lines()
        .map(s -> "║ " + " ".repeat(prefix.length()) + s)
        .toList();

    builder.append(prefix)
        .append(wrappedLines.isEmpty() ? "║" : wrappedLines.getFirst())
        .append(System.lineSeparator());

    wrappedLines.stream()
        .skip(1)
        .forEach(l -> builder.append(l)
            .append(System.lineSeparator()));
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
