/*******************************************************************************
 * Copyright (c) 2013 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise;

import org.apache.log4j.Logger;

public class JavaInfo {
    private static final Logger log = Logger.getLogger(JavaInfo.class);

    private static final PropertyName[] names = {
            new JavaInfo.PropertyName("java.home", "The directory in which Java is installed"),
            new JavaInfo.PropertyName("java.class.path", "The value of the CLASSPATH environment variable"),
            new JavaInfo.PropertyName("java.version", "The version of the Java interpreter"), };

    private static final class PropertyName {
        private String name;
        private String description;

        public PropertyName(String name, String description) {
            super();
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static final void logInfo() {
        for (PropertyName name : names) {
            String value = System.getProperty(name.getName());
            log.info("Java system property - name=" + name.getName() + ", value=" + value + ", desc=" + name.getDescription());
        }
    }
}
