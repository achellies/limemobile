package com.limemobile.app.sdk.orm.internal;

import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.limemobile.app.sdk.http.BasicJSONResponse;
import com.limemobile.app.sdk.http.HttpUtils;
import com.limemobile.app.sdk.http.JSONResponseListener;
import com.limemobile.app.sdk.http.loopj.AndroidAsyncClientRequest;
import com.limemobile.app.sdk.http.loopj.AndroidAsyncHttpClient;
import com.limemobile.app.sdk.orm.ModelProviderListener;
import com.limemobile.app.sdk.orm.LoopjModelProvider;
import com.limemobile.app.sdk.orm.gson.GsonModel;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

@SuppressWarnings("serial")
public class UpdateJob<T> extends Job {
    protected final Context mContext;
    protected final Handler mHandler;

    protected final AbstractDao<T, Long> mDao;
    protected final QueryBuilder<T> mQueryBuilder;

    protected final AndroidAsyncHttpClient mHttpClient;
    protected final AndroidAsyncClientRequest mApi;

    protected final GsonModel<T> mGsonModel;

    public UpdateJob(Context context, int priorty, Handler handler,
            AndroidAsyncHttpClient httpClient, AndroidAsyncClientRequest api,
            GsonModel<T> gson, AbstractDao<T, Long> dao, int offset, int limit,
            WhereCondition cond, WhereCondition... condMore) {
        // This job requires network connectivity,
        // and should not be persisted in case the application exits before job
        // is
        // completed.
        super(new Params(priorty).requireNetwork().setPersistent(false));
        mContext = context;

        mHandler = handler;

        mHttpClient = httpClient;
        mApi = api;

        mDao = dao;
        mQueryBuilder = mDao.queryBuilder();
        mQueryBuilder.offset(offset);
        mQueryBuilder.limit(limit);
        mQueryBuilder.where(cond, condMore);

        mGsonModel = gson;
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will
        // eventually run.
        mHandler.obtainMessage(LoopjModelProvider.UPDATE_START_MESSAGE)
                .sendToTarget();
    }

    @Override
    public void onRun() throws Throwable {
        // Job logic goes here.
        if (!HttpUtils.isNetworkAvaliable(mContext)) {
            mHandler.obtainMessage(
                    LoopjModelProvider.UPDATE_FAILURE_MESSAGE,
                    new Object[] {
                            ModelProviderListener.RESULT_STATUS_NETOWRK_NOT_AVALIABLE,
                            ModelProviderListener.FROM_SERVER, null, null })
                    .sendToTarget();
            mHandler.obtainMessage(LoopjModelProvider.UPDATE_FINISH_MESSAGE)
                    .sendToTarget();
            return;
        }
        mApi.setResponseHandler(new JSONResponseListener() {

            @Override
            public void onResponse(BasicJSONResponse response) {
                if (BasicJSONResponse.SUCCESS == response.getErrorCode()) {
                    List<T> entities = mGsonModel.parseObjects(response
                            .getJSONObject());
                    mDao.deleteAll();
                    mDao.insertInTx(entities);
                    entities = mQueryBuilder.list();
                    mHandler.obtainMessage(
                            LoopjModelProvider.UPDATE_SUCCESS_MESSAGE,
                            new Object[] {
                                    ModelProviderListener.RESULT_STATUS_OK,
                                    ModelProviderListener.FROM_SERVER,
                                    entities, response }).sendToTarget();
                    mHandler.obtainMessage(LoopjModelProvider.UPDATE_FINISH_MESSAGE)
                            .sendToTarget();
                } else {
                    mHandler.obtainMessage(
                            LoopjModelProvider.UPDATE_FAILURE_MESSAGE,
                            new Object[] {
                                    ModelProviderListener.RESULT_STATUS_API_ERROR,
                                    ModelProviderListener.FROM_SERVER, null,
                                    response }).sendToTarget();
                    mHandler.obtainMessage(LoopjModelProvider.UPDATE_FINISH_MESSAGE)
                            .sendToTarget();
                }
            }

        });
        mHttpClient.get(mContext, mApi);
    }

    @Override
    protected void onCancel() {
        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true)
        // or abort (false).
        mHandler.obtainMessage(
                LoopjModelProvider.UPDATE_CANCEL_MESSAGE,
                new Object[] { ModelProviderListener.RESULT_STATUS_CANCEL,
                        ModelProviderListener.FROM_SERVER, null, null })
                .sendToTarget();
        mHandler.obtainMessage(LoopjModelProvider.UPDATE_FINISH_MESSAGE)
                .sendToTarget();
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has
        // returned false.
        return false;
    }

}
