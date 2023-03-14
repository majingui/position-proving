package Backend.BackDemo.MianAct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonTest
{
    public static void main(String[] args)
    {
        UserData user= new UserData();
        user.setId(1);
        user.setName("lzc");
        user.setPublicKey("yzx");
        String json=JSON.toJSONString(user);//关键
        UserData userdata = JSON.parseObject(json,UserData.class);
        System.out.println(json);
    }

}
