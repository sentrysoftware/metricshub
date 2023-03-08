package com.sentrysoftware.matrix.connector.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;

import lombok.NonNull;

public abstract class SourceConnectorUpdateChain extends AbstractConnectorUpdateChain {

	private static final Map<Integer, Set<String>> sourceLevels = new HashMap<>();
	private static final Map<String, Set<String>> sourceDependencies = new HashMap<>();
	
	/**
	 * Get the correct order of the sources execution Check for each source if its
	 * references have been already executed
	 * 
	 * @param sources
	 * @return
	 * @throws Exception 
	 */
	protected List<Set<String>> updateSourceDependency(final Map<String, Source> sources, String context) {
		// initialize
		sourceLevels.clear();
		sourceDependencies.clear();
		
		Set<String> done = new HashSet<>();
		Set<String> pending = new HashSet<>();

		// process sources
		for (String sourceId : sources.keySet()) {
			Source currentSource = sources.get(sourceId);
			Set<String> dependencies = getDependencies(currentSource, context, sources.keySet());
			sourceDependencies.put(sourceId, dependencies);
			if (dependencies.isEmpty()) {
				done.add(sourceId);
			} else {
				pending.add(sourceId);
			}
		}
		
		// add all sources without dependency on level 1
		sourceLevels.put(1, done);
		// make sure that you pass the copy of done Set in order to avoid that this
		// reference will be modified
		processDependencies(pending, done.stream().collect(Collectors.toSet()));

		return getSourceDependencies(sourceLevels);
	}

	
	/**
	 * Recursive function that check for each element of the pending list, if all
	 * its references have been processed stop call once all pending sources have
	 * been processed
	 * 
	 * @param pending
	 * @param done
	 */
	private void processDependencies(Set<String> pending, Set<String> done) {
		Set<String> levelSource = new HashSet<>();
		// check if all dependencies are done
		for (String src : pending) {
			Set<String> dependencySet = sourceDependencies.get(src);
			if (done.containsAll(dependencySet)) {
				levelSource.add(src);
			}
		}

		if (!levelSource.isEmpty()) {
			sourceLevels.put(sourceLevels.size() + 1, levelSource);
			done.addAll(levelSource);
			pending.removeAll(levelSource);
		}

		if (!pending.isEmpty()) {
			processDependencies(pending, done);
		}
	}

	/**
	 * Get the dependency list of each source, make sure that the dependency really exists in order to avoid infinite loop on processing
	 * @param source
	 * @param set 
	 * @param context 
	 * @return
	 * @throws Exception 
	 */
	private Set<String> getDependencies(Source source, String context, Set<String> set)  {
		Set<String> ref = new HashSet<>();
		if (source instanceof CopySource src) {
			// check if we keep or ignore this source : same context and 
			
			@NonNull
			String from = src.getFrom();
			String sourceString = getSourceString(from);
			if (checksSource(from, context, set, sourceString)) {
				ref.add(sourceString);
			}
		}

		if (source instanceof TableUnionSource src) {
			for (String unionSource : src.getTables() ) {
				@NonNull
				String sourceString = getSourceString(unionSource);
				if (checksSource(unionSource, context, set, sourceString)) {
					ref.add(sourceString);
				}
			}
		}

		if (source instanceof TableJoinSource src) {
			String table = src.getLeftTable();
			@NonNull
			String sourceString = getSourceString(table);
			if (checksSource(table, context, set, sourceString)) {
				ref.add(sourceString);
			}

			table = src.getRightTable();
			sourceString = getSourceString(table);
			if (checksSource(table, context, set, sourceString)) {
				ref.add(sourceString);
			}
		}
		
		if (source.isExecuteForEachEntryOf()) {
			String table = source.getExecuteForEachEntryOf().getSource();
			@NonNull
			String sourceString = getSourceString(table);
			if (checksSource(table, context, set, sourceString)) {
				ref.add(sourceString);
			}
			
		}

		return ref;
	}
	
	/**
	 * Check that dependency 
	 * 1- is part of the current context
	 * 2- has been actually declared for the given monitor
	 * @param sourcePath
	 * @param context
	 * @param allSources
	 * @param sourceString
	 * @return
	 */
	private boolean checksSource(String sourcePath, String context, Set<String> allSources, String sourceString) {
		if (sourcePath.toLowerCase().contains(context.toLowerCase())) {
			if (allSources.stream().anyMatch(path -> path.equalsIgnoreCase(sourceString))) {
				return true;
			}
			if (allSources.stream().noneMatch(path -> path.equalsIgnoreCase(sourceString))) {
				throw new IllegalStateException(String.format(
						"'%s' is an unknown referenced source. Cannot build dependency of sources.", sourcePath));
			}
		}
		return false;
	}

	/**
	 * Convert Map<String, Set<String>> to List<Set<String>> Example : {
	 * 1=[source(3), source(6), source(1), source(2), source(9)], 
	 * 2=[source(4), source(5)], 
	 * 3=[source(10), source(7)], 
	 * 4=[source(11), source(8)],
	 * 5=[source(12)] } 
	 * to 
	 * [[source(3), source(6), source(1), source(2), source(9)],
	 * [source(4), source(5)], [source(10), source(7)], [source(11), source(8)],
	 * [source(12)]]
	 * 
	 * @param level
	 * @return
	 */
	private List<Set<String>> getSourceDependencies(Map<Integer, Set<String>> level) {
		List<Set<String>> sourceDep = new ArrayList<>();
		for (Set<String> values : level.values()) {
			sourceDep.add(values.stream().map(this::getSourceString).collect(Collectors.toSet()));
		}

		return sourceDep;
	}

	/**
	 * Get the source id from the source path
	 * example :  $monitors.enclosure.multiCollect.sources.source(1) -> source(1)
	 * @param str
	 * @return
	 */
	private String getSourceString(String str) {
		return str.substring(str.lastIndexOf(".") + 1);
	}

}
