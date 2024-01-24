package org.sentrysoftware.metricshub.engine.strategy.source;

import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableJoinSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableUnionSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;

/**
 * Interface for processing different types of data sources.
 * Implementations of this interface handle specific source types and provide a SourceTable as a result.
 */
public interface ISourceProcessor {
	/**
	 * Process the CopySource and return a SourceTable.
	 *
	 * @param copySource The CopySource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(CopySource copySource);

	/**
	 * Process the HttpSource and return a SourceTable.
	 *
	 * @param httpSource The HttpSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(HttpSource httpSource);

	/**
	 * Process the IpmiSource and return a SourceTable.
	 *
	 * @param ipmiSource The IpmiSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(IpmiSource ipmiSource);

	/**
	 * Process the OsCommandSource and return a SourceTable.
	 *
	 * @param osCommandSource The OsCommandSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(OsCommandSource osCommandSource);

	/**
	 * Process the SnmpGetSource and return a SourceTable.
	 *
	 * @param snmpSource The SnmpGetSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(SnmpGetSource snmpSource);

	/**
	 * Process the SnmpTableSource and return a SourceTable.
	 *
	 * @param snmpTableSource The SnmpTableSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(SnmpTableSource snmpTableSource);

	/**
	 * Process the StaticSource and return a SourceTable.
	 *
	 * @param staticSource The StaticSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(StaticSource staticSource);

	/**
	 * Process the TableJoinSource and return a SourceTable.
	 *
	 * @param tableJoinSource The TableJoinSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(TableJoinSource tableJoinSource);

	/**
	 * Process the TableUnionSource and return a SourceTable.
	 *
	 * @param tableUnionSource The TableUnionSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(TableUnionSource tableUnionSource);

	/**
	 * Process the WbemSource and return a SourceTable.
	 *
	 * @param wbemSource The WbemSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(WbemSource wbemSource);

	/**
	 * Process the WmiSource and return a SourceTable.
	 *
	 * @param wmiSource The WmiSource to process.
	 * @return The SourceTable result.
	 */
	SourceTable process(WmiSource wmiSource);
}
