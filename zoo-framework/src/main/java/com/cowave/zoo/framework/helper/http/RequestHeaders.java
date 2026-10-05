package com.cowave.zoo.framework.helper.http;

import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.http.client.asserts.I18Messages;
import com.cowave.zoo.tools.ids.IdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.cowave.zoo.http.client.constants.HttpHeader.*;

/**
 * @author shanhuiming
 */
public final class RequestHeaders {

    // 公共请求ID生成器
    private static final IdGenerator GENERATOR = new IdGenerator();

    // 允许透传的来源请求头
    private static final String[] SOURCE_HEADERS = {
            User_Agent,
            X_Real_IP,
            X_Forwarded_For,
            Proxy_Client_IP,
            WL_Proxy_Client_IP
    };

    private RequestHeaders() {

    }

    public static void apply(String port, int clusterId,
                             Predicate<String> predicate, BiConsumer<String, String> setHeader) {
        // X_Request_ID
        String accessId = Access.accessId();
        if (StringUtils.isNotBlank(accessId)) {
            setHeader.accept(X_Request_ID, accessId);
        } else if (!predicate.test(X_Request_ID)) {
            String prefix = "#" + clusterId + port;
            setHeader.accept(X_Request_ID, GENERATOR.generateIdWithDate(prefix, "", "yyyyMMddHHmmss", 1000));
        }
        // Accept_Language
        HttpServletRequest source = Access.httpRequest();
        if (!predicate.test(Accept_Language)) {
            String language = source == null ? null : source.getHeader(Accept_Language);
            if (StringUtils.isBlank(language)) {
                language = I18Messages.getLanguage().getLanguage();
            }
            if (StringUtils.isNotBlank(language)) {
                setHeader.accept(Accept_Language, language);
            }
        }
        // 来源请求头
        if (source != null) {
            for (String name : SOURCE_HEADERS) {
                if (!predicate.test(name)) {
                    String value = source.getHeader(name);
                    if (StringUtils.isNotBlank(value)) {
                        setHeader.accept(name, value);
                    }
                }
            }
        }
    }
}
