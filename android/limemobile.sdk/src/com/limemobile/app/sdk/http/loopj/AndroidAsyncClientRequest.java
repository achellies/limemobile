package com.limemobile.app.sdk.http.loopj;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import com.limemobile.app.sdk.http.BasicJSONResponse;
import com.limemobile.app.sdk.http.BasicRequest;
import com.limemobile.app.sdk.http.JSONResponseListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public abstract class AndroidAsyncClientRequest extends BasicRequest {
	protected JsonHttpResponseHandler mResponseHandler;

	public AndroidAsyncClientRequest(String domain, String host, String path,
			JSONResponseListener listener) {
		super(domain, host, path, listener);

		mResponseHandler = new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject json) {
				// super.onSuccess(statusCode, headers, json);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.parseResponse(json);
					mListener.onResponse(response);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONArray jsonArray) {
				// super.onSuccess(statusCode, headers, jsonArray);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.setErrorCode(BasicJSONResponse.FAILED);
					response.setErrorMessage(jsonArray.toString());
					mListener.onResponse(response);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// super.onFailure(statusCode, headers, throwable,
				// errorResponse);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.setErrorCode(BasicJSONResponse.FAILED);
					response.setErrorMessage(throwable.toString());
					mListener.onResponse(response);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				// super.onFailure(statusCode, headers, throwable,
				// errorResponse);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.setErrorCode(BasicJSONResponse.FAILED);
					response.setErrorMessage(throwable.toString());
					mListener.onResponse(response);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// super.onFailure(statusCode, headers, responseString,
				// throwable);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.setErrorCode(BasicJSONResponse.FAILED);
					response.setErrorMessage(throwable.toString());
					mListener.onResponse(response);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String responseString) {
				// super.onSuccess(statusCode, headers, responseString);
				if (mListener != null) {
					BasicJSONResponse response = parseJSONResponse(statusCode,
							headers);
					response.setErrorCode(BasicJSONResponse.FAILED);
					response.setErrorMessage(responseString);
					mListener.onResponse(response);
				}
			}

		};
	}

	public ResponseHandlerInterface getResponseHanlder() {
		return mResponseHandler;
	}

	protected BasicJSONResponse parseJSONResponse(int statusCode,
			Header[] headers) {
		BasicJSONResponse response = new BasicJSONResponse(statusCode, headers);
		return response;
	}

	public abstract RequestParams getRequestParams();
}
