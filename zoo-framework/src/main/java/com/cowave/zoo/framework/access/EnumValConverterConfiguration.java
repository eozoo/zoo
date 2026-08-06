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
package com.cowave.zoo.framework.access;

import com.cowave.zoo.tools.EnumVal;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author shanhuiming
 */
@Configuration
public class EnumValConverterConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new EnumValConverterFactory());
    }

    private static class EnumValConverterFactory implements ConverterFactory<String, Enum<?>> {
        @Override
        public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
            if (!EnumVal.class.isAssignableFrom(targetType)) {
                return null;
            }
            return source -> {
                for (T e : targetType.getEnumConstants()) {
                    if (((EnumVal<?>) e).getVal().toString().equals(source)) {
                        return e;
                    }
                }
                throw new IllegalArgumentException("No enum constant matching value: " + source);
            };
        }
    }
}
