package org.to2mbn.maptranslator.tree;

import org.to2mbn.maptranslator.internal.org.json.JSONArray;
import org.to2mbn.maptranslator.internal.org.json.JSONObject;

public class JsonListNode extends JsonNode implements ListNode {

	public final int index;

	public JsonListNode(Object json, int index) {
		super(json);
		this.index = index;
	}

	@Override
	public String toString() {
		return "[" + index + "]";
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public void replaceJson(Object json) {
		super.replaceJson(json);
		((JSONArray) ((JsonNode) parent()).json).put(index, json);
	}

	@Override
	public String getDisplayText() {
		if (json instanceof JSONObject || json instanceof JSONArray) {
			return toString();
		} else {
			return "[" + index + "] = " + json;
		}
	}

}