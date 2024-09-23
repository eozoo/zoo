/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda.lambda.meta;

import java.io.*;

/**
 * 当前类是 {@link java.lang.invoke.SerializedLambda } 的一个镜像
 * <p>
 * Create by hcl at 2020/7/17
 *
 * @author jiangbo
 */
public class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 8025925345765570181L;

    private Class<?> capturingClass;
    private String implMethodName;
    private String instantiatedMethodType;

    public static SerializedLambda extract(Serializable serializable) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(serializable);
            oos.flush();
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    Class<?> clazz = super.resolveClass(desc);
                    return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
                }

            }) {
                return (SerializedLambda) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    public String getImplMethodName() {
        return implMethodName;
    }
}
