/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.util.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to manage TiesDB annotations.
 * 
 * <P>
 * Contains some utility methods for TiesDB annotations handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class AnnotationHelper {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHelper.class);

    public static final String DEFAULT_BINDINGS_PATH = "META-INF/bindings/";

    private static final String FORBID_FLAG = "!";

    private static final String COMMENT_FLAG = "#";

    private final ClassLoader classLoader;
    private final String bindingPath;

    public AnnotationHelper() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public AnnotationHelper(ClassLoader classLoader) {
        this(classLoader, DEFAULT_BINDINGS_PATH);
    }

    public AnnotationHelper(ClassLoader classLoader, String bindingPath) {
        this.classLoader = classLoader;
        this.bindingPath = bindingPath;
    }

    public <A extends Annotation> Collection<Class<? extends Object>> getBindings(Class<A> annotation) throws IOException {
        return getBindings(annotation, classLoader.getResources(bindingPath + annotation.getName()));
    }

    public <A extends Annotation> Collection<Class<? extends Object>> getBindings(Class<A> annotation, Collection<URL> resource)
            throws IOException {
        return getBindings(annotation, Collections.enumeration(resource));
    }

    private <A extends Annotation> Collection<Class<? extends Object>> getBindings(Class<A> annotation, Enumeration<URL> resources)
            throws IOException {
        if (null == annotation) {
            throw new NullPointerException("The annotation should not be null");
        }
        if (null == resources) {
            throw new NullPointerException("The resources should not be null");
        }
        Set<Class<? extends Object>> classes = new HashSet<>();
        Set<Class<? extends Object>> forbidden = new HashSet<>();
        while (resources.hasMoreElements()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resources.nextElement().openStream()))) {
                String line;
                while (null != (line = reader.readLine())) {
                    if (line.startsWith(COMMENT_FLAG)) {
                        continue;
                    }
                    try {
                        if (line.startsWith(FORBID_FLAG)) {
                            forbidden.add(classLoader.loadClass(line.substring(FORBID_FLAG.length()).trim()));
                        } else {
                            classes.add(classLoader.loadClass(line.trim()));
                        }
                    } catch (ClassNotFoundException e) {
                        logger.warn("Could not find binding {}", line, e);
                    }
                }
            }
        }
        classes.removeAll(forbidden);
        return classes;
    }
}
