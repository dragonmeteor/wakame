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
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
    public static String relativizeSecondToFirst(String firstDir, String secondFile) {
        Path firstPath = Paths.get(firstDir).toAbsolutePath();
        Path secondPath = Paths.get(secondFile).toAbsolutePath();
        Path result = firstPath.relativize(secondPath);
        return FilenameUtils.separatorsToUnix(result.toString());
    }

    public static String relativizeSecondToFirstDir(String firstFile, String secondFile) {
        Path firstPath = Paths.get(firstFile).toAbsolutePath();
        Path secondPath = Paths.get(secondFile).toAbsolutePath();
        Path result = firstPath.getParent().relativize(secondPath);
        return FilenameUtils.separatorsToUnix(result.toString());
    }

    public static String getNormalizedAbsolutePath(String path) {
        path = FilenameUtils.normalize(new File(path).getAbsolutePath(), true);
        return path;
    }

}
