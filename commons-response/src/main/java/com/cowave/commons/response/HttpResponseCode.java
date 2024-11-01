package com.cowave.commons.response;

/**
 *
 * @author shanhuiming
 *
 */
public enum HttpResponseCode implements ResponseCode {

    /* ******************************************
     * 1.xx Informational
     * ******************************************/

    /**
     * @see <a href="https://http.dev/100">100 Continue</a>
     */
    CONTINUE(100, "100", "Continue"),

    /**
     * @see <a href="https://http.dev/101">101 Switching Protocols</a>
     */
    SWITCHING_PROTOCOLS(101, "101", "Switching Protocols"),

    /**
     * @see <a href="https://http.dev/102">102 Processing</a>
     */
    PROCESSING(102, "102", "Processing"),

    /**
     * @see <a href="https://http.dev/103">103 Early Hints</a>
     */
    EARLY_HINTS(103, "103", "Early Hints"),

    /* ******************************************
     * 2.xx Success
     * ******************************************/

    /**
     * @see <a href="https://http.dev/200">200 Success</a>
     */
    SUCCESS(200, "200", "Success"),

    /**
     * @see <a href="https://http.dev/201">201 Created</a>
     */
    CREATED(201, "201", "Created"),

    /**
     * @see <a href="https://http.dev/202">202 Accepted</a>
     */
    ACCEPTED(202, "202", "Accepted"),

    /**
     * @see <a href="https://http.dev/203">203 Non-Authoritative Information</a>
     */
    NON_AUTHORITATIVE_INFORMATION(203, "203", "Non-Authoritative Information"),

    /**
     * @see <a href="https://http.dev/204">204 No Content</a>
     */
    NO_CONTENT(204, "204", "No Content"),

    /**
     * @see <a href="https://http.dev/205">205 Reset Content</a>
     */
    RESET_CONTENT(205, "205", "Reset Content"),

    /**
     * @see <a href="https://http.dev/206">206 Partial Content</a>
     */
    PARTIAL_CONTENT(206, "206", "Partial Content"),

    /**
     * @see <a href="https://http.dev/207">207 Multi-Status</a>
     */
    MULTI_STATUS(207, "207", "Multi-Status"),

    /**
     * @see <a href="https://http.dev/208">208 Already Reported</a>
     */
    ALREADY_REPORTED(208, "208", "Already Reported"),

    /**
     * @see <a href="https://http.dev/218">218 This Is Fine</a>
     */
    THIS_IS_FINE(218, "218", "This Is Fine"),

    /**
     * @see <a href="https://http.dev/226">226 IM Used</a>
     */
    IM_USED(226, "226", "IM Used"),

    /* ******************************************
     * 3.xx Redirection
     * ******************************************/

    /**
     * @see <a href="https://http.dev/300">300 Multiple Choices</a>
     */
    MULTIPLE_CHOICES(300, "300", "Multiple Choices"),

    /**
     * @see <a href="https://http.dev/301">301 Moved Permanently</a>
     */
    MOVED_PERMANENTLY(301, "301", "Moved Permanently"),

    /**
     * @see <a href="https://http.dev/302">302 Found</a>
     */
    FOUND(302, "302", "Found"),

    /**
     * @see <a href="https://http.dev/303">303 See Other</a>
     */
    SEE_OTHER(303, "303", "See Other"),

    /**
     * @see <a href="https://http.dev/304">304 Not Modified</a>
     */
    NOT_MODIFIED(304, "304", "Not Modified"),

    /**
     * @see <a href="https://http.dev/305">305 Use Proxy</a>
     */
    USE_PROXY(305, "305", "Use Proxy"),

    /**
     * @see <a href="https://http.dev/306">306 Switch Proxy</a>
     */
    SWITCH_PROXY(306, "306", "Switch Proxy"),

    /**
     * @see <a href="https://http.dev/307">307 Temporary Redirect</a>
     */
    TEMPORARY_REDIRECT(307, "307", "Temporary Redirect"),

    /**
     * @see <a href="https://http.dev/308">308 Permanent Redirect</a>
     */
    PERMANENT_REDIRECT(308, "308", "Permanent Redirect"),

    /* ******************************************
     * 4.xx Client Error
     * ******************************************/

    /**
     * @see <a href="https://http.dev/400">400 Bad Request</a>
     */
    BAD_REQUEST(400, "400", "Bad Request"),

    /**
     * @see <a href="https://http.dev/401">401 Unauthorized</a>
     */
    UNAUTHORIZED(401, "401", "Unauthorized"),

