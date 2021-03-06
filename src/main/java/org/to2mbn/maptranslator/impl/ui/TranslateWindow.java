package org.to2mbn.maptranslator.impl.ui;

import static org.to2mbn.maptranslator.impl.ui.ProgressWindow.progressWindow;
import static org.to2mbn.maptranslator.impl.ui.UIUtils.reportException;
import static org.to2mbn.maptranslator.impl.ui.UIUtils.translate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.to2mbn.maptranslator.impl.json.parse.JSONObject;
import org.to2mbn.maptranslator.impl.json.parse.JSONTokener;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

class TranslateWindow {

	public static class TranslateEntry {

		String origin;
		SimpleStringProperty originProperty = new SimpleStringProperty();
		SimpleStringProperty targetProperty = new SimpleStringProperty("");

	}

	public Stage stage;
	private TableView<TranslateEntry> table;
	private ObservableList<TranslateEntry> entries = FXCollections.observableArrayList();
	private Button btnImport;
	private Button btnExport;
	private Button btnApply;
	private TableColumn<TranslateEntry, String> colOrigin;
	private TableColumn<TranslateEntry, String> colTarget;
	private volatile Map<String, String> lastStoredData = Collections.emptyMap();

	public Consumer<String> onTextDbclick;
	public Consumer<String> onAdded;
	public Consumer<String> onRemoved;
	public Consumer<Map<String, String>> applier;

	public TranslateWindow() {
		stage = new Stage();
		stage.setTitle(translate("translate.title"));
		btnImport = new Button(translate("translate.import"));
		btnExport = new Button(translate("translate.export"));
		btnApply = new Button(translate("translate.apply"));
		table = new TableView<>(entries);
		BorderPane rootPane = new BorderPane();
		rootPane.setCenter(table);
		rootPane.setBottom(new FlowPane(btnImport, btnExport, btnApply));
		stage.setScene(new Scene(rootPane));

		colOrigin = new TableColumn<>(translate("tranalate.origin"));
		colOrigin.setEditable(false);
		colOrigin.setCellValueFactory(entry -> entry.getValue().originProperty);
		colOrigin.setCellFactory(param -> {
			@SuppressWarnings("unchecked")
			TableCell<TranslateEntry, String> cell = (TableCell<TranslateEntry, String>) TableColumn.DEFAULT_CELL_FACTORY.call(param);
			cell.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2) {
					TranslateEntry selected = table.getSelectionModel().getSelectedItem();
					if (selected != null)
						onTextDbclick.accept(selected.origin);
				}
			});
			return cell;
		});

		colTarget = new TableColumn<>(translate("tranalate.translated"));
		colTarget.setCellValueFactory(entry -> entry.getValue().targetProperty);
		colTarget.setCellFactory(param -> new TextFieldTableCell<TranslateEntry, String>(new DefaultStringConverter()));
		table.getColumns().add(colOrigin);
		table.getColumns().add(colTarget);
		table.setEditable(true);

		stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), () -> {
			if (!table.getSelectionModel().isEmpty()) {
				String origin = table.getSelectionModel().getSelectedItem().origin;
				entries.remove(table.getSelectionModel().getSelectedIndex());
				onRemoved.accept(origin);
			}
		});
		stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), () -> {
			TranslateEntry entry = table.getSelectionModel().getSelectedItem();
			if (entry != null) {
				TranslateEditWindow.show(entry);
			}
		});

		btnApply.setOnAction(event -> applier.accept(toTranslateTable()));
		btnExport.setOnAction(event -> exportData(toTranslateTable()));
		btnImport.setOnAction(event -> importData());
	}

	private Map<String, String> toTranslateTable() {
		Map<String, String> result = new LinkedHashMap<>();
		entries.forEach(entry -> result.put(entry.origin, entry.targetProperty.get()));
		return result;
	}

	public void tryAddEntry(String origin) {
		for (TranslateEntry entry : entries) {
			if (entry.origin.equals(origin)) {
				table.getSelectionModel().select(entry);
				table.scrollTo(entry);
				return;
			}
		}

		TranslateEntry entry = new TranslateEntry();
		entry.origin = origin;
		entry.originProperty.set(origin);
		entry.targetProperty.set(origin);
		entries.add(entry);
		onAdded.accept(origin);
		stage.requestFocus();
		table.requestFocus();
		table.getSelectionModel().select(entry);
		table.scrollTo(table.getSelectionModel().getSelectedIndex());
		TranslateEditWindow.show(entry);
	}

	private void importEntry(String origin, String target) {
		for (TranslateEntry entry : entries) {
			if (entry.origin.equals(origin)) {
				entry.targetProperty.set(target);
				return;
			}
		}

		TranslateEntry entry = new TranslateEntry();
		entry.origin = origin;
		entry.originProperty.set(origin);
		entry.targetProperty.set(target);
		entries.add(entry);
		onAdded.accept(origin);
	}

	public boolean isStringTranslated(String str) {
		for (TranslateEntry entry : entries) {
			if (entry.origin.equals(str))
				return true;
		}
		return false;
	}

	public boolean warnExit(){
		return !entries.isEmpty() && !toTranslateTable().equals(lastStoredData);
	}

	private void exportData(Map<String, String> data) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(translate("translate.export"));
		chooser.setSelectedExtensionFilter(new ExtensionFilter("*.json", "*.json"));
		File target = chooser.showSaveDialog(stage);
		if (target == null) return;
		progressWindow().show(false);
		CompletableFuture
				.runAsync(() -> {
					JSONObject json = new JSONObject(data);
					try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")) {
						writer.write(json.toString());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.handleAsync((result, err) -> {
					progressWindow().hide();
					if (err == null) {
						lastStoredData = new HashMap<>(data);
					} else {
						reportException(err);
					}
					return null;
				}, Platform::runLater);
	}

	private void importData() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(translate("translate.import"));
		chooser.setSelectedExtensionFilter(new ExtensionFilter("*.json", "*.json"));
		File target = chooser.showOpenDialog(stage);
		if (target == null) return;
		progressWindow().show(false);
		CompletableFuture
				.supplyAsync(() -> {
					try (Reader reader = new InputStreamReader(new FileInputStream(target), "UTF-8")) {
						return new JSONObject(new JSONTokener(reader));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.handleAsync((result, err) -> {
					progressWindow().hide();
					if (err == null) {

						Map<String, String> mapping = new LinkedHashMap<>();
						for (String key : result.keySet())
							mapping.put(key, result.getString(key));
						mapping.forEach((k, v) -> importEntry(k, v));
						if (toTranslateTable().equals(mapping))
							lastStoredData = new HashMap<>(mapping);

					} else {
						reportException(err);
					}
					return null;
				}, Platform::runLater)
				.exceptionally(reportException);
	}

}
