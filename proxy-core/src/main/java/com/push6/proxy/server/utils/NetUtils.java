package com.push6.proxy.server.utils;

import com.push6.proxy.server.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by serv on 2014/10/27.
 */
public final class NetUtils {

    //Increase max total connection
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;

    //Increase default max connection per route
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;

    private static final CloseableHttpClient client ;

    static {

        // http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0
        System.setProperty("jsse.enableSNIExtension", "false");

        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(Constants.REQUEST_RESTFUL_TIMEOUT * 1000)//超时 5秒
                .setConnectionRequestTimeout(Constants.REQUEST_RESTFUL_TIMEOUT * 1000)//
                .setConnectTimeout(Constants.REQUEST_RESTFUL_TIMEOUT * 1000)//超时 5秒
                .build();

        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setMaxConnTotal(DEFAULT_MAX_TOTAL_CONNECTIONS)
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .setMaxConnPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE)
                .disableAutomaticRetries()
                .build();
    }

    public static HttpClient getHttpClient(){
        return client;
    }

    public static String get(String url) throws URISyntaxException, IOException {
        HttpResponse response = client.execute(new HttpGet(url));
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public static String post(String url,HttpEntity httpEntity,HttpClient httpClient) throws IOException {
        if(httpClient==null){
            httpClient = client;
        }
        HttpPost post = new HttpPost(url);
        post.setEntity(httpEntity);
        return EntityUtils.toString(httpClient.execute(post).getEntity(), "UTF-8");
    }

    public static String post(String url,HttpEntity httpEntity) throws IOException {
        return post(url, httpEntity, null);
    }

    public static String post(String url,String data,String contentType,HttpClient httpClient) throws IOException {
        return post(url,data.getBytes("UTF-8"),contentType,httpClient);
    }


    public static String post(String url,byte[] data,String contentType,HttpClient httpClient) throws IOException {
        ByteArrayEntity se = new ByteArrayEntity(data);
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, contentType));
        return post(url, se, httpClient);
    }
    public static String post(String url,String data,String contentType) throws IOException {
        return post(url, data.getBytes("UTF-8"), contentType);
    }

    public static String post(String url,byte[] data,String contentType) throws IOException {
        return post(url, data, contentType, null);
    }

}
