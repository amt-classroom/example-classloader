package ch.heigvd.amt.classloader;

import java.sql.DriverManager;

public class WhichClassLoader {

    public static void main(String... args) {
        System.out.println(WhichClassLoader.class.getClassLoader()); // AppClassLoader (application class loader)
        System.out.println(DriverManager.class.getClassLoader()); // PlatformClassLoader (extension class loader)
        System.out.println(String.class.getClassLoader()); // null (bootstrap class loader)
    }

}
