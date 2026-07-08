package com.cowave.zoo.framework.access.annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

/**
 *
 * @author shanhuiming
 *
 */
public class SensitiveBeanPropertyWriter extends BeanPropertyWriter {

    private final BeanPropertyWriter delegate;

    public SensitiveBeanPropertyWriter(BeanPropertyWriter delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        Object value = delegate.get(bean);
        if (value == null) {
            delegate.serializeAsField(bean, gen, prov);
            return;
        }
        gen.writeStringField(delegate.getName(), "******");
    }
}
