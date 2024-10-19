#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.api.service.impl;

import ${package}.api.service.HelloService;
import ${package}.core.db.HelloDao;
import ${package}.model.HelloModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author ${author}
 *
 */
@RequiredArgsConstructor
@Service
public class HelloServiceImpl implements HelloService {

    private final HelloDao helloDao;

    @Override
    public HelloModel ddd() {
        return helloDao.ddd();
    }
}
