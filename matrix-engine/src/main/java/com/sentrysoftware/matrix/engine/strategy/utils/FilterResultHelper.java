package com.sentrysoftware.matrix.engine.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TAB;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterResultHelper {

	/**
	 * <p>Filter the lines:
	 * <li>In removing the header if exists: all the lines from start to removeHeader number.</li>
	 * <li>In removing the Footer if exists: all the removeFooter number lines from the end.</li>
	 * <li>In removing all the lines matching to the excludeRegExp if exist</li>
	 * <li>In keeping only the lines matching to the keepOnlyRegExp if exist</li>
	 * </p>
	 * @param lines The lines to be filtered. (mandatory)
	 * @param removeHeader The number of lines to ignored from the start.
	 * @param removeFooter  The number of lines to ignored from the end.
	 * @param excludeRegExp The PSL regexp to exclude lines.
	 * @param keepOnlyRegExp The PSL regexp for lines to keep.
	 * @return The filterd lines.
	 */
	public static List<String> filterLines(
			@NonNull
			final List<String> lines,
			final Integer removeHeader,
			final Integer removeFooter,
			final String excludeRegExp,
			final String keepOnlyRegExp) {

		// Remove header : remove number of lines from beginning
		final int begin = removeHeader != null ? removeHeader : 0;

		// Remove footer : remove number of lines from the end.
		final int end =
				removeFooter != null ?
						lines.size() - removeFooter :
							lines.size();
		
		final Pattern excludePattern =
				excludeRegExp == null || excludeRegExp.isEmpty() ?
						null :
							Pattern.compile(PslUtils.psl2JavaRegex(excludeRegExp));
		
		final Pattern keepOnlyPattern =
				keepOnlyRegExp == null || keepOnlyRegExp.isEmpty() ?
						null :
							Pattern.compile(PslUtils.psl2JavaRegex(keepOnlyRegExp));

		// Remove lines containing a given regular expression excludeRegExp
		// Keep only the lines containing a given regular expression
		return IntStream.range(begin, end)
				.mapToObj(i -> lines.get(i))
				.filter(line -> (excludePattern == null || !excludePattern.matcher(line).find()) &&
									(keepOnlyPattern == null || keepOnlyPattern.matcher(line).find()))
				.collect(Collectors.toList());
	}

	/**
	 * Select the columns in the lines.
	 * Extract separators and split each line with these separators
	 * keep only values (from the split result) which index matches with the selected column list 
	 * @param lines The lines (mandatory)
	 * @param separators The separators
	 * @param selectColumns The list of the selected columns position.
	 * 
	 * @return The lines with the selected columns.
	 */
	public static List<String> selectedColumns(
			@NonNull
			final List<String> lines,
			final String separators,
			final List<String> selectColumns) {

		if (separators == null || separators.isEmpty() ||
				selectColumns == null || selectColumns.isEmpty()) {
			return lines;
		}

		final String selectColumnsStr = selectColumns.stream().collect(Collectors.joining(","));

		return lines.stream()
				.map(line -> {
					// protect the initial string that contains ";" and replace it with "," if this
					// latest is not in Separators list. Otherwise, just remove the ";"
					// replace all separators by ";", which is the standard separator used by MS_HW
					if (!separators.contains(TABLE_SEP) && !separators.contains(",")) {
						return line.replace(TABLE_SEP, ",");
					} 
					if (!separators.contains(TABLE_SEP)) {
						return line.replace(TABLE_SEP, "");
					} 
					return line;
				})
				.map(line -> !separators.contains(TAB) && !separators.contains(WHITE_SPACE) ?
						// if separator = tab or simple space, then ignore empty cells
						// equivalent to ntharg
						PslUtils.nthArgf(line, selectColumnsStr, separators, TABLE_SEP) :
							PslUtils.nthArg(line, selectColumnsStr, separators, TABLE_SEP))
				.collect(Collectors.toList());
	}
}
