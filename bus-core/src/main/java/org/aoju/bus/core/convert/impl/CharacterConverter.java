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
package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.core.utils.StringUtils;

/**
 * 字符转换器
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class CharacterConverter extends AbstractConverter<Character> {

    @Override
    protected Character convertInternal(Object value) {
        if (char.class == value.getClass()) {
            return Character.valueOf((char) value);
        } else if (value instanceof Boolean) {
            return BooleanUtils.toCharacter((Boolean) value);
        } else if (boolean.class == value.getClass()) {
            return BooleanUtils.toCharacter((boolean) value);
        } else {
            final String valueStr = convertToStr(value);
            if (StringUtils.isNotBlank(valueStr)) {
                return Character.valueOf(valueStr.charAt(0));
            }
        }
        return null;
    }

}
