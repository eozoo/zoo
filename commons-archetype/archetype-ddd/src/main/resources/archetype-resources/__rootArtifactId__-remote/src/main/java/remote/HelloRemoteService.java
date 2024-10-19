#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.remote;

import ${package}.model.HelloModel;
import feign.RequestLine;
import org.springframework.feign.annotation.FeignClient;
import org.springframework.feign.codec.ResponseDecoder;

/**
 *
 * @author ${author}
 *
 */
@FeignClient(url = "http://localhost:${serverPort}", decoder = ResponseDecoder.class)
public interface HelloRemoteService {

    @RequestLine("GET #if(${serverPath} == '/')/api/v1/hello/ddd#else${serverPath}/api/v1/hello/ddd#end")
    HelloModel hello();
}