    /**
     * @see <a href="https://http.dev/402">402 Payment Required</a>
     */
    PAYMENT_REQUIRED(402, "402", "Payment Required"),

    /**
     * @see <a href="https://http.dev/403">403 Forbidden</a>
     */
    FORBIDDEN(403, "403", "Forbidden"),

    /**
     * @see <a href="https://http.dev/404">404 Not Found</a>
     */
    NOT_FOUND(404, "404", "Not Found"),

    /**
     * @see <a href="https://http.dev/405">405 Method Not Allowed</a>
     */
    METHOD_NOT_ALLOWED(405, "405", "Method Not Allowed"),

    /**
     * @see <a href="https://http.dev/406">406 Not Acceptable</a>
     */
    NOT_ACCEPTABLE(406, "406", "Not Acceptable"),

    /**
     * @see <a href="https://http.dev/407">407 Proxy Authentication Required</a>
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "407", "Proxy Authentication Required"),

    /**
     * @see <a href="https://http.dev/408">408 Request Timeout</a>
     */
    REQUEST_TIMEOUT(408, "408", "Request Timeout"),

    /**
     * @see <a href="https://http.dev/409">409 Conflict</a>
     */
    CONFLICT(409, "409", "Conflict"),

    /**
     * @see <a href="https://http.dev/410">410 Gone</a>
     */
    GONE(410, "410", "Gone"),

    /**
     * @see <a href="https://http.dev/411">411 Length Required</a>
     */
    LENGTH_REQUIRED(411, "411", "Length Required"),

    /**
     * @see <a href="https://http.dev/412">412 Precondition Failed</a>
     */
    PRECONDITION_FAILED(412, "412", "Precondition Failed"),

    /**
     * @see <a href="https://http.dev/413">413 Payload Too Large</a>
     */
    PAYLOAD_TOO_LARGE(413, "413", "Payload Too Large"),

    /**
     * @see <a href="https://http.dev/414">414 URI Too Long</a>
     */
    URI_TOO_LONG(414, "414", "URI Too Long"),

    /**
     * @see <a href="https://http.dev/415">415 Unsupported Media Type</a>
     */
    UNSUPPORTED_MEDIA_TYPE(415, "415", "Unsupported Media Type"),

    /**
     * @see <a href="https://http.dev/416">416 Range Not Satisfiable</a>
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "416", "Requested range not satisfiable"),

    /**
     * @see <a href="https://http.dev/417">417 Expectation Failed</a>
     */
    EXPECTATION_FAILED(417, "417", "Expectation Failed"),

    /**
     * @see <a href="https://http.dev/418">418 I'm a Teapot</a>
     */
    I_AM_A_TEAPOT(418, "418", "I'm a teapot"),

    /**
     * @see <a href="https://http.dev/419">419 Page Expired</a>
     */
    PAGE_EXPIRED(419, "419", "Page Expired"),

    /**
     * @see <a href="https://http.dev/420">420 Method Failure</a>
     */
    METHOD_FAILURE(420, "420", "Method Failure"),

    /**
     * @see <a href="https://http.dev/421">421 Misdirected Request</a>
     */
    MISDIRECTED_REQUEST(421, "421", "Misdirected Request"),

    /**
     * @see <a href="https://http.dev/422">422 Unprocessable Entity</a>
     */
    UNPROCESSABLE_ENTITY(422, "422", "Unprocessable Entity"),

    /**
     * @see <a href="https://http.dev/423">423 Locked</a>
     */
    LOCKED(423, "423", "Locked"),

    /**
     * @see <a href="https://http.dev/424">424 Failed Dependency</a>
     */
    FAILED_DEPENDENCY(424, "424", "Failed Dependency"),

    /**
     * @see <a href="https://http.dev/425">425 Too Early</a>
     */
    TOO_EARLY(425, "425", "Too Early"),

    /**
     * @see <a href="https://http.dev/426">426 Upgrade Required</a>
     */
    UPGRADE_REQUIRED(426, "426", "Upgrade Required"),

    /**
     * @see <a href="https://http.dev/428">428 Precondition Required</a>
     */
    PRECONDITION_REQUIRED(428, "428", "Precondition Required"),

    /**
     * @see <a href="https://http.dev/429">429 Too Many Requests</a>
     */
    TOO_MANY_REQUESTS(429, "429", "Too Many Requests"),

    /**
     * @see <a href="https://http.dev/430">430 HTTP Status Code</a>
     */
    HTTP_STATUS_CODE(430, "430", "HTTP Status Code"),

