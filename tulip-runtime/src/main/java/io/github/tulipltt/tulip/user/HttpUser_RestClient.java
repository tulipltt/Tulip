package io.github.tulipltt.tulip.user;

import static io.github.tulipltt.tulip.core.TulipKt.gMaxNumUsers;

import io.github.tulipltt.tulip.api.*;
import io.github.tulipltt.tulip.api.TulipUser;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

record HttpRecord(
        RestClient restClient,
        HttpClient httpClient,
        String url,
        String urlProtocol,
        String urlHost,
        int urlPort,
        String urlPath,
        String urlQuery) {}

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

        var shareConnections_ = getUserParamValue("shareConnections");
        if (!shareConnections_.isEmpty()) {
            if (!shareConnections_.equalsIgnoreCase("true") && !shareConnections_.equalsIgnoreCase("false")) {
                logger().warn("Unrecognized shareConnections value '{}', defaulting to false", shareConnections_);
            }
            shareConnections = Boolean.parseBoolean(shareConnections_);
        }

        if (url_.isEmpty()) {
            logger().error("\"url\" property is empty");
            return false;
        }

        int N;
        String[] urls = url_.split(",");
        if (shareConnections) {
            https = new ArrayList<>(urls.length);
            N = 1;
        } else {
            https = new ArrayList<>(gMaxNumUsers);
            N = gMaxNumUsers;
            if (urls.length > 1) {
                logger().warn(
                                "Multiple URLs are specified, but shareConnections is false. Only the first URL will be used.");
                urls = Arrays.copyOf(urls, 1);
            }
        }
        int n = 0;
        int idx = 0;
        while (n < N) {
            for (String url : urls) {
                HttpRecord record =
                        createRestClient(
                                idx, url.trim(), connectTimeout_, readTimeout_, httpVersion_);
                if (record == null) {
                    return false;
                }
                https.add(record);
                idx += 1;
            }
            n += 1;
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
    HttpRecord createRestClient(
            int idx,
            String url_,
            String connectTimeout_,
            String readTimeout_,
            String httpVersion_) {

        RestClient restClient = null;

        String urlProtocol;
        String urlHost;
        int urlPort;
        String urlPath;
        String urlQuery;
        try {
            URL url = new URI(url_).toURL();
            urlProtocol = url.getProtocol();
            urlHost = url.getHost();
            urlPort = url.getPort();
            urlPath = url.getPath();
            urlQuery = url.getQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String baseUrl = urlProtocol + "://" + urlHost;
        if (urlPort != -1) {
            baseUrl += ":" + urlPort;
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
        return new HttpRecord(
                restClient,
                httpClient,
                url_,
                urlProtocol,
                urlHost,
                urlPort,
                urlPath == null ? "" : urlPath,
                urlQuery == null ? "" : urlQuery);
    }

    /**
     * onStop() method
     *
     * @return boolean
     */
    public boolean onStop() {
        for (HttpRecord hr : https) {
            if (hr.httpClient() != null) {
                hr.httpClient().close();
            }
        }
        https.clear();
        return true;
    }

    /**
     * restClient() method
     *
     * @return RestClient
     */
    public RestClient restClient() {
        if (shareConnections) {
            return https.get(getUserId() % https.size()).restClient();
        } else {
            throw new RuntimeException(
                    "RestClient is not shared, please create a new RestClient for each request.");
        }
    }

    // RestClient objects
    private static ArrayList<HttpRecord> https = null;

    /**
     * getUrlProtocol() method
     *
     * @return String
     */
    public String getUrlProtocol() {
        return https.get(getUserId() % https.size()).urlProtocol();
    }

    /**
     * getUrlHost() method
     *
     * @return String
     */
    public String getUrlHost() {
        return https.get(getUserId() % https.size()).urlHost();
    }

    /**
     * getUrlPort() method
     *
     * @return int
     */
    public int getUrlPort() {
        return https.get(getUserId() % https.size()).urlPort();
    }

    /**
     * getUrlPath() method
     *
     * @return String
     */
    public String getUrlPath() {
        return https.get(getUserId() % https.size()).urlPath();
    }

    /**
     * getUrlQuery() method
     *
     * @return String
     */
    public String getUrlQuery() {
        return https.get(getUserId() % https.size()).urlQuery();
    }

    /** HTTP header key name */
    public static String http_header_key = "";

    /** HTTP header key value */
    public static String http_header_val = "";

    /**
     * Whether to share connections If false, then each user will be allocated its own restClient
     * object. This is required when using httpOnly cookies to user JWTs that contain the userId.
     */
    public static boolean shareConnections = true;
}
