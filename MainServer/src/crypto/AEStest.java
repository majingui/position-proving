package crypto;

import java.util.Map;

public class AEStest {
    public static void main(String[] args) throws Exception {
        String A="hello";
        String Key="1234567812345678";
        String ENRES=AES.encryptToBase64(A,Key);
        System.out.println(ENRES+"\n");

        Map<String,String> KeyPair=RSA.generateKeyPair();
        String PublicKey1=KeyPair.get("publicKey");
        String PrivateKey1=KeyPair.get("privateKey");
        String RSAEN=RSA.encrypt(Key,PublicKey1);
        System.out.println(RSAEN+"\n");
        String Key2=RSA.decrypt(RSAEN,PrivateKey1);

        String DERES=AES.decryptFromBase64(ENRES,Key2);
        System.out.println(DERES);
    }
}
