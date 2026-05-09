package com.Kari3600.mc.fisher;

import com.Kari3600.mc.fisher.util.LambdaUtil;
import com.Kari3600.mc.fisher.util.TypeReference;
import junit.framework.TestCase;

public class SpelTest extends TestCase {

    interface MyBiFunction<T, U, R> {
        R apply(T t, U u);
    }

    static class ClassA {
        private final int a;

        public int getA() {
            return a;
        }

        public ClassA(int a) {
            this.a = a;
        }
    }

    public void test() {
        TypeReference<MyBiFunction<ClassA, Integer, Integer>> type = new TypeReference<MyBiFunction<ClassA, Integer, Integer>>() {};
        MyBiFunction<ClassA, Integer, Integer> func = LambdaUtil.parseExpressionAsFunctionalInterface("#t.getA() * #u", type);
        assertEquals(15, (int) func.apply(new ClassA(3), 5));
    }
}