    /**
     * @see <a href="https://http.dev/431">431 Request Header Fields Too Large</a>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "431", "Request Header Fields Too Large"),

    /**
     * @see <a href="https://http.dev/440">440 Login Time-Out</a>
     */
    LOGIN_TIME_OUT(440, "440", "Login Time-Out"),

    /**
     * @see <a href="https://http.dev/444">444 No Response</a>
     */
    NO_RESPONSE(444, "444", "No Response"),

    /**
     * @see <a href="https://http.dev/449">449 Retry With</a>
     */
    RETRY_WITH(449, "449", "Retry With"),

    /**
     * @see <a href="https://http.dev/450">450 Blocked by Windows Parental Controls</a>
     */
    BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450, "450", "Blocked by Windows Parental Controls"),

    /**
     * @see <a href="https://http.dev/451">451 Unavailable For Legal Reasons</a>
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "451", "Unavailable For Legal Reasons"),

    /**
     * @see <a href="https://http.dev/460">460 Client Closed Connection Prematurely</a>
     */
    CLIENT_CLOSED_CONNECTION_PREMATURELY(460, "460", "Client Closed Connection Prematurely"),

    /**
     * @see <a href="https://http.dev/463">463 Too Many Forwarded IP Addresses</a>
     */
    TOO_MANY_FORWARDED_IP_ADDRESSES(463, "463", "Too Many Forwarded IP Addresses"),

    /**
     * @see <a href="https://http.dev/464">464 Incompatible Protocol</a>
     */
    INCOMPATIBLE_PROTOCOL(464, "464", "Incompatible Protocol"),

    /**
     * @see <a href="https://http.dev/464">494 Request Header Too Large</a>
     */
    REQUEST_HEADER_TOO_LARGE(494, "494", "Request Header Too Large"),

    /**
     * @see <a href="https://http.dev/495">495 SSL Certificate Error</a>
     */
    SSL_CERTIFICATE_ERROR(495, "495", "SSL Certificate Error"),

    /**
     * @see <a href="https://http.dev/496">496 SSL Certificate Required</a>
     */
    SSL_CERTIFICATE_REQUIRED(496, "496", "SSL Certificate Required"),

    /**
     * @see <a href="https://http.dev/497">497 HTTP Request Sent to HTTPS Port</a>
     */
    HTTP_REQUEST_SENT_TO_HTTPS_PORT(497, "497", "HTTP Request Sent to HTTPS Port"),

    /**
     * @see <a href="https://http.dev/498">498 Invalid Token</a>
     */
    INVALID_TOKEN(498, "498", "Invalid Token"),

    /**
     * @see <a href="https://http.dev/499">499 Token Required or Client Closed Request</a>
     */
    TOKEN_REQUIRED_OR_CLIENT_CLOSED_REQUEST(499, "499", "Token Required or Client Closed Request"),

    /* ******************************************
     * 5.xx Server Error
     * ******************************************/

    /**
     * @see <a href="https://http.dev/500">500 Internal Server Error</a>
     */
    INTERNAL_SERVER_ERROR(500, "500", "Internal Server Error"),

    /**
     * @see <a href="https://http.dev/501">501 Not Implemented</a>
     */
    NOT_IMPLEMENTED(501, "501", "Not Implemented"),

    /**
     * @see <a href="https://http.dev/502">502 Bad Gateway</a>
     */
    BAD_GATEWAY(502, "502", "Bad Gateway"),

    /**
     * @see <a href="https://http.dev/503">503 Service Unavailable</a>
     */
    SERVICE_UNAVAILABLE(503, "503", "Service Unavailable"),

    /**
     * @see <a href="https://http.dev/504">504 Gateway Timeout</a>
     */
    GATEWAY_TIMEOUT(504, "504", "Gateway Timeout"),

    /**
     * @see <a href="https://http.dev/505">505 HTTP Version Not Supported</a>
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "505", "HTTP Version not supported"),

    /**
     * @see <a href="https://http.dev/506">506 Variant Also Negotiates</a>
     */
    VARIANT_ALSO_NEGOTIATES(506, "506", "Variant Also Negotiates"),

    /**
     * @see <a href="https://http.dev/507">507 Insufficient Storage</a>
     */
    INSUFFICIENT_STORAGE(507, "507", "Insufficient Storage"),

    /**
     * @see <a href="https://http.dev/508">508 Loop Detected</a>
     */
    LOOP_DETECTED(508, "508", "Loop Detected"),

    /**
     * @see <a href="https://http.dev/509">509 Bandwidth Limit Exceeded</a>
     */
    BANDWIDTH_LIMIT_EXCEEDED(509, "509", "Bandwidth Limit Exceeded"),

    /**
     * @see <a href="https://http.dev/510">510 Not Extended</a>
     */
    NOT_EXTENDED(510, "510", "Not Extended"),

    /**
     * @see <a href="https://http.dev/511">511 Network Authentication Required</a>
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "511", "Network Authentication Required"),

    /**
     * @see <a href="https://http.dev/520">520 Web Server Is Returning an Unknown Error</a>
     */
    WEB_SERVER_IS_RETURNING_AN_UNKNOWN_ERROR(520, "520", "Web Server Is Returning an Unknown Error"),

    /**
     * @see <a href="https://http.dev/521">521 Web Server Is Down</a>
     */
    WEB_SERVER_IS_DOWN(521, "521", "Web Server Is Down"),

    /**
     * @see <a href="https://http.dev/522">522 Connection Timed Out</a>
     */
    CONNECTION_TIMED_OUT(522, "522", "Connection Timed Out"),

    /**
     * @see <a href="https://http.dev/523">523 Origin Is Unreachable</a>
     */
    ORIGIN_IS_UNREACHABLE(523, "523", "Origin Is Unreachable"),

    /**
     * @see <a href="https://http.dev/524">524 A Timeout Occurred</a>
     */
    A_TIMEOUT_OCCURRED(524, "524", "A Timeout Occurred"),

    /**
     * @see <a href="https://http.dev/525">525 SSL Handshake Failed</a>
     */
    SSL_HANDSHAKE_FAILED(525, "525", "SSL Handshake Failed"),

    /**
     * @see <a href="https://http.dev/526">526 Invalid SSL Certificate</a>
     */
    INVALID_SSL_CERTIFICATE(526, "526", "Invalid SSL Certificate"),

    /**
     * @see <a href="https://http.dev/527">527 Railgun Listener to Origin</a>
     */
    RAILGUN_LISTENER_TO_ORIGIN(527, "527", "Railgun Listener to Origin"),

    /**
     * @see <a href="https://http.dev/529">529 The Service Is Overloaded</a>
     */
    THE_SERVER_IS_OVERLOADED(529, "529", "The Service Is Overloaded"),

    /**
     * @see <a href="https://http.dev/530">530 Site Frozen</a>
     */
    SITE_FROZEN(530, "530", "Site Frozen"),

    /**
     * @see <a href="https://http.dev/561">561 Unauthorized</a>
     */
    SERVER_UNAUTHORIZED(561, "561", "Unauthorized"),

    /**
     * @see <a href="https://http.dev/598">598 Network Read Timeout Error</a>
     */
    NETWORK_READ_TIMEOUT_ERROR(598, "598", "Network Read Timeout Error"),

    /**
     * @see <a href="https://http.dev/599">599 Network Connect Timeout Error</a>
     */
    NETWORK_CONNECT_TIMEOUT_ERROR(599, "599", "Network Connect Timeout Error"),

    /* ******************************************
     * 5.xx Other Error
     * ******************************************/

    /**
     * 597 服务出错
     */
    SERVICE_ERROR(597, "597", "Service Error"),

    /**
     * @see <a href="https://http.dev/110">110 Response Is Stale</a>
     */
    RESPONSE_IS_STALE(110, "110", "Response Is Stale"),

    /**
     * @see <a href="https://http.dev/111">111 Revalidation Failed</a>
     */
    REVALIDATION_FAILED(111, "111", "Revalidation Failed"),

    /**
     * @see <a href="https://http.dev/112">112 Disconnected Operation</a>
     */
    DISCONNECTED_OPERATION(112, "112", "Disconnected Operation"),

    /**
     * @see <a href="https://http.dev/113">113 Heuristic Expiration</a>
     */
    HEURISTIC_EXPIRATION(113, "113", "Heuristic Expiration"),

    /**
     * @see <a href="https://http.dev/199">199 Miscellaneous Warning</a>
     */
    MISCELLANEOUS_WARNING(199, "199", "Miscellaneous Warning"),

    /**
     * @see <a href="https://http.dev/214">214 Transformation Applied</a>
     */
    TRANSFORMATION_APPLIED(214, "214", "Transformation Applied"),

    /**
     * @see <a href="https://http.dev/299">299 Miscellaneous Persistent Warning</a>
     */
    MISCELLANEOUS_PERSISTENT_WARNING(299, "299", "Miscellaneous Persistent Warning"),

    /**
     * @see <a href="https://http.dev/999">999 999</a>
     */
    UNKNOWN(999, "999", "Unknown");

    private final Integer status;

    private final String code;

    private final String msg;

    HttpResponseCode(Integer status, String code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }

    public int getStatus(){
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
