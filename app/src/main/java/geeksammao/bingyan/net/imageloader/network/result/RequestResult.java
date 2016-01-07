package geeksammao.bingyan.net.imageloader.network.result;

import android.os.Bundle;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class RequestResult<T> {
    private int status;
    private T data;
    private Bundle multiData;

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMultiData(Bundle multiData) {
        this.multiData = multiData;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public Bundle getMultiData() {
        return multiData;
    }
}
