package com.limemobile.app.sdk.orm;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.limemobile.app.sdk.http.BasicJSONResponse;
import com.limemobile.app.sdk.http.loopj.AndroidAsyncClientRequest;
import com.limemobile.app.sdk.http.loopj.AndroidAsyncHttpClient;
import com.limemobile.app.sdk.orm.gson.GsonModel;
import com.limemobile.app.sdk.orm.internal.QueryJob;
import com.limemobile.app.sdk.orm.internal.UpdateJob;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.query.WhereCondition;

public class LoopjModelProvider<T> {
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_HIGH = 3;

    public static final int QUERY_SUCCESS_MESSAGE = 0;
    public static final int QUERY_FAILURE_MESSAGE = 1;
    public static final int QUERY_START_MESSAGE = 2;
    public static final int QUERY_FINISH_MESSAGE = 3;
    public static final int QUERY_CANCEL_MESSAGE = 6;
    public static final int UPDATE_SUCCESS_MESSAGE = 7;
    public static final int UPDATE_FAILURE_MESSAGE = 8;
    public static final int UPDATE_START_MESSAGE = 9;
    public static final int UPDATE_FINISH_MESSAGE = 10;
    public static final int UPDATE_CANCEL_MESSAGE = 12;

    private static JobManager sJobManager;

    protected final Context mContext;

    protected final SQLiteDatabase mDatabase;
    protected final AbstractDaoMaster mDaoMaster;
    protected final AbstractDaoSession mDaoSession;
    protected final AbstractDao<T, Long> mDao;

    protected final AndroidAsyncHttpClient mHttpClient;

    protected final Handler mHandler;

    protected final GsonModel<T> mGsonModel;

    protected final ModelProviderListener<T> mListener;

    @SuppressWarnings("unchecked")
    public LoopjModelProvider(Context context, SQLiteDatabase db,
            AbstractDaoMaster daoMaster, Class<T> clazz, Looper looper,
            GsonModel<T> gson, ModelProviderListener<T> listener) {
        super();
        mContext = context;

        mDatabase = db;
        mDaoMaster = daoMaster;
        mDaoSession = mDaoMaster.newSession();
        mDao = (AbstractDao<T, Long>) mDaoSession.getDao(clazz);

        mHttpClient = new AndroidAsyncHttpClient(context, true);

        Looper tempLooper = looper == null ? Looper.myLooper() : looper;
        mHandler = new ResponderHandler<T>(this, tempLooper);

        mGsonModel = gson;

        mListener = listener;
    }

    /*
     * QueryBuilder qb = userDao.queryBuilder();
     * qb.where(Properties.FirstName.eq("Joe"),
     * qb.or(Properties.YearOfBirth.gt(1970),
     * qb.and(Properties.YearOfBirth.eq(1970),
     * Properties.MonthOfBirth.ge(10)))); List youngJoes = qb.list();
     * 
     * 
     * Select * from xxx where xxxx Order by xxxx offset xxx limit xxx
     */
    public void query(Context context, AndroidAsyncClientRequest api,
            int offset, int limit, WhereCondition cond,
            WhereCondition... condMore) {
        sJobManager.addJob(new QueryJob<T>(mContext, PRIORITY_NORMAL, mHandler,
                mHttpClient, api, mGsonModel, mDao, offset, limit, cond,
                condMore));
    }

    public void update(Context context, AndroidAsyncClientRequest api,
            int offset, int limit, WhereCondition cond,
            WhereCondition... condMore) {
        sJobManager.addJob(new UpdateJob<T>(mContext, PRIORITY_NORMAL,
                mHandler, mHttpClient, api, mGsonModel, mDao, offset, limit,
                cond, condMore));
    }

    public AbstractDao<T, Long> getDao() {
        return mDao;
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
        Object[] response;

        if (mListener != null) {
            switch (msg.what) {
            case QUERY_START_MESSAGE:
                mListener.onQueryStart();
                break;
            case QUERY_FINISH_MESSAGE:
                // do noting
                break;
            case QUERY_SUCCESS_MESSAGE:
            case QUERY_FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 3) {

                    mListener.onQueryFinish((Integer) response[0],
                            (Integer) response[1], (List<T>) response[2],
                            (BasicJSONResponse) response[3]);

                }
                break;
            case UPDATE_START_MESSAGE:
                mListener.onUpdateStart();
                break;
            case UPDATE_FINISH_MESSAGE:
                // do noting
                break;
            case UPDATE_SUCCESS_MESSAGE:
            case UPDATE_FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 3) {

                    mListener.onUpdateFinish((Integer) response[0],
                            (Integer) response[1], (List<T>) response[2],
                            (BasicJSONResponse) response[3]);

                }
                break;
            case QUERY_CANCEL_MESSAGE:
            case UPDATE_CANCEL_MESSAGE:
                mListener.onCancel();
                break;
            }
        }
    }

    private static void configureJobManager(Context context) {
        Configuration configuration = new Configuration.Builder(context)
                .minConsumerCount(1)// always keep at least one consumer alive
                .maxConsumerCount(2)// up to 3 consumers at a time
                .loadFactor(3)// 3 jobs per consumer
                .consumerKeepAlive(120)// wait 2 minute
                .build();
        sJobManager = new JobManager(context, configuration);
    }

    public synchronized static JobManager getJobManager(Context context) {
        if (sJobManager == null) {
            configureJobManager(context);
        }
        return sJobManager;
    }

    /**
     * Avoid leaks by using a non-anonymous handler class.
     */
    private static class ResponderHandler<T> extends Handler {
        private final LoopjModelProvider<T> mProvider;

        public ResponderHandler(LoopjModelProvider<T> provider, Looper looper) {
            super(looper);
            mProvider = provider;
        }

        @Override
        public void handleMessage(Message msg) {
            mProvider.handleMessage(msg);
        }
    }
}
