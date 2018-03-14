/**
 * Copyright (c) 2018, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.biezhi.anima.enhancer;

import io.github.biezhi.anima.core.ActiveRecord;
import io.github.biezhi.anima.core.JavaRecord;
import io.github.biezhi.anima.exception.InstrumentationException;
import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModelEnhanced {

    private static       Map<ClassLoader, Context> contextMap = new HashMap<>();
    private static final String                    javaRecord = JavaRecord.class.getName();

    public ModelEnhanced() {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
    }

    public byte[] instrument(CtClass target) throws InstrumentationException {
        try {
            doInstrument(Thread.currentThread().getContextClassLoader(), target);
            target.detach();
            return target.toBytecode();
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    public Context getContext(ClassLoader classLoader) {
        Context context = contextMap.get(classLoader);
        Context parent  = null;
        if (context == null) {
            if (classLoader != null) {
                if (classLoader.getParent() != null) {
                    parent = getContext(classLoader.getParent());
                }
            }
            context = new Context(parent, classLoader);
            contextMap.put(classLoader, context);
        }
        return context;
    }

    private void doInstrument(ClassLoader loader, CtClass target) {
        String className = target.getSimpleName();
        try {
            try {
                target.getField("db");
                return;
            } catch (Exception e) {
            }
            Context context = getContext(loader);
            if (!context.isClassLoaded(className)) {
                log.trace("Attempting to enhance the class - " + target.getSimpleName());
                log.debug("Transforming the class - " + className);
                target.defrost();

                createDB(target);
                createModelMethods(context, target);

                context.addClass(className);
            }
        } catch (Exception e) {
            log.error("Failed while transforming the class " + className, e);
            throw new RuntimeException("Failed while transforming the class " + className, e);
        }
    }

    private void createDB(CtClass ctClass) {
        try {
            String db = String.format("private static final %s db = new %s(%s.class);", javaRecord, javaRecord, ctClass.getName());
            ctClass.addField(CtField.make(db, ctClass));
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private void createModelMethods(Context context, CtClass ctClass) throws CannotCompileException {
        createMethod(context, ctClass, "count", "long");
        createMethod(context, ctClass, "where", javaRecord, "String statement");
        createMethod(context, ctClass, "where", javaRecord, "String statement", "Object value");
        createMethod(context, ctClass, "in", javaRecord, "String column", "Object[] paramValues");
        createMethod(context, ctClass, "findById", ActiveRecord.class.getName(), "java.io.Serializable id");
        createMethod(context, ctClass, "all", "java.util.List");
    }

    private void createMethod(Context context, CtClass ctClass, String methodName, String returnType, String... arguments) throws CannotCompileException {
        CtMethod method;
        try {
            method = getMethod(context, ctClass, methodName, arguments);
            if (method != null) {
                ctClass.removeMethod(method);
            }
        } catch (NotFoundException e) {
            log.trace("The method {} doesn't exist, will create...", methodName);
            // Just ignore if the method doesn't exist already
        } catch (InstrumentationException e) {
            //return;
        }

        StringWriter writer = new StringWriter();
        writer.append("public static ").append(returnType).append(" ").append(methodName).append("(");
        if (arguments != null && arguments.length > 0) {
            for (int i = 0; i < arguments.length - 1; i++) {
                writer.append(arguments[i]).append(", ");
            }
            writer.append(arguments[arguments.length - 1]);
        }
        writer.append(") {");
        if (!returnType.equals("void")) {
            writer.append("return (" + returnType + ")");
        }

        writer.append(ctClass.getName()).append(".db.").append(methodName).append("(");
        if (arguments != null && arguments.length > 0) {
            for (int i = 0; i < arguments.length; i++) {
                String m = arguments[i].split(" ")[1];
                if (i != 0) {
                    writer.append(", ");
                }
                writer.append(m);
            }
        }
        writer.append(");}");

        method = CtNewMethod.make(writer.toString(), ctClass);
        ctClass.addMethod(method);
    }

    private CtMethod getMethod(Context context, CtClass ctClass, String methodName, String... arguments) throws NotFoundException {
        List<CtClass> paramTypes = new ArrayList<>();
        if (arguments != null) {
            for (String argument : arguments) {
                String arg = argument.split(" ")[0];
                try {
                    CtClass paramType = context.getCtClass(arg);
                    paramTypes.add(paramType);
                } catch (NotFoundException e) {
                    throw new InstrumentationException(e);
                }
            }
        }
        return ctClass.getDeclaredMethod(methodName, paramTypes.toArray(new CtClass[0]));
    }

}