package MianAct;

public class CA2package { /*对应从用户端发送的CA2的包*/
    private String EncodedKey; //用RSA加密过的AES密钥
    private String EncodedCa2; //用AES加密过的CA2的JSON

    public String getAESkey_Encode_ByRSA() {
        return EncodedKey;
    }
    public void setAESkey_Encode_ByRSA(String AESkey_Encode_ByRSA) {
        this.EncodedKey = AESkey_Encode_ByRSA;
    }
    public String getCA2() {
        return EncodedCa2;
    }
    public void setCA2(String EncodedCa2) {
        this.EncodedCa2 = EncodedCa2;
    }
}
