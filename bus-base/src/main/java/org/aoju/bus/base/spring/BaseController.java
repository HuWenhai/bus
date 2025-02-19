/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.base.spring;

import io.swagger.annotations.ApiOperation;
import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基础请求封装
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class BaseController<Service extends BaseService<T>, T> extends Controller {

    @Autowired
    protected Service service;

    /**
     * 通用:添加数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "通用:添加数据", httpMethod = "POST")
    @ResponseBody
    public Object add(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.insertSelective(entity));
    }

    /**
     * 通用:删除数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @ApiOperation(value = "通用:删除数据", httpMethod = "POST")
    @ResponseBody
    public Object remove(T entity) {
        service.deleteById(entity);
        return write(ErrorCode.EM_SUCCESS);
    }

    /**
     * 通用:主键更新
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation(value = "通用:主键更新", httpMethod = "POST")
    @ResponseBody
    public Object update(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.updateSelectiveById(entity));
    }

    /**
     * 通用:数据主键查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据主键查询", httpMethod = "GET")
    @ResponseBody
    public Object get(T entity) {
        return write(service.selectById(entity));
    }

    /**
     * 通用:数据条件查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据条件查询", httpMethod = "GET")
    @ResponseBody
    public Object list(T entity) {
        return write(service.selectList(entity));
    }

    /**
     * 通用:数据分页查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据分页查询", httpMethod = "GET")
    @ResponseBody
    public Object page(T entity) {
        return write(service.page(entity));
    }

}
