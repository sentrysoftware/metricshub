package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import java.io.Serializable;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = Add.class, name = "add"),
		@JsonSubTypes.Type(value = And.class, name = "and"),
		@JsonSubTypes.Type(value = ArrayTranslate.class, name = "arrayTranslate"),
		@JsonSubTypes.Type(value = Awk.class, name = "awk"),
		@JsonSubTypes.Type(value = Convert.class, name = "convert"),
		@JsonSubTypes.Type(value = Divide.class, name = "divide"),
		@JsonSubTypes.Type(value = DuplicateColumn.class, name = "duplicateColumn"),
		@JsonSubTypes.Type(value = ExcludeMatchingLines.class, name = "excludeMatchingLines"),
		@JsonSubTypes.Type(value = Extract.class, name = "extract"),
		@JsonSubTypes.Type(value = ExtractPropertyFromWbemPath.class, name = "extractPropertyFromWbemPath"),
		@JsonSubTypes.Type(value = Json2Csv.class, name = "json2Csv"),
		@JsonSubTypes.Type(value = KeepColumns.class, name = "keepColumns"),
		@JsonSubTypes.Type(value = KeepOnlyMatchingLines.class, name = "keepOnlyMatchingLines"),
		@JsonSubTypes.Type(value = LeftConcat.class, name = "leftConcat"),
		@JsonSubTypes.Type(value = Multiply.class, name = "multiply"),
		@JsonSubTypes.Type(value = PerBitTranslation.class, name = "perBitTranslation"),
		@JsonSubTypes.Type(value = Replace.class, name = "replace"),
		@JsonSubTypes.Type(value = RightConcat.class, name = "rightConcat"),
		@JsonSubTypes.Type(value = Substract.class, name = "substract"),
		@JsonSubTypes.Type(value = Substring.class, name = "substring"),
		@JsonSubTypes.Type(value = Translate.class, name = "translate"),
		@JsonSubTypes.Type(value = Xml2Csv.class, name = "xml2Csv"),
	}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Compute implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;

	public abstract Compute copy();

	public abstract void update(UnaryOperator<String> updater);

	@Override
	public String toString() {
		return new StringBuilder("- type=")
			.append(this.getClass().getSimpleName())
			.toString();
	}
}
