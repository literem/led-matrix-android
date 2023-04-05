package com.literem.matrix.common.font;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
/**
 * author : literem
 * time   : 2022/12/05
 * desc   : 功能：读取GBK编码的类，位于sun.nio.cs包下的类，由于as中没有该包，复制源码新建了一个类
 * version: 1.0
 */
public class ThreadLocalCoders {
    private static final int CACHE_SIZE = 3;
    private static ThreadLocalCoders.Cache decoderCache = new ThreadLocalCoders.Cache(3) {
        boolean hasName(Object var1, Object var2) {
            if (var2 instanceof String) {
                return ((CharsetDecoder)var1).charset().name().equals(var2);
            } else {
                return var2 instanceof Charset && ((CharsetDecoder) var1).charset().equals(var2);
            }
        }

        Object create(Object var1) {
            if (var1 instanceof String) {
                return Charset.forName((String)var1).newDecoder();
            } else if (var1 instanceof Charset) {
                return ((Charset)var1).newDecoder();
            } else {
                return null;
            }
        }
    };
    private static ThreadLocalCoders.Cache encoderCache = new ThreadLocalCoders.Cache(3) {
        boolean hasName(Object var1, Object var2) {
            if (var2 instanceof String) {
                return ((CharsetEncoder)var1).charset().name().equals(var2);
            } else {
                return var2 instanceof Charset && ((CharsetEncoder) var1).charset().equals(var2);
            }
        }

        Object create(Object var1) {
            if (var1 instanceof String) {
                return Charset.forName((String)var1).newEncoder();
            } else if (var1 instanceof Charset) {
                return ((Charset)var1).newEncoder();
            } else {
                //assert false;
                return null;
            }
        }
    };

    public ThreadLocalCoders() {
    }

    public static CharsetDecoder decoderFor(Object var0) {
        CharsetDecoder var1 = (CharsetDecoder)decoderCache.forName(var0);
        var1.reset();
        return var1;
    }

    public static CharsetEncoder encoderFor(Object var0) {
        CharsetEncoder var1 = (CharsetEncoder)encoderCache.forName(var0);
        var1.reset();
        return var1;
    }

    private abstract static class Cache {
        private ThreadLocal cache = new ThreadLocal();
        private final int size;

        Cache(int var1) {
            this.size = var1;
        }

        abstract Object create(Object var1);

        private void moveToFront(Object[] var1, int var2) {
            Object var3 = var1[var2];

            for(int var4 = var2; var4 > 0; --var4) {
                var1[var4] = var1[var4 - 1];
            }

            var1[0] = var3;
        }

        abstract boolean hasName(Object var1, Object var2);

        Object forName(Object var1) {
            Object[] var2 = (Object[])this.cache.get();
            if (var2 == null) {
                var2 = new Object[this.size];
                this.cache.set(var2);
            } else {
                for(int var3 = 0; var3 < var2.length; ++var3) {
                    Object var4 = var2[var3];
                    if (var4 != null && this.hasName(var4, var1)) {
                        if (var3 > 0) {
                            this.moveToFront(var2, var3);
                        }

                        return var4;
                    }
                }
            }

            Object var5 = this.create(var1);
            var2[var2.length - 1] = var5;
            this.moveToFront(var2, var2.length - 1);
            return var5;
        }
    }
}
