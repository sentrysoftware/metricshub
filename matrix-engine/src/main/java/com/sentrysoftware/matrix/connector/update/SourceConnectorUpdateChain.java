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

public abstract class SourceConnectorUpdateChain extends AbstractConnectorUpdateChain {

	private static final Map<Integer, Set<String>> sourceLevels = new HashMap<>();
	private static final Map<String, Set<String>> sourceDependencies = new HashMap<>();
	
	/**
	 * Get the correct order of the sources execution Check for each source if its
	 * references have been already executed
	 * 
	 * @param sources
	 * @return
	 */
	protected List<Set<String>> updateSourceDependency(final Map<String, Source> sources) {
		// initialize
		sourceLevels.clear();
		sourceDependencies.clear();
		
		Set<String> done = new HashSet<>();
		Set<String> pending = new HashSet<>();

		// process sources
		for (String sourceId : sources.keySet()) {
			Source currentSource = sources.get(sourceId);
			Set<String> dependencies = getReferences(currentSource);
			sourceDependencies.put(sourceId, dependencies);
			if (dependencies.isEmpty()) {
				done.add(sourceId);
			} else {
				pending.add(sourceId);
			}
		}
		// check that all pending references are actually sources
		// TODO
//		sources.keySet().containsAll(pending);
//		pending.removeAll(sources.keySet()); // doesn't work
		
		
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
	 * get the dependency list of each source
	 * @param source
	 * @return
	 */
	private Set<String> getReferences(Source source) {
		Set<String> ref = new HashSet<>();
		if (source instanceof CopySource src) {
			ref.add(getSourceString(src.getFrom()));
		}

		if (source instanceof TableUnionSource src) {
			ref.addAll(src.getTables().stream().map(this::getSourceString).collect(Collectors.toSet()));
		}

		if (source instanceof TableJoinSource src) {
			ref.add(getSourceString(src.getLeftTable()));
			ref.add(getSourceString(src.getRightTable()));
		}

		return ref;
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

	private String getSourceString(String str) {
		return str.substring(str.lastIndexOf(".") + 1);
	}

}
