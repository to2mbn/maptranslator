package org.to2mbn.maptranslator.impl.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.to2mbn.maptranslator.tree.Node;

public interface MapHandler {

	public static CompletableFuture<MapHandler> create(Path mapFolder) {
		return MapHandlerImpl.create(mapFolder);
	}

	List<String> excludes();

	CompletableFuture<Map<String, List<String[]>>> extractStrings();

	List<ParsingWarning> lastParsingWarnings();

	Set<BiConsumer<Node, Map<String, Object>>> parsingWarningMetadataProviders();

	CompletableFuture<Void> replace(Map<String, String> table);

	CompletableFuture<Optional<Node>> resolveNode(String[] path);

	CompletableFuture<Optional<Node>> resolveNode(String path);

	CompletableFuture<Void> saveNode(Node node);

	CompletableFuture<Void> close();

	long currentProgress();

	long totalProgress();

}
