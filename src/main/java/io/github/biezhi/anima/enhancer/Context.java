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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.util.HashSet;
import java.util.Set;

public class Context {

    private Context parent;

    private Set<String> loadedClasses = new HashSet<String>();

    private ClassPool classPool;

    public Context(Context parent, ClassLoader loader) {
        this.parent = parent;
        if (parent == null) {
            classPool = ClassPool.getDefault();
        } else {
            classPool = new ClassPool(parent.classPool);
            classPool.appendClassPath(new LoaderClassPath(loader));
        }
    }

    /**
     * @return the parent
     */
    public Context getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Context parent) {
        this.parent = parent;
    }

    /**
     * @return the loadedClasses
     */
    public Set<String> getLoadedClasses() {
        return loadedClasses;
    }

    /**
     * @param loadedClasses the loadedClasses to set
     */
    public void setLoadedClasses(Set<String> loadedClasses) {
        this.loadedClasses = loadedClasses;
    }

    /**
     * @return the classPool
     */
    public ClassPool getClassPool() {
        return classPool;
    }

    /**
     * @param classPool the classPool to set
     */
    public void setClassPool(ClassPool classPool) {
        this.classPool = classPool;
    }

    public boolean isClassLoaded(String className) {
        if (loadedClasses.contains(className)) {
            return true;
        }
        if (parent != null) {
            return parent.isClassLoaded(className);
        }
        return false;
    }

    public CtClass getCtClass(String className) throws NotFoundException {
        return classPool.get(className);
    }

    public void addClass(String className) {
        loadedClasses.add(className);
    }
}