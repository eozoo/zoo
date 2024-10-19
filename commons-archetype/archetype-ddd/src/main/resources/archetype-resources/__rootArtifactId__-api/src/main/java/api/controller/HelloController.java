#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.api.controller;

import ${package}.api.service.HelloService;
import ${package}.model.HelloModel;
import lombok.RequiredArgsConstructor;
import org.springframework.feign.codec.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ${author}
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/hello")
public class HelloController {

    private final HelloService helloService;

    @GetMapping("/ddd")
    public Response<HelloModel> ddd() {
        return Response.success(helloService.ddd());
    }
}
