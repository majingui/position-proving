package Backend.BackDemo.crypto;

import java.util.Map;

public class RSAtest {
    public static void main(String[] args) throws Exception {
        String A="hello";
        Map<String,String> KeyPair=RSA.generateKeyPair();
        String PublicKey1=KeyPair.get("publicKey");
        String PrivateKey1=KeyPair.get("privateKey");
        KeyPair=RSA.generateKeyPair();
        String PublicKey2=KeyPair.get("publicKey");
        String PrivateKey2=KeyPair.get("privateKey");
        String EN=RSA.encrypt(A,PublicKey2);
        String DE=RSA.decrypt(EN,PrivateKey2);
        System.out.println(DE);
    }
}
