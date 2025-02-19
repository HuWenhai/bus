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
package org.aoju.bus.core.text.csv;

import java.io.Serializable;

/**
 * CSV读取配置项
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class CsvReadConfig extends CsvConfig implements Serializable {

    private static final long serialVersionUID = 5396453565371560052L;

    /**
     * 是否首行做为标题行,默认false
     */
    protected boolean containsHeader;
    /**
     * 是否跳过空白行,默认true
     */
    protected boolean skipEmptyRows = true;
    /**
     * 每行字段个数不同时是否抛出异常,默认false
     */
    protected boolean errorOnDifferentFieldCount;

    /**
     * 默认配置
     *
     * @return 默认配置
     */
    public static CsvReadConfig defaultConfig() {
        return new CsvReadConfig();
    }

    /**
     * 设置是否首行做为标题行,默认false
     *
     * @param containsHeader 是否首行做为标题行,默认false
     */
    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }

    /**
     * 设置是否跳过空白行,默认true
     *
     * @param skipEmptyRows 是否跳过空白行,默认true
     */
    public void setSkipEmptyRows(boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
    }

    /**
     * 设置每行字段个数不同时是否抛出异常,默认false
     *
     * @param errorOnDifferentFieldCount 每行字段个数不同时是否抛出异常,默认false
     */
    public void setErrorOnDifferentFieldCount(boolean errorOnDifferentFieldCount) {
        this.errorOnDifferentFieldCount = errorOnDifferentFieldCount;
    }

}
