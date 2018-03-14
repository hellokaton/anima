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
package io.github.biezhi.anima.utils;

/**
 * Utility class for composing SQL statements
 */
public class SqlUtils {
    
    private SqlUtils() {
    }

    public static boolean isNotEmpty(String value){
        return null != value && !value.isEmpty();
    }
    
    /**
     * User -> user | perfix_user
     */
    public static String toTableName(String className, String perfix) {
        boolean hasPerfix = perfix != null && perfix.trim().length() > 0;
        return hasPerfix ? perfix +"_"+toColumnName(className) + "s" : toColumnName(className) + "s";
    }
    
    /**
     * eg: userId -> user_id
     */
    public static String toColumnName(String propertyName) {
        StringBuilder result = new StringBuilder();
        if (propertyName != null && propertyName.length() > 0) {
            result.append(propertyName.substring(0, 1).toLowerCase());
            for (int i = 1; i < propertyName.length(); i++) {
                String s = propertyName.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                }
                else {
                    result.append(s);
                }
            }
        }
        return result.toString();
    }
    
    /**
     * eg: user_id -> userId
     */
    public static String toPropertyName(String columneName) {
        String[] partOfNames = columneName.split("_");
        StringBuffer sb = new StringBuffer(partOfNames[0]);
        for(int i=1; i<partOfNames.length; i++){
            sb.append(partOfNames[i].substring(0, 1).toUpperCase());
            sb.append(partOfNames[i].substring(1));
        }
        return sb.toString();
    }


}
