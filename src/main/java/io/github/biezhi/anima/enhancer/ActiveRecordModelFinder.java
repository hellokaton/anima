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
import io.github.biezhi.anima.exception.InstrumentationException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ActiveRecordModelFinder {

    private final CtClass modelClass;
    private final List<CtClass> models = new ArrayList<>();
    private final ClassPool cp;
    private       String    currentDirectoryPath;

    public static final String ACTIVE_RECORD_CLASS = ActiveRecord.class.getName();

    protected ActiveRecordModelFinder() throws NotFoundException {
        cp = ClassPool.getDefault();
        modelClass = cp.get(ACTIVE_RECORD_CLASS);
    }

    protected void processDirectoryPath(File directory) throws IOException {
        currentDirectoryPath = directory.getCanonicalPath();
        processDirectory(directory);
    }

    /**
     * Recursively processes this directory.
     *
     * @param directory - start directory for processing.
     */
    private void processDirectory(File directory) throws IOException {
        findFiles(directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                }
            }
        }
    }

    /**
     * This will scan directory for class files, non-recursive.
     *
     * @param directory directory to scan.
     * @throws IOException, NotFoundException
     */
    private void findFiles(File directory) throws IOException {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".class"));
        if (files != null) {
            for (File file : files) {
                int    current   = currentDirectoryPath.length();
                String fileName  = file.getCanonicalPath().substring(++current);
                String className = fileName.replace(File.separatorChar, '.').substring(0, fileName.length() - 6);
                tryClass(className);
            }
        }
    }

    protected void tryClass(String className) {
        try {
            CtClass clazz = getClazz(className);
            if (isModel(clazz)) {
                if (!models.contains(clazz)) {
                    models.add(clazz);
                    log.debug("Found model: " + className);
                }
            }
        } catch (Exception e) {
            throw new InstrumentationException(e);
        }
    }

    protected CtClass getClazz(String className) throws NotFoundException {
        return cp.get(className);
    }

    protected boolean isModel(CtClass clazz) {
        return clazz != null && notAbstract(clazz) && clazz.subclassOf(modelClass) && !clazz.equals(modelClass);
    }

    private boolean notAbstract(CtClass clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers));
    }

    protected List<CtClass> getModels() {
        return models;
    }
}