/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.infra.dao;

import ${package}.domain.HelloModel;
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
