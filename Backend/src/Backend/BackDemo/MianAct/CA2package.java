package Backend.BackDemo.MianAct;

public class CA2package { /*对应从用户端发送的CA2的包*/
    private String AESkey_Encode_ByRSA; //用RSA加密过的AES密钥
    private String CA2; //CA2的JSON字符串
    public String getAESkey_Encode_ByRSA() {
        return AESkey_Encode_ByRSA;
    }
    public void setAESkey_Encode_ByRSA(String AESkey_Encode_ByRSA) {
        this.AESkey_Encode_ByRSA = AESkey_Encode_ByRSA;
    }
    public String getCA2() {
        return CA2;
    }
    public void setCA2(String CA2) {
        this.CA2 = CA2;
    }
}
