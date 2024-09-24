package ch.heigvd.amt.classloader;

/**
 * A custom class loader that delegates to its parent class loader.
 */
public class CustomClassLoader extends ClassLoader {

    /**
     * Creates a new DelegatingClassLoader with the given parent class loader.
     *
     * @param parent The parent class loader to delegate to.
     */
    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Finds a class by delegating to the parent class loader.
     *
     * @param name The name of the class to find.
     * @return The resulting Class object.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Load the class from a custom source
        System.out.println("Trying to load class with custom class loader: " + name);

        // If the class is not found, delegate to the parent class loader
        return super.findClass(name);
    }

    public static void main(String[] args) throws Exception {
        // Create a new CustomClassLoader with the system class loader as its parent
        CustomClassLoader customClassLoader = new CustomClassLoader(ClassLoader.getSystemClassLoader());

        // Load the class using the custom class loader
        Class<?> clazz = customClassLoader.loadClass("java.sql.DriverManager");
    }
}
