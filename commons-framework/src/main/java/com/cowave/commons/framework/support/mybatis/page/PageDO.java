/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.mybatis.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cowave.convert.CollectionConverts;
import org.springframework.feign.codec.Response;

import java.util.List;
import java.util.function.Function;

/**
 *
 * @author jiangbo
 *
 */
public class PageDO<T> extends Page<T> {

    public PageDO() {

    }

    public PageDO(long current, long size) {
        super(current, size);
    }

    public static <T> PageDO<T> of(Integer page, Integer pageSize) {
        return new PageDO<>(page, pageSize);
    }

    public static <T> PageDO<T> of(Integer total, List<T> list) {
        PageDO<T> pageDO = new PageDO<>();
        pageDO.setTotal(total);
        pageDO.setRecords(list);
        return pageDO;
    }

    public List<T> getList() {
        return getRecords();
    }

    public Response.Page<T> page() {
        return new Response.Page<>(getRecords(), getTotal());
    }

    public <V> Response.Page<V> page(Class<V> clazz) {
        List<V> vos = CollectionConverts.copyTo(getRecords(), clazz);
        return new Response.Page<>(vos, getTotal());
    }

    public <V> Response.Page<V> page(Function<T, V> mapper) {
        List<V> vos = CollectionConverts.copyTo(getRecords(), mapper);
        return new Response.Page<>(vos, getTotal());
    }
}
