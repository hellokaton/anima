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
package com.hellokaton.anima.enums;

import java.util.ArrayList;
import java.util.List;

public enum SupportedType {
    instance;
    
    public static boolean contains(String name){
        return supportedTypeList.contains(name.toLowerCase());
    }
    
    @SuppressWarnings({"serial"})
    private static List<String> supportedTypeList = new ArrayList<String>(){{
        add("string");     add("char");   add("character");
        add("integer");    add("int");    add("short");       add("byte");
        add("long");       add("float");  add("double");
        add("date");       add("time");   add("timestamp"); 
        add("boolean");
    }};
}
