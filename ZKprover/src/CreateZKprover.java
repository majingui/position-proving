import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.alibaba.fastjson.JSON;
import static java.lang.Math.abs;

public class CreateZKprover {
    public static String getRamdomStr(int length){
        String str="0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length-1;i++){
            int number=random.nextInt(10);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
    public static Map<String, BigInteger> initBigInts(){ //生成大整数a~s
        Map <String,BigInteger> BigInts = new HashMap<String,BigInteger>();
        String[] keys = {"a","b1","b2","b3","b4","b5","e1","e2","e3","e4","f1","f2","f3","f4","k","l1","l2","l3","l4","n","q1","q2","s"};
        for(int i=0;i<keys.length;i++){
            BigInts.put(keys[i],new BigInteger(getRamdomStr(512)));
        }
        return BigInts;
    }
    public static BigInteger ExGcd(BigInteger a,BigInteger b, BigInteger s, BigInteger t){ //扩展的欧几里得算法
        BigInteger[] DivideRes = a.divideAndRemainder(b); //计算a/b的商和余数
        BigInteger q = DivideRes[0]; //a/b
        BigInteger r = DivideRes[1]; //a%b
        BigInteger s_ = s.subtract(q.multiply(t)); //s_ = s-q*t
        if(r.equals(new BigInteger("1"))){
            return s_;
        }else
            return  ExGcd(b, r, t, s_);
    }
    public static BigInteger Inverse(BigInteger a, BigInteger mod){ //求乘法逆元
        BigInteger s = ExGcd(a, mod, new BigInteger("1"), new BigInteger("0"));
        return s.mod(mod);
    }
    public static BigInteger getPrimeMultiple(){ //随机选择两个随机大素数a和b，计算其乘积并返回
        BigInteger a = BigInteger.probablePrime(512,new Random());
        BigInteger b = BigInteger.probablePrime(512,new Random());
        return a.multiply(b);
    }
    public static Map <String,Integer> SquareSum4 (int A){ //将A表示为四个整数的平方和的形式
        Map <String,Integer> Res =new HashMap<String,Integer>();
        for(int c1 = 0 ; c1 < A ; c1++){
            for(int c2 = c1; c2 < A ; c2++){
                for(int c3 = c2 ; c3 < A ; c3++){
                    for(int c4 = c3 ; c4<A; c4++){
                        if(c1*c1 + c2*c2 + c3*c3 + c4*c4 == A){
                            Res.put("c1",c1);
                            Res.put("c2",c2);
                            Res.put("c3",c3);
                            Res.put("c4",c4);
                            return Res;
                        }
                    }
                }
            }
        }
        return Res;
    }
    public static  BigInteger Kex(BigInteger a, BigInteger e, BigInteger m){ //使用模重复平方法计算a^e(modm)
        BigInteger kex = new BigInteger("1");
        String binary = e.toString(2); //将指数转换为二进制字符串
        for(int i= binary.length()-1; i>=0; i--){
            if(binary.charAt(i)=='1'){
                kex = (kex.multiply(a)).remainder(m);
                a = (a.multiply(a)).remainder(m);
            }else{
                a= (a.multiply(a)).remainder(m);
            }
        }
        return  kex.mod(m);
    }
    public static String ZKProve(int a1, int a2, int a3, int R) { //证明者和见证者的经纬度，以及阈值半径
        int distances = a1*a1 + a2*a2 + a3*a3; //【2】计算distance
        if(R*R > distances){ //【3】当在阈值半径之内
            Map <String,BigInteger> BigInts = initBigInts(); //【1】生成a~s的随机大数
            BigInteger N = getPrimeMultiple(); //【4】随机选择两个随机大素数a和b，计算其乘积N
            Map <String,Integer> C1_4 = SquareSum4(R*R - distances); //【5】得到平方和为距离差的四个整数

            /*将Map转化为数组*/
            BigInteger []  b_ = {BigInts.get("b1"),BigInts.get("b2"),BigInts.get("b3"),BigInts.get("b4"),BigInts.get("b5")};
            BigInteger []  e_ = {BigInts.get("e1"),BigInts.get("e2"),BigInts.get("e3"),BigInts.get("e4")};
            BigInteger []  f_ = {BigInts.get("f1"),BigInts.get("f2"),BigInts.get("f3"),BigInts.get("f4")};
            BigInteger []  l_ = {BigInts.get("l1"),BigInts.get("l2"),BigInts.get("l3"),BigInts.get("l4")};
            BigInteger []  c_ = {new BigInteger(String.valueOf(C1_4.get("c1"))), new BigInteger(String.valueOf(C1_4.get("c2"))), new BigInteger(String.valueOf(C1_4.get("c3"))), new BigInteger(String.valueOf(C1_4.get("c4")))};
            BigInteger [] a_ ={new BigInteger(String.valueOf(a1)), new BigInteger(String.valueOf(a2)),new BigInteger(String.valueOf(a3))};

            /*【6】计算d1*/
            BigInteger d1 = new BigInteger("0");
            for(int i=0;i<3;i++) //计算Σei^2
                d1 = d1.add(e_[i].pow(2));
            for(int i=0;i<4;i++) //计算Σfi^2
                d1 = d1.add(f_[i].pow(2));
            d1 = d1.mod(N);

            /*【7】计算d2*/
            BigInteger d2 = new BigInteger("0");
            for(int i=0;i<4;i++) //计算Σcifi
                d2 = d2.add(f_[i].multiply(c_[i]));
            BigInteger AmulE = new BigInteger("1");
            for(int i=0;i<3;i++) //计算Πeiai
                AmulE = AmulE.multiply(e_[i].multiply(a_[i]));
            d2 = d2.add(AmulE);
            d2 = d2.mod(N);

            /*【8】计算g*/
            BigInteger g = new BigInteger("1");
            for(int i=0;i<4;i++){ //计算Πbi^ei(modN)
                g = g.multiply(Kex(b_[i],e_[i],N));
                g = g.mod(N);
            }
            /*此处以及之后的连乘积利用了公式(A*B)modN=(AmodN * BmodN)modN*/

            /*【9】计算h*/
            BigInteger h =Kex(b_[3],BigInts.get("k"),N);
            for(int i=0;i<4;i++) { //计算Πli^ci(modN)
                h = h.multiply(Kex(l_[i], c_[i], N));
                h = h.mod(N);
            }
            /*【10】计算m*/
            BigInteger m = Kex(b_[3],BigInts.get("n"),N);
            for(int i=0;i<4;i++) { //计算Πli^fi(modN)
                m = m.multiply(Kex(l_[i], f_[i], N));
                m = m.mod(N);
            }

            /*【11】计算p*/
            BigInteger p = Inverse(Kex(b_[3],d1,N),N);
            p = p.multiply(Kex(b_[4],BigInts.get("q1"),N));
            p = p.mod(N);

            /*【12】计算r*/
            BigInteger r = Inverse( Kex(b_[3],d2.multiply(new BigInteger("2")),N) ,N );
            r = r.multiply(Kex(b_[4],BigInts.get("q2"),N));
            r = r.mod(N);

            /*【13】生成x1~x4*/
            BigInteger [] x = new BigInteger[4];
            for(int i=0; i<3 ;i++){
                x[i] = BigInts.get("s").multiply( a_[i] );
                x[i] = x[i].add(e_[i]);
            }
            x[3] = BigInts.get("s").multiply(BigInts.get("a"));
            x[3] = x[3].add(e_[3]);

            /*【14】生成β*/
            BigInteger [] beta = new BigInteger[4];
            for(int i=0; i<4; i++)
                beta[i] = ( BigInts.get("s").multiply( c_[i] ) ).add(f_[i]);


            /*【15】生成γ*/
            BigInteger gamma = BigInts.get("s").multiply(BigInts.get("k"));
            gamma = gamma.add(BigInts.get("n"));

            /*【15】生成lambda*/
            BigInteger lambda = BigInts.get("s").multiply(BigInts.get("q2"));
            lambda = lambda.add(BigInts.get("q1"));

            /*【16】生成A*/
            BigInteger A = Kex(b_[3],BigInts.get("a"),N);
            for(int i=0;i<3;i++) {
                A = A.multiply(Kex(b_[i], a_[i], N));
                A = A.mod(N);
            }
            ZKpackage1 zKpackage1 = new ZKpackage1();
            ZKpackage2 zKpackage2 = new ZKpackage2();
            return createpackage(zKpackage1,zKpackage2,N,A,BigInts.get("s"),b_,x,g,R,beta,lambda,p,r,gamma,h,l_,m);
            //System.out.println(res);
            //System.out.println(res.length());
        }else{ //否则返回空
            return "{__}{__}";
        }
    }
    public static String createpackage(ZKpackage1 zKpackage1, ZKpackage2 zKpackage2, BigInteger n, BigInteger a, BigInteger s, BigInteger[] b, BigInteger[] x, BigInteger g, int R, BigInteger[] beta, BigInteger lambda, BigInteger p, BigInteger r, BigInteger gamma, BigInteger h, BigInteger[] l, BigInteger m){ //将零知识证明返回值打包
        zKpackage1.setA(a);
        zKpackage1.setX(x);
        zKpackage1.setR(R);
        zKpackage1.setLambda(lambda);
        zKpackage1.setR1(r);
        zKpackage1.setGamma(gamma);
        zKpackage1.setL(l);
        zKpackage1.setM(m);
        zKpackage2.setN(n);
        zKpackage2.setS(s);
        zKpackage2.setB(b);
        zKpackage2.setG(g);
        zKpackage2.setBeta(beta);
        zKpackage2.setP(p);
        zKpackage2.setH(h);
        return JSON.toJSONString(zKpackage1)+JSON.toJSONString(zKpackage2);
    }
}
