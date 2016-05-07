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

package wakame.util;

import wakame.struct.Color3d;
import wakame.struct.Transform;

import javax_.vecmath.Point3d;
import java.util.HashMap;

/**
 * Utility methods for manipulating property lists.
 */
public class PropertiesUtil {
    public static String getString(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof String)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a String");
            }
            return (String)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static String getString(HashMap<String, Object> properties, String name, String defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof String)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a String");
            }
            return (String)properties.get(name);
        } else {
            return defaultValue;
        }
    }

    public static int getInteger(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof String)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not an integer");
            }
            return (int)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static int getInteger(HashMap<String, Object> properties, String name, int defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Integer)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not an integer.");
            }
            return (int)properties.get(name);
        } else {
            return defaultValue;
        }
    }

    public static double getDouble(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Double)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not an double");
            }
            return (double)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static double getDouble(HashMap<String, Object> properties, String name, double defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Double)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not an double.");
            }
            return (double)properties.get(name);
        } else {
            return defaultValue;
        }
    }

    public static Transform getTransform(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Transform)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Transform.");
            }
            return (Transform)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static Transform getTransform(HashMap<String, Object> properties, String name, Transform defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Transform)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Transform.");
            }
            return (Transform)properties.get(name);
        } else {
            return defaultValue;
        }
    }

    public static Color3d getColor(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Color3d)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Color3d.");
            }
            return (Color3d)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static Color3d getColor(HashMap<String, Object> properties, String name, Color3d defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Color3d)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Color3d.");
            }
            return (Color3d)properties.get(name);
        } else {
            return defaultValue;
        }
    }

    public static Point3d getPoint(HashMap<String, Object> properties, String name) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Point3d)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Point3d.");
            }
            return (Point3d)properties.get(name);
        } else {
            throw new RuntimeException("PropertyUtil.getStringProperty(): The property list does not contain " +
                    "a property by the name '" + name + "'.");
        }
    }

    public static Point3d getPoint(HashMap<String, Object> properties, String name, Point3d defaultValue) {
        if (properties.containsKey(name)) {
            if (!(properties.get(name) instanceof Point3d)) {
                throw new RuntimeException("PropertyUtil.getStringProperty(): " +
                        "The property '" + name + "' is not a Point3d.");
            }
            return (Point3d)properties.get(name);
        } else {
            return defaultValue;
        }
    }
}
