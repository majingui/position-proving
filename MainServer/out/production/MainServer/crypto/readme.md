# 主要功能类
## AES（对称加密）
密钥格式：长度为16的字节数组（共128位）
明文和密文：字节数组
（1）encrypt：输入明文和密钥实现AES加密
（2）decrypt：输入密文和密钥实现AES解密（密钥错误将报错）

## RSA（非对称加密）
密钥格式：Map<String,String>（包括公钥串、私钥串、模数），每个串为字符串格式
明文和密文：字符串
（1）generateKeyPair；随机生成公私钥并返回
（2）encrypt：输入明文和公钥串，输出密文（公钥加密）
（3）decrypt：输入密文和私串，输出明文（私钥解密）
（4）sign：输入内容和私钥，输出签名
（5）checkSign：输入内容、签名和公钥，输出验证结果

## Digest（哈希摘要）
（1）signMD5：输入字符串以及指定的编码方式，输出MD5值
（2）hmacSign：HMACMD5算法：MAC（Message Authentication Code，消息认证码算法）是含有密钥散列函数算法，兼容了MD和SHA算法的特性，并在此基础上加上了密钥。因此MAC算法也经常被称作HMAC算法。
            ①输入字符串，输出MD5值
            ②输入字符串、密钥】编码方式，输出经过密钥处理过的MD5值
（3）hmacSHASign：字符串、密钥、编码方式，输出SHA函数结果
（4）digest：实现不带密钥的SHA摘要

## Base64
（1）encodeBase64：加密字节数组
（2）decodeBase64：解密字节数组
（3）encode：加密字符串
（4）decode：解密字符串