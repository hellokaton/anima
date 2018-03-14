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

import io.github.biezhi.anima.exception.InstrumentationException;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;

@Slf4j
public class Instrumentation {

    private String outputDirectory;

    public Instrumentation(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void instrument() {
        if (outputDirectory == null) {
            throw new InstrumentationException("Property 'outputDirectory' must be provided");
        }

        try {
            log.debug("**************************** Start ByteCode Enhanced ****************************");
            ActiveRecordModelFinder mf     = new ActiveRecordModelFinder();
            File                    target = new File(outputDirectory);
            mf.processDirectoryPath(target);
            ModelEnhanced mi = new ModelEnhanced();

            for (CtClass clazz : mf.getModels()) {
                byte[]           bytecode = mi.instrument(clazz);
                String           fileName = getFullFilePath(clazz);
                FileOutputStream fout     = new FileOutputStream(fileName);
                fout.write(bytecode);
                fout.flush();
                fout.close();
                log.debug("Instrumented class: " + fileName);
            }
            log.debug("**************************** End ByteCode Enhanced ****************************");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFullFilePath(CtClass modelClass) throws NotFoundException, URISyntaxException {
        return modelClass.getURL().toURI().getPath();
    }

}