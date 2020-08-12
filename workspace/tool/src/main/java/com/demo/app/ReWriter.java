package com.demo.app;

import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.util.Set;

public class ReWriter {
    public static DexBackedDexFile rewrite(DexBackedDexFile f) {
        Set<? extends DexBackedClassDef> classes = f.getClasses();
        for (DexBackedClassDef c : classes) {
            String type = c.getType();
            if (type.startsWith("Ljava") || type.startsWith("Lkotlin") || type.startsWith("Landroid")) {
                continue;
            }
            String superclass = c.getSuperclass();
            if (superclass.equals("Ljava/lang/Thread;")) {
                System.out.println(type);
            }
        }
        return f;
    }
}
