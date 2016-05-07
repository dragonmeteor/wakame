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

import java.util.ArrayList;

public class StringUtil {
    /**
     * Splits the passed string into words separated by any one of a list of characters.
     *
     * This method is faster than String.split(), which uses regular expressions. We don't
     * need regex splitting here.
     *
     * @param str The string to split.
     * @param delims Words in the string are separated by any of these.
     * @param keepEmptyWords If true, empty words (between consecutive delimeter characters) are
     *        included in the result array.
     *
     * @return Array of words separated by characters in delims.
     */
    public static String[] splitString(String str, String delims, boolean keepEmptyWords)
    {
        ArrayList<String> result = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();

        int offset = 0;

		/* Loop over characters in the string. */
        while (offset < str.length())
        {
            char c = str.charAt(offset);

            if (delims.indexOf(c) < 0)
            {
				/* If this isn't in the delimeters list, add it to the current word. */
                builder.append(c);
            }
            else
            {
				/* This is a delimeter, so add the current word, if any, to the results list,
				 * and clear the builder. */
                if (builder.length() > 0 || keepEmptyWords)
                {
                    result.add(builder.toString());
                    builder.delete(0, builder.length());
                }
            }

			/* Next character. */
            ++offset;
        }

		/* Add the last word, if any. */
        if (builder.length() > 0 || keepEmptyWords)
        {
            result.add(builder.toString());
        }

        return result.toArray(new String[]{});
    }

    public static String indent(String s) {
        return indent(s, 1, 2);
    }

    public static String indent(String s, int level) {
        return indent(s, level, 2);
    }

    public static String indent(String s, int level, int spacePerLevel) {
        String spaceString = "";
        for (int i = 0; i < spacePerLevel; i++) {
            spaceString += " ";
        }
        String[] comps = s.split("\n");
        StringBuilder builder = new StringBuilder();
        builder.append(comps[0]);
        for (int i = 1; i < comps.length; i++) {
            builder.append("\n");
            for (int j = 0; j < level; j++) {
                builder.append(spaceString);
            }
            builder.append(comps[i]);
        }
        return builder.toString();
    }

    public static String safeToString(Object obj) {
        if (obj == null)
            return "null";
        else
            return obj.toString();
    }
}
