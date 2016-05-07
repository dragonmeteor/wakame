/*
 * This file is part of Wakame, a Java reimplementation of Nori, an educational ray tracer by Wenzel Jakob.
 *
 * Copyright (c) 2015 by Pramook Khungurn
 *
 * Wakame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 3
 * as published by the Free Software Foundation.
 *
 * Wakame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yondoko.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple class for resolving path names.
 *
 * This is a reimplementation of the "filesystem" library by Wenzel Jakob.
 */
public class FileResolver {
    private static ArrayList<String> paths = new ArrayList<String>();

    public static String resolve(String path) {
        for(String prefix : paths) {
            String combined = FilenameUtils.concat(prefix, path);
            File file = new File(combined);
            if (file.exists() && !file.isDirectory()) {
                return combined;
            }
        }
        return path;
    }

    public static void append(String path) {
        paths.add(path);
    }

    public static void prepend(String path) {
        paths.add(0, path);
    }
}
