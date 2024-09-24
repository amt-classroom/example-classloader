package ch.heigvd.amt.classloader;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

/**
 * A custom class loader that uses the Java Compiler API to compile and load classes at runtime.
 */
public class JavaCompilerApiClassLoader extends ClassLoader {
    // A map to store compiled class bytecode in memory. The class name is the key, and the bytecode is the value.
    private final Map<String, byte[]> classes = new HashMap<>();

    /**
     * Adds a compiled class to the class loader's internal storage.
     *
     * @param name     The name of the class.
     * @param byteCode The compiled bytecode of the class.
     */
    public void addClass(String name, byte[] byteCode) {
        classes.put(name, byteCode);
    }

    /**
     * Finds a class that has been previously added to the class loader.
     *
     * @param name The name of the class to find.
     * @return The resulting Class object.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    @Override
    protected Class<?> findClass(String name) {
        // Retrieve the bytecode from the map using the class name.
        byte[] byteCode = classes.get(name);
        // Define the class from the bytecode.
        return defineClass(name, byteCode, 0, byteCode.length);
    }

    public static void main(String[] args) throws Exception {
        // The name of the class we want to compile and load.
        String className = "HelloWorld";

        // The Java source code for the HelloWorld class, stored as a String.
        String sourceCode = """
                public class HelloWorld {
                    public void sayHello() {
                        System.out.println("Hello, World!");
                    }
                }
                """;

        // Obtain the system Java compiler. This is only available when running with a JDK, not a JRE.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // ByteArrayOutputStream to store the compiled bytecode in memory.
        ByteArrayOutputStream byteCodeStream = new ByteArrayOutputStream();

        // Create a SimpleJavaFileObject representing the source code in memory.
        // This is used as input to the Java compiler.
        SimpleJavaFileObject javaFileObject = new SimpleJavaFileObject(URI.create("string:///" + className + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;  // Return the source code as a CharSequence.
            }
        };

        // A custom JavaFileManager to control where the compiled bytecode is stored.
        // We are overriding it to direct the output to our ByteArrayOutputStream.
        JavaFileManager fileManager = new ForwardingJavaFileManager<>(compiler.getStandardFileManager(null, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                // Return a new SimpleJavaFileObject for the compiled bytecode, directing output to byteCodeStream.
                return new SimpleJavaFileObject(URI.create("bytes:///" + className + ".class"), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return byteCodeStream;  // Directs the compiler to write the bytecode to the byteCodeStream.
                    }
                };
            }
        };

        // Compile the source code using the compiler API.
        // The getTask() method sets up a compilation task with the source code and custom file manager.
        compiler.getTask(null, fileManager, null, null, null, List.of(javaFileObject)).call();

        // Create an instance of our custom class loader.
        JavaCompilerApiClassLoader classLoader = new JavaCompilerApiClassLoader();

        // Add the compiled bytecode to the class loader's storage.
        classLoader.addClass(className, byteCodeStream.toByteArray());

        // Load the compiled class and create an instance.
        Class<?> compiledClass = classLoader.loadClass(className);

        // Use reflection to invoke the sayHello() method on the created instance.
        Method method = compiledClass.getMethod("sayHello");
        method.invoke(compiledClass.getDeclaredConstructor().newInstance());
    }
}
