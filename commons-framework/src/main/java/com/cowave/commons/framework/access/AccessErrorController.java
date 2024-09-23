package com.cowave.commons.framework.access;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *
 * @ignore
 * @author shanhuiming
 *
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class AccessErrorController extends BasicErrorController {

    public AccessErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) { 
            return new ResponseEntity<>(status);
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        ResponseEntity<Map<String, Object>> resp = new ResponseEntity<>(body, status);
        AccessLogger.warn(status.value() + "  " + status.getReasonPhrase() + "  " + body.get("path"));
        return resp;
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> mediaTypeNotAcceptable(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        ResponseEntity<String> resp = ResponseEntity.status(status).build();
        AccessLogger.warn(JSON.toJSONString(resp));
        return resp;
    }
}
