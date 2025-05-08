package cc.winboll.studio.libjc.cmd;

/**
 * @Author ZhanGSKen@QQ.COM
 * @Date 2025/01/11 12:43:29
 * @Describe 列举定义了main函数的所有类。
 */
import java.io.File;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ListJarClassHasMain {

    public static final String TAG = "ListJarClassHasMain";

    public static void main(String[] args) {
        System.out.println("ListJarClassHasMain main");
        try {
            String jarFileName = getCurrentClassJarFileName();
            System.out.println(String.format("\nCurrent Class Jar File Name : %s", jarFileName));
            
            File fJarFolder = new File("/sdcard/WinBollStudio/DEV/jars/");
            System.out.println(String.format("\nJars Folder : %s", fJarFolder.getPath()));
            for (File fJarFile : fJarFolder.listFiles()) {
                if (fJarFile.getName().endsWith(".jar")) {
                    System.out.println(String.format("\nJar File Name : %s", fJarFile.getName()));
                    listJarFileMainClass(new JarFile(fJarFile.getPath()));
                }
            }

        } catch (Exception e) {
            System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
        }
    }

    static void listJarFileMainClass(JarFile jarFile) {
        System.out.println("\nMain Functions Class : ");
        //JarFile jarFile = new JarFile("yourJarFile.jar");
        java.util.Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    Method method = clazz.getMethod("main", String[].class);
                    if (method != null) {
                        System.out.println(className);
                    }
                } catch (Exception e) {
                    // 没有main方法或者加载类出错，忽略
                }
            }
        }
    }




    public static String getCurrentClassJarFileName() {
        String jarFileName = "";
        try {
            String classPath = ListJarClassHasMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            jarFileName = new java.io.File(classPath).getName();
        } catch (NullPointerException e) {
            // 忽略读取Jar文件失败问题，返回空数据
            return "";
        } catch (Exception e) {
            System.err.println(String.format("%s Exception : %s", TAG, e.getMessage()));
        }
        return jarFileName;
    }
}

