#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.core.db;

import ${package}.model.HelloModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ${author}
 *
 */
@RequiredArgsConstructor
@Repository
public class HelloDao {

    public HelloModel ddd(){
        return new HelloModel();
    }
}
