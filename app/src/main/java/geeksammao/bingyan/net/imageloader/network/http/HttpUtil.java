package geeksammao.bingyan.net.imageloader.network.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import geeksammao.bingyan.net.imageloader.network.result.RequestResult;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class HttpUtil {
    public static final int HTTP_OK = 200;
    public static final int HTTP_ERROR = 404;
    public static final int HTTP_CLIENT_TIMEOUT = 403;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_PARTIAL = 206;

    private static HttpUtil httpUtil = new HttpUtil();

    public static HttpUtil getInstance() {
        return httpUtil;
    }

    private HttpUtil() {
    }

    public RequestResult<String> getString(String targetUrl) {
        HttpURLConnection urlConnection = null;
        RequestResult<String> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            setUrlConnection(urlConnection);
            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpsURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(inputStreamToString(urlConnection.getInputStream()));
                    break;
                case HttpsURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_CLIENT_TIMEOUT);
                    requestResult.setData(null);
                    break;
                case HttpsURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_GATEWAY_TIMEOUT);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            requestResult.setStatus(HTTP_ERROR);
            requestResult.setData(null);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return requestResult;
    }

    public RequestResult<Integer> getContentLength(String targetUrl) {
        HttpURLConnection urlConnection = null;
        RequestResult<Integer> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            setUrlConnection(urlConnection);
            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(urlConnection.getContentLength());
                    break;
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_ERROR);
                    requestResult.setData(null);
                    break;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_ERROR);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            requestResult.setStatus(HTTP_ERROR);
            requestResult.setData(null);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return requestResult;
    }

    public final RequestResult<InputStream> getInputStream(String targetUrl) {
        HttpURLConnection urlConnection = null;
        RequestResult<InputStream> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            setUrlConnection(urlConnection);
            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpsURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(urlConnection.getInputStream());
                    break;
                case HttpsURLConnection.HTTP_PARTIAL:
                    requestResult.setStatus(HTTP_PARTIAL);
                    requestResult.setData(urlConnection.getInputStream());
                    break;
                case HttpsURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_CLIENT_TIMEOUT);
                    requestResult.setData(null);
                    break;
                case HttpsURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_GATEWAY_TIMEOUT);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            requestResult.setData(null);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return requestResult;
    }


    private void setUrlConnection(HttpURLConnection urlConnection) throws ProtocolException {
        urlConnection.setConnectTimeout(5 * 1000);
        urlConnection.setReadTimeout(5 * 1000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "image/gif,image/jpeg," +
                "image/pjpeg,application/x-shockwave-flash,application/xaml+xml," +
                "application/vnd.ms-xpsdocument,application/x-ms-xbap," +
                "application/x-ms-application,application/vnd.ms-excel," +
                "application/vnd.ms-powerpoint,application/msword,*/*");
        urlConnection.setRequestProperty("Charset", "UTF-8");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0(" +
                "compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; " +
                ".NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; " +
                ".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        urlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
        urlConnection.setRequestProperty("Connection", "Keep-alive");
        urlConnection.setDefaultUseCaches(true);
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
