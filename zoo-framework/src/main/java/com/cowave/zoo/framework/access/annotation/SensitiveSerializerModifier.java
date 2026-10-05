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
package com.cowave.zoo.framework.access.annotation;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author shanhuiming
 *
 */
public class SensitiveSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> writers = new ArrayList<>(beanProperties.size());
        Map<String, BeanPropertyDefinition> properties = beanDesc.findProperties().stream().collect(Collectors.toMap(
                BeanPropertyDefinition::getName, property -> property,
                (first, second) -> first));
        for (BeanPropertyWriter writer : beanProperties) {
            AnnotatedMember member = writer.getMember();
            BeanPropertyDefinition property = properties.get(writer.getName());
            boolean sensitive = member != null && member.hasAnnotation(Sensitive.class);
            if (!sensitive && property != null && property.getField() != null) {
                sensitive = property.getField().hasAnnotation(Sensitive.class);
            }
            if (sensitive) {
                writers.add(new SensitiveBeanPropertyWriter(writer));
            } else {
                writers.add(writer);
            }
        }
        return writers;
    }
}
