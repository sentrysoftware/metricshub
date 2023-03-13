package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = CopySource.class, name = "copy"),
		@JsonSubTypes.Type(value = HttpSource.class, name = "http"),
		@JsonSubTypes.Type(value = IpmiSource.class, name = "ipmi"),
		@JsonSubTypes.Type(value = OsCommandSource.class, name = "osCommand"),
		@JsonSubTypes.Type(value = SnmpGetSource.class, name = "snmpGet"),
		@JsonSubTypes.Type(value = SnmpTableSource.class, name = "snmpTable"),
		@JsonSubTypes.Type(value = SshInteractiveSource.class, name = "sshInteractive"),
		@JsonSubTypes.Type(value = StaticSource.class, name = "static"),
		@JsonSubTypes.Type(value = TableJoinSource.class, name = "tableJoin"),
		@JsonSubTypes.Type(value = TableUnionSource.class, name = "tableUnion"),
		@JsonSubTypes.Type(value = UcsSource.class, name = "ucs"),
		@JsonSubTypes.Type(value = WbemSource.class, name = "wbem"),
		@JsonSubTypes.Type(value = WmiSource.class, name = "wmi"),
	}
)
@Data
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;
	@JsonSetter(nulls = SKIP)
	private List<Compute> computes = new ArrayList<>();
	protected boolean forceSerialization;
	@JsonIgnore
	protected String key;
	protected ExecuteForEachEntryOf executeForEachEntryOf;

	protected Source(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		this.type = type;
		this.computes = computes == null ? new ArrayList<>() : computes;
		this.forceSerialization = forceSerialization;
		this.key = key;
		this.executeForEachEntryOf = executeForEachEntryOf;
	}

	public abstract Source copy();

	public abstract void update(UnaryOperator<String> updater);

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(new StringBuilder("- ").append(key).append(".type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- forceSerialization=", forceSerialization);
		// A small trick here because the executeForEachEntryOf.toString value is already
		// formatted that's why we don't need a prefix for the string value of the nested executeForEachEntryOf
		addNonNull(stringJoiner, EMPTY, executeForEachEntryOf != null ? executeForEachEntryOf.toString() : null);

		return stringJoiner.toString();

	}

	/**
	 * Whether the {@link ExecuteForEachEntryOf} is present in the {@link Source} or
	 * not
	 * 
	 * @return <code>true</code> if {@link ExecuteForEachEntryOf} is present otherwise
	 *         <code>false</code>
	 */
	public boolean isExecuteForEachEntryOf() {
		return executeForEachEntryOf != null && executeForEachEntryOf.getSource() != null
				&& !executeForEachEntryOf.getSource().isBlank();
	}

	/**
	 * Return the source references concatenated to the executeForEachEntryOf reference.
	 * 
	 * @return new String array
	 */
	public String[] getPossibleReferences() {

		// TODO implement

		return new String[] { };

	}
}
