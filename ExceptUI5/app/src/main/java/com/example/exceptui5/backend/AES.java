package com.example.exceptui5.backend;


//这个文件定义了加解密的一些信息，不用管
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

public class AES {//定义了AES类
    public  static byte[] encrypt(byte[] data, byte[] key) { //加密
        CheckUtils.notEmpty(data,"data");
        CheckUtils.notEmpty(key,"key");
        //验证密钥和明文不为空
        if (key.length != 16){//这里应该不会把\n算进去吧
            throw new RuntimeException("Invalid AES key length(must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key,"AES");//用key字符串密钥，指定算法，得到加密密钥对象
            byte [] enCodeFormat = secretKey.getEncoded();//获取加密的格式
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(ConfigureEncryptAndDecrypt.AES_ALGORITHM);// 创建密码器
            IvParameterSpec iv = new IvParameterSpec(key);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, seckey, iv);// 初始化
            byte[] result = cipher.doFinal(data);//对data加密
            return result; // 加密
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("encrypt fail!", e);
        }
    }
    public static byte[] decrypt(byte[] data, byte[] key) {//解密
        CheckUtils.notEmpty(data, "data");
        CheckUtils.notEmpty(key, "key");
        if (key.length != 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(ConfigureEncryptAndDecrypt.AES_ALGORITHM);// 创建密码器
            IvParameterSpec iv = new IvParameterSpec(key);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.DECRYPT_MODE, seckey, iv);// 初始化
            byte[] result = cipher.doFinal(data);
            return result; // 解密
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("decrypt fail!", e);
        }
    }
    //下面是base64编码方式的编码和解码方法
    public static String encryptToBase64(String data, String key) {
        try {
            byte[] valueByte = encrypt(data.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING), key.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING));
            return new String(Base64.encode(valueByte));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encrypt fail!", e);
        }

    }

    public static String decryptFromBase64(String data, String key) {
        try {
            byte[] originalData = Base64.decode(data.getBytes());
            byte[] valueByte = decrypt(originalData, key.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING));
            return new String(valueByte, ConfigureEncryptAndDecrypt.CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }
}

