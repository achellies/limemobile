package com.limemobile.app.sdk.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class BasicJSONResponse {
	public static final int SUCCESS = 0;
	public static final int FAILED = 1;

	protected int mStatusCode;
	protected final Map<String, String> mHeaders;
	protected int mErrorCode;
	protected String mErrorMessage;
	protected JSONObject mJSONObject;

	public BasicJSONResponse(int statusCode, Header[] headers) {
		super();

		mStatusCode = statusCode;
		if (mStatusCode == HttpStatus.SC_OK) {
			mErrorCode = SUCCESS;
		} else {
			mErrorCode = FAILED;
		}
		mHeaders = convertHeaders(headers);
	}

	public BasicJSONResponse(int statusCode, Map<String, String> headers) {
		super();

		mStatusCode = statusCode;
		if (mStatusCode == HttpStatus.SC_OK) {
			mErrorCode = SUCCESS;
		} else {
			mErrorCode = FAILED;
		}
		mHeaders = headers;
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public String getErrorMessage() {
		return mErrorMessage;
	}

	public void setErrorMessage(String msg) {
		mErrorMessage = msg;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public void setErrorCode(int code) {
		mErrorCode = code;
	}

	public JSONObject getJSONObject() {
		return mJSONObject;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void parseResponse(JSONObject json) {
		this.mJSONObject = json;
		try {
			this.parseJSON();
			mErrorCode = SUCCESS;
		} catch (JSONException e) {
			mErrorCode = FAILED;
			if (e != null) {
				mErrorMessage = e.toString();
			}
		}
	}

	protected void parseJSON() throws JSONException {
		// do nothing
	}

	/**
	 * Converts Headers[] to Map<String, String>.
	 */
	private static Map<String, String> convertHeaders(Header[] headers) {
		Map<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < headers.length; i++) {
			result.put(headers[i].getName(), headers[i].getValue());
		}
		return result;
	}
}
