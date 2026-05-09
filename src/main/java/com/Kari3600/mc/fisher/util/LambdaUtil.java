/*
 * Copyright (c) 2026. Kari3600.
 * This file is part of MinecraftFisher.
 *
 * MinecraftFisher is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MinecraftFisher is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MinecraftFisher. If not, see <https://www.gnu.org/licenses/>.
 */

package com.Kari3600.mc.fisher.util;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.*;

public class LambdaUtil {

    private static Method findSingleAbstractMethod(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Provided type must be an interface.");
        }

        Method found = null;

        for (Method method : type.getMethods()) {

            if (Modifier.isAbstract(method.getModifiers())
                    && method.getDeclaringClass() != Object.class) {

                if (found != null) {
                    throw new IllegalArgumentException("Interface has multiple abstract methods.");
                }

                found = method;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Interface has no abstract methods.");
        }

        return found;
    }

    private static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        throw new IllegalArgumentException("Unsupported Type: " + type);
    }

    private static Type resolveType(Type type, Type contextType) {

        if (!(contextType instanceof ParameterizedType)) {
            return type;
        }

        ParameterizedType pt = (ParameterizedType) contextType;
        TypeVariable<?>[] vars =
                ((Class<?>) pt.getRawType()).getTypeParameters();

        Type[] actualTypes = pt.getActualTypeArguments();

        if (type instanceof TypeVariable<?>) {

            for (int i = 0; i < vars.length; i++) {
                if (vars[i].equals(type)) {
                    return actualTypes[i];
                }
            }
        }

        return type;
    }

    private static Type[] resolveTypes(Type[] types, Type contextType) {
        Type[] resolved = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            resolved[i] = resolveType(types[i], contextType);
        }
        return resolved;
    }

    private static InvocationHandler parseExpressionAsInvocationHandler(String expression, Parameter[] parameters, Class<?> returnType, ClassLoader classLoader) {
        SpelParserConfiguration config =
                new SpelParserConfiguration(
                        SpelCompilerMode.IMMEDIATE,
                        classLoader);

        ExpressionParser parser = new SpelExpressionParser(config);
        Expression compiledExpression = parser.parseExpression(expression);

        return (proxy, method, args) -> {

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(proxy, args);
            }

            StandardEvaluationContext context = new StandardEvaluationContext();

            for (int i = 0; i < args.length; i++) {
                System.out.println("Argument nr " + i + " (" + parameters[i].getName() + "): " + args[i]);
                context.setVariable(parameters[i].getName(), args[i]);
                context.setVariable("arg" + i, args[i]);
            }

            System.out.println("Params: "+context.lookupVariable("arg0"));

            return compiledExpression.getValue(context, returnType);
        };
    }

    public static <T> T parseExpressionAsFunctionalInterface(String expression, Class<T> functionalInterface) {
        Method functionalMethod = findSingleAbstractMethod(functionalInterface);

        InvocationHandler handler = parseExpressionAsInvocationHandler(
                expression,
                functionalMethod.getParameters(),
                functionalMethod.getReturnType(),
                functionalInterface.getClassLoader()
        );

        return (T) Proxy.newProxyInstance(
                functionalInterface.getClassLoader(),
                new Class[]{functionalInterface},
                handler
        );
    }

    public static <T> T parseExpressionAsFunctionalInterface(String expression, TypeReference<T> typeReference) {
        Type functionalInterface = typeReference.getType();

        Class<?> rawClass = getRawClass(functionalInterface);

        Method functionalMethod = findSingleAbstractMethod(rawClass);

        Type resolvedReturnType =
                resolveType(functionalMethod.getGenericReturnType(), functionalInterface);

        Type[] resolvedParamTypes =
                resolveTypes(functionalMethod.getGenericParameterTypes(), functionalInterface);

        InvocationHandler handler = parseExpressionAsInvocationHandler(
                expression,
                functionalMethod.getParameters(),
                getRawClass(resolvedReturnType),
                rawClass.getClassLoader()
        );

        return (T) Proxy.newProxyInstance(
                rawClass.getClassLoader(),
                new Class[]{rawClass},
                handler
        );
    }
}
