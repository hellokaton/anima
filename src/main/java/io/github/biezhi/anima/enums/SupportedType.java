package io.github.biezhi.anima.enums;

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
