package com.demo.app;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.ClassDefRewriter;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReWriter {
    private static final DexRewriter DEX_REWRITER = new DexRewriter(new RewriterModule() {
        @Nonnull
        @Override
        public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
            return new ClassDefRewriter(rewriters) {
                @Nonnull
                @Override
                public ClassDef rewrite(@Nonnull ClassDef classDef) {
                    return new RewrittenClassDef(classDef) {
                        @Nullable
                        @Override
                        public String getSuperclass() {
                            String type = super.getType();
                            if (type.startsWith("Ljava") || type.startsWith("Lkotlin") || type.startsWith("Landroid")) {
                                return super.getSuperclass();
                            }
                            String superclass = super.getSuperclass();
                            if (superclass.equals("Ljava/lang/Thread;")) {
                                return "Lcom/demo/app/Faker/Thread;";
                            }
                            return superclass;
                        }
                    };
                }
            };
        }
    });

    public static DexFile transform(DexBackedDexFile f) {
        return DEX_REWRITER.getDexFileRewriter().rewrite(f);
    }

    public static InputStream fromDexFile(DexFile file) throws IOException {
        MemoryDataStore dataStore = new MemoryDataStore();
        DexPool.writeTo(dataStore, file);
        return dataStore.readAt(0);
    }
}
