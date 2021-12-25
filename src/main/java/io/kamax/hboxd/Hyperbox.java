/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Max Dor
 *
 * https://apps.kamax.io/hyperbox
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.hboxd;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.HyperboxAPI;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.tools.Version;
import io.kamax.tools.logging.KxLog;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class Hyperbox {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static final Properties buildProperties = new Properties();
    private static final Reflections reflections = new Reflections("io.kamax");
    private static Version version;

    private static void failedToLoad(Exception e) {
        version = Version.UNKNOWN;
        log.error("Unable to access the build.properties file: " + e.getMessage());
        log.error("Version and revision will not be accurate");
    }

    public static <T> Set<T> loadSubTypes(Class<T> c) {
        Set<T> subTypeInstances = new HashSet<>();
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(c);
        subTypes.forEach(subType -> {
            Optional<T> subTypeInstance = Hyperbox.loadClass(subType);
            if (!subTypeInstance.isPresent()) return;
            subTypeInstances.add(subTypeInstance.get());
        });
        return subTypeInstances;
    }

    public static <T> T loadClass(String cfgKey, Class<?> fallback) {
        try {
            String className = Configuration.getSetting(cfgKey, fallback.getName());
            Optional<T> opt = loadClass(Class.forName(className));
            if (!opt.isPresent()) {
                throw new ClassNotFoundException();
            }
            return opt.get();
        } catch (ClassNotFoundException e) {
            throw new HyperboxException(e);
        }
    }

    public static <T> Optional<T> loadClass(Class<?> def) {
        try {
            if (Modifier.isAbstract(def.getModifiers())) {
                return Optional.empty();
            }

            return Optional.of((T) def.getConstructor().newInstance());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed class: " + def.getName());
            throw new HyperboxException(e);
        }
    }

    public static Version getVersion() {
        if (version == null) {
            loadVersions();
        }

        return version;
    }

    public static String getConfigFilePath() {
        return "conf/main.cfg";
    }

    private static void loadVersions() {
        try {
            InputStream buildPropertiesStream = Hyperbox.class.getResourceAsStream("/server.build.properties");
            if (Objects.isNull(buildPropertiesStream)) throw new IOException("No build properties resource");
            buildProperties.load(buildPropertiesStream);
            Version rawVersion = new Version(buildProperties.getProperty("version"));
            if (!rawVersion.isValid()) throw new IOException("Invalid build properties resource");
            version = rawVersion;
        } catch (IOException e) {
            failedToLoad(e);
        }
    }

    static String[] _args;

    public static void setArgs(String[] args) {
        if (_args == null) {
            _args = args;
        }
    }

    public static String[] getArgs() {
        return _args;
    }

    public static void processArgs(Set<String> args) {
        HyperboxAPI.processArgs(args);

        if (args.contains("-?") || args.contains("--help")) {
            System.out.println("Hyperbox available executable switches:\n");
            System.out.println("--help or -? : Print this help");
            // TODO enable more command line switches
            System.out.println("--apiversion : Print API version");
            System.out.println("--apirevision : Print API revision");
            System.out.println("--netversion : Print Net protocol version");
            System.out.println("--version : Print Server version");
            System.out.println("--revision : Print Server revision");
            System.out.println("--reset-admin-pass : Reset the admin password");
            System.exit(0);
        }
        if (args.contains("--version")) {
            System.out.println(getVersion());
            System.exit(0);
        }

    }

}
