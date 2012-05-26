/*******************************************************************************
 * Copyright (c) 2010 Hung Le
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class BuildNumber {
    private static final Logger log = Logger.getLogger(BuildNumber.class);

    public static String findBuilderNumber(String resourceName, ClassLoader classLoader) {
        String buildNumber = null;
        if (log.isDebugEnabled()) {
            log.debug("> findBuilderNumber: resourceName=" + resourceName + ", classLoader=" + classLoader);
        }

        if (classLoader == null) {
            return null;
        }
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(resourceName);
            if (resources == null) {
                log.warn("classLoader.getResources return null");
                return null;
            }
        } catch (IOException e) {
            log.warn(e);
            return null;
        }

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (log.isDebugEnabled()) {
                log.debug("  resource=" + resource);
            }
            if (resource == null) {
                break;
            }

            InputStream stream = null;
            try {
                stream = resource.openStream();
                if (stream == null) {
                    log.warn("  stream is null.");
                    break;
                }

                Manifest mf = null;
                try {
                    mf = new Manifest();
                    mf.read(stream);
                    Attributes attributes = mf.getMainAttributes();
                    // Implementation-Vendor-Id: com.le.tools.moneyutils
                    String id = attributes.getValue("Implementation-Vendor-Id");
                    if ((id == null) || (id.length() <= 0)) {
                        continue;
                    }
                    if (id.compareTo("com.le.tools.moneyutils") != 0) {
                        continue;
                    }
                    log.info("FOUND Manifest with id='" + "com.le.tools.moneyutils" + "'");
                    String build = attributes.getValue("Implementation-Build");
                    if (build != null) {
                        // GUI.VERSION = build;
                        buildNumber = build;
                        break;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Manifest has no value for \"Implementation-Build\", classLoader=" + classLoader);
                            log.debug("START - Dumping Manifest, resource=" + resource);
                            for (Object key : attributes.keySet()) {
                                Object value = attributes.get(key);
                                log.debug("    " + key + ": " + value);
                            }
                            log.debug("END - Dumping Manifest, resource=" + resource);
                        }
                    }
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    if (mf != null) {
                        mf = null;
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        stream = null;
                    }
                }
            }
        }
        return buildNumber;
    }

    public static String findBuilderNumber() {
        String buildNumber = null;
        String resourceName = "META-INF/MANIFEST.MF";
        ClassLoader classLoader = null;
        if (buildNumber == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            log.debug("> findBuilderNumber (contextClassLoader): resourceName=" + resourceName + ", classLoader=" + classLoader);
            buildNumber = findBuilderNumber(resourceName, classLoader);
        }
        if (buildNumber == null) {
            classLoader = log.getClass().getClassLoader();
            log.debug("> findBuilderNumber (logger classLoader): resourceName=" + resourceName + ", classLoader=" + classLoader);
            buildNumber = findBuilderNumber(resourceName, classLoader);
        }
        if (buildNumber == null) {
            classLoader = ClassLoader.getSystemClassLoader();
            log.debug("> findBuilderNumber (systemClassLoader): resourceName=" + resourceName + ", classLoader=" + classLoader);
            buildNumber = findBuilderNumber(resourceName, classLoader);
        }
        return buildNumber;
    }

}
