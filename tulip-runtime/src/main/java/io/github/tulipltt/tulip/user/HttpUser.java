package io.github.tulipltt.tulip.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/** The HttpUser class. */
public class HttpUser extends HttpUser_RestClient {

    /** Default constructor for HttpUser */
    public HttpUser() {}

    /**
     * initConfig() method
     *
     * @param config - test config
     */
    public void initConfig(HashMap<String, String> config) {
        this.config = config;
    }

    /**
     * Response record
     *
     * @param statusCode - HTTP status code
     * @param headers - HTTP response headers
     * @param body - response body
     */
    public record Response(
            int statusCode, org.springframework.http.HttpHeaders headers, String body) {

        /**
         * isSuccessful() method
         *
         * @return True if status code is 2xx
         */
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    /**
     * httpGet() method
     *
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpGet(String uri, Object... uriVariables) throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .get()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpPost() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpPost(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .post()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpPut() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpPut(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .put()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpPatch() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpPatch(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .patch()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpDelete() method
     *
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpDelete(String uri, Object... uriVariables) throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .delete()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpQuery() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpQuery(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .method(HttpMethod.valueOf("QUERY"))
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * httpHead() method
     *
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public Response httpHead(String uri, Object... uriVariables) throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .head()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getHeaders(), entity.getBody());
    }

    /**
     * getUserParamValue() method
     *
     * @param paramName - key to return value for from config
     */
    @NotNull
    public String getUserParamValue(@NotNull String paramName) {
        if (config == null) {
            return super.getUserParamValue(paramName);
        }
        String value = config.get(paramName);
        if (value == null) {
            return "";
        }
        return value;
    }

    private HashMap<String, String> config = null;
}
