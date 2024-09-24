package ch.heigvd.amt.classloader;

public class DelegationChainInStackTrace {

    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> clazz = Class.forName("ch.heigvd.amt.MyClass");
    }

}
