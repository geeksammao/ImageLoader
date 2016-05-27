package geeksammao.bingyan.net.imageloader.core.task;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by sammao on 5/11/16.
 */
public class HttpFutureTask extends FutureTask {
    public HttpFutureTask(Callable callable) {
        super(callable);
    }
}
