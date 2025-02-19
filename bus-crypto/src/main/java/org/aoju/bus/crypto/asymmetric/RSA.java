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
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.consts.Algorithm;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.crypto.Builder;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * <p>
 * RSA公钥/私钥/签名加密解密
 * </p>
 * <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 由于非对称加密速度极其缓慢,一般文件不使用它来加密而是使用对称加密,<br>
 * 非对称加密算法可以用来对对称加密的密钥加密,这样保证密钥的安全也就保证了数据的安全
 * </p>
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class RSA extends Asymmetric {

    /**
     * 构造,生成新的私钥公钥对
     */
    public RSA() {
        super(Algorithm.RSA);
    }

    /**
     * 构造,生成新的私钥公钥对
     *
     * @param rsaAlgorithm 自定义RSA算法,例如RSA/ECB/PKCS1Padding
     */
    public RSA(String rsaAlgorithm) {
        super(rsaAlgorithm);
    }

    /**
     * 构造<br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public RSA(String privateKey, String publicKey) {
        super(Algorithm.RSA, privateKey, publicKey);
    }

    /**
     * 构造<br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  自定义RSA算法,例如RSA/ECB/PKCS1Padding
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public RSA(String algorithm, String privateKey, String publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public RSA(byte[] privateKey, byte[] publicKey) {
        super(Algorithm.RSA, privateKey, publicKey);
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param modulus         N特征值
     * @param privateExponent d特征值
     * @param publicExponent  e特征值
     * @since 3.1.1
     */
    public RSA(BigInteger modulus, BigInteger privateExponent, BigInteger publicExponent) {
        this(generatePrivateKey(modulus, privateExponent), generatePublicKey(modulus, publicExponent));
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @since 3.1.1
     */
    public RSA(PrivateKey privateKey, PublicKey publicKey) {
        super(Algorithm.RSA, privateKey, publicKey);
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param rsaAlgorithm 自定义RSA算法,例如RSA/ECB/PKCS1Padding
     * @param privateKey   私钥
     * @param publicKey    公钥
     */
    public RSA(String rsaAlgorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(rsaAlgorithm, privateKey, publicKey);
    }

    /**
     * 生成RSA私钥
     *
     * @param modulus         N特征值
     * @param privateExponent d特征值
     * @return {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(BigInteger modulus, BigInteger privateExponent) {
        return Builder.generatePrivateKey(Algorithm.RSA_ECB_PKCS1, new RSAPrivateKeySpec(modulus, privateExponent));
    }

    /**
     * 生成RSA公钥
     *
     * @param modulus        N特征值
     * @param publicExponent e特征值
     * @return {@link PublicKey}
     */
    public static PublicKey generatePublicKey(BigInteger modulus, BigInteger publicExponent) {
        return Builder.generatePublicKey(Algorithm.RSA_ECB_PKCS1, new RSAPublicKeySpec(modulus, publicExponent));
    }

    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) {
        if (this.encryptBlockSize < 0) {
            // 加密数据长度 <= 模长-11
            this.encryptBlockSize = ((RSAKey) getKeyByType(keyType)).getModulus().bitLength() / 8 - 11;
        }
        return super.encrypt(data, keyType);
    }

    @Override
    public byte[] decrypt(byte[] bytes, KeyType keyType) {
        if (this.decryptBlockSize < 0) {
            // 加密数据长度 <= 模长-11
            this.decryptBlockSize = ((RSAKey) getKeyByType(keyType)).getModulus().bitLength() / 8;
        }
        return super.decrypt(bytes, keyType);
    }

    @Override
    protected void initCipher() {
        try {
            super.initCipher();
        } catch (InstrumentException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof NoSuchAlgorithmException) {
                // 在Linux下,未引入BC库可能会导致RSA/ECB/PKCS1Padding算法无法找到,此时使用默认算法
                this.algorithm = Algorithm.RSA;
                super.initCipher();
            }
            throw e;
        }
    }

}
