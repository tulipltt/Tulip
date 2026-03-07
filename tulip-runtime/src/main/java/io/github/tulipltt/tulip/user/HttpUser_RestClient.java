package io.github.tulipltt.tulip.user;

import io.github.tulipltt.tulip.api.*;
import io.github.tulipltt.tulip.api.TulipUser;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class HttpRecord {
    public RestClient restClient = null;
    public String url = "";
    public String urlProtocol = "";
    public String urlHost = "";
    public int urlPort = -1;
    public String urlPath = "";
}

/** The HttpUser class. */
public class HttpUser_RestClient extends TulipUser {

    /** Default constructor for HttpUser_RestClient */
    public HttpUser_RestClient() {}

    /**
     * onStart() method
     *
     * @return boolean
     */
    public boolean onStart() {
        // Initialize the shared RestClient object only once
        if (getUserId() != 0) {
            return true;
        }

        var url_ = getUserParamValue("url");

        var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
        var readTimeout_ = getUserParamValue("readTimeoutMillis");
        var httpVersion_ = getUserParamValue("httpVersion").toUpperCase();

        if (url_.isEmpty()) {
            logger().error("\"url\" property is empty");
            return false;
        }

        String[] urls = url_.split(",");
        https = new HttpRecord[urls.length];
        int idx = 0;
        for (String url : urls) {
            https[idx] = new HttpRecord();
            https[idx].restClient =
                    createRestClient(idx, url.trim(), connectTimeout_, readTimeout_, httpVersion_);
            idx += 1;
        }

        var httpHeader_ = getUserParamValue("httpHeader");
        if (httpHeader_.isEmpty()) {
            httpHeader_ = "User-Agent: Tulip";
        }
        if (httpHeader_.contains(":")) {
            var L = httpHeader_.split(":");
            http_header_key = L[0].trim();
            http_header_val = L[1].trim();
        }

        return true;
    }

    /**
     * @param url_
     * @param connectTimeout_
     * @param readTimeout_
     * @param httpVersion_
     * @return
     */
    RestClient createRestClient(
            int idx,
            String url_,
            String connectTimeout_,
            String readTimeout_,
            String httpVersion_) {

        RestClient restClient = null;

        https[idx].url = url_;
        try {
            URL url = new URI(url_).toURL();
            https[idx].urlProtocol = url.getProtocol();
            https[idx].urlHost = url.getHost();
            https[idx].urlPort = url.getPort();
            https[idx].urlPath = url.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String baseUrl = https[idx].urlProtocol + "://" + https[idx].urlHost;
        if (https[idx].urlPort != -1) {
            baseUrl += ":" + https[idx].urlPort;
        }
        logger().info("[{}]baseUrl={}", idx, baseUrl);

        if (httpVersion_.isEmpty()) {
            httpVersion_ = "HTTP_1_1";
        }
        logger().info("[{}]httpVersion={}", idx, httpVersion_);

        // HTTP 1.1 or HTTP/2 or HTTP/3
        HttpClient httpClient = null;
        HttpClient.Version httpVersion;
        try {
            httpVersion = HttpClient.Version.valueOf(httpVersion_.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger().error("[{}]Unsupported HTTP version: {}", idx, httpVersion_);
            logger().error("[{}]Falling back to HTTP 1.1", idx);
            httpVersion = HttpClient.Version.HTTP_1_1;
        }
        if (!connectTimeout_.isEmpty()) {
            logger().info("[{}]connectTimeoutMillis={}", idx, connectTimeout_);
            httpClient =
                    HttpClient.newBuilder()
                            .version(httpVersion)
                            .connectTimeout(Duration.ofMillis(Integer.parseInt(connectTimeout_)))
                            .build();
        } else {
            httpClient = HttpClient.newBuilder().version(httpVersion).build();
        }
        var factory = new JdkClientHttpRequestFactory(httpClient);
        if (!readTimeout_.isEmpty()) {
            factory.setReadTimeout(Integer.parseInt(readTimeout_));
            logger().info("[{}]readTimeoutMillis={}", idx, readTimeout_);
        }
        restClient = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
        return restClient;
    }

    /**
     * onStop() method
     *
     * @return boolean
     */
    public boolean onStop() {
        return true;
    }

    /**
     * restClient() method
     *
     * @return RestClient
     */
    public RestClient restClient() {
        return https[getUserId() % https.length].restClient;
    }

    // RestClient objects
    private static HttpRecord[] https = null;

    /**
     * getUrlProtocol() method
     *
     * @return String
     */
    public String getUrlProtocol() {
        return https[getUserId() % https.length].urlProtocol;
    }

    /**
     * getUrlHost() method
     *
     * @return String
     */
    public String getUrlHost() {
        return https[getUserId() % https.length].urlHost;
    }

    /**
     * getUrlPort() method
     *
     * @return int
     */
    public int getUrlPort() {
        return https[getUserId() % https.length].urlPort;
    }

    /**
     * getUrlPath() method
     *
     * @return String
     */
    public String getUrlPath() {
        return https[getUserId() % https.length].urlPath;
    }

    /** HTTP header key name */
    public static String http_header_key = "";

    /** HTTP header key value */
    public static String http_header_val = "";
}
