package com.limemobile.app.sdk.orm;

import java.util.List;

import com.limemobile.app.sdk.http.BasicJSONResponse;

public interface ModelProviderListener<T> {
    /**
     * 数据从本地cache数据库中获取
     */
    public static final int FROM_DATABASE = 1;
    /**
     * 数据从server端获取
     */
    public static final int FROM_SERVER = 2;

    /**
     * 成功
     */
    public static final int RESULT_STATUS_OK = 0;
    /**
     * 取消
     */
    public static final int RESULT_STATUS_CANCEL = -10001;
    /**
     * 网络不可用
     */
    public static final int RESULT_STATUS_NETOWRK_NOT_AVALIABLE = -10002;
    /**
     * 数据库IO错误
     */
    public static final int RESULT_STATUS_DATABSE_IO_ERROR = -10003;
    /**
     * API错误
     */
    public static final int RESULT_STATUS_API_ERROR = -10004;

    public void onQueryStart();

    public void onUpdateStart();
    
    public void onCancel();

    /**
     * 
     * @param resultCode
     * @param from
     * @param datas
     *            当resultCode(RESULT_STATUS_OK)时有效
     * @param response
     *            当from(FROM_SERVER) 和 resultCode(RESULT_STATUS_OK |
     *            RESULT_STATUS_API_ERROR)时有效
     */
    public void onQueryFinish(int resultCode, int from, List<T> datas,
            BasicJSONResponse response);

    /**
     * 
     * @param resultCode
     * @param from
     * @param datas
     *            当resultCode(RESULT_STATUS_OK)时有效
     * @param response
     *            当from(FROM_SERVER) 和 resultCode(RESULT_STATUS_OK |
     *            RESULT_STATUS_API_ERROR)时有效
     */
    public void onUpdateFinish(int resultCode, int from, List<T> datas,
            BasicJSONResponse response);
}
