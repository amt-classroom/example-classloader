package ch.heigvd.amt.classloader;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom class loader that uses the ClassFile API to compile and load classes at runtime.
 */
public class ClassFileApiClassLoader extends ClassLoader {
    // A map to store the compiled class bytecode in memory.
    // The class name is the key, and the bytecode is the value.
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
        // The name of the class we want to create and load.
        String className = "HelloWorld";

        // Create the bytecode for the HelloWorld class using the ClassFile API.
        byte[] byteCode = createHelloWorldClass();

        // Create an instance of our custom class loader.
        ClassFileApiClassLoader classLoader = new ClassFileApiClassLoader();

        // Add the generated bytecode to the class loader's storage.
        classLoader.addClass(className, byteCode);

        // Load the compiled class and create an instance.
        Class<?> compiledClass = classLoader.loadClass(className);

        // Use reflection to invoke the sayHello() method on the created instance.
        Method method = compiledClass.getMethod("sayHello");
        method.invoke(compiledClass.getDeclaredConstructor().newInstance());
    }

    /**
     * Creates a byte array containing the bytecode for a HelloWorld class with a sayHello() method.
     *
     * @return A byte array containing the bytecode for the HelloWorld class.
     */
    public static byte[] createHelloWorldClass() {
        // Use the ClassFile API to build a new class called "HelloWorld"
        return ClassFile.of().build(
                ClassDesc.of("HelloWorld"),  // Define the class descriptor for "HelloWorld"
                clb -> clb.withFlags(ClassFile.ACC_PUBLIC)  // Set the class as public
                        // Add a default constructor to the class
                        .withMethod(
                                ConstantDescs.INIT_NAME,  // The constructor's name is "<init>"
                                ConstantDescs.MTD_void,   // The constructor returns void
                                ClassFile.ACC_PUBLIC,     // The constructor is public
                                mb -> mb.withCode(        // Add bytecode to the constructor
                                        cob -> cob.aload(0)  // Load 'this' onto the stack (for the superclass call)
                                                .invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void) // Call the superclass constructor (Object's constructor)
                                                .return_()  // Return from the constructor
                                ))
                        // Add a public method sayHello() that prints "Hello, World!" to the console
                        .withMethod(
                                "sayHello",  // The method name
                                MethodTypeDesc.of(ConstantDescs.CD_void),  // The method returns void
                                ClassFile.ACC_PUBLIC,  // The method is public
                                mb -> mb.withCode(     // Add bytecode to the method
                                        cob -> cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")) // Get the static field System.out of type PrintStream
                                                .ldc("Hello, World!")  // Load the constant "Hello, World!" onto the stack
                                                .invokevirtual(ClassDesc.of("java.io.PrintStream"), "println", MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_String)) // Invoke the println method of PrintStream to print the message
                                                .return_()  // Return from the method
                                )));
    }
}