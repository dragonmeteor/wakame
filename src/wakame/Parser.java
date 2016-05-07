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

package wakame;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wakame.struct.Color3d;
import wakame.struct.Transform;

import javax_.vecmath.AxisAngle4d;
import javax_.vecmath.Matrix4d;
import javax_.vecmath.Point3d;
import javax_.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class Parser {
    private static HashSet<String> objectTags = new HashSet<String>(Arrays.asList(new String[]{
            "scene", "mesh", "bsdf", "emitter", "camera", "medium", "phase", "integrator",
            "sampler", "rfilter", "test"
    }));
    private static HashSet<String> propertyTags = new HashSet<String>(Arrays.asList(new String[]{
            "boolean", "integer", "float", "string", "point", "vector", "color", "transform"
    }));

    /**
     * Load a scene from the specified file name.
     *
     * @param fileName
     * @return the root object specified in the file name.
     */
    public static WakameObject loadFromXML(String fileName) {
        try {
            File xmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            return parseElement(doc.getDocumentElement(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static WakameObject parseElement(Element element, String fileName) {
        String tag = element.getNodeName();
        if (tag.equals("scene")) {
            element.setAttribute("type", "scene");
        }

        WakameObject.Builder builder = null;
        if (objectTags.contains(tag)) {
            String type = element.getAttribute("type");
            if (!WakameObject.hasBuilder(type)) {
                throw new RuntimeException("Error while parsing " + fileName
                        + ": The builder of class '" + type + "' has not been registered.");
            }
            builder = WakameObject.getBuilder(type);
        } else {
            throw new RuntimeException("Error while parsing " + fileName
                + ": Tag '" + tag + "' is not valid as an object tag.");
        }

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element)childNode;
                String childTag = childElement.getTagName();
                if (objectTags.contains(childTag)) {
                    WakameObject childObject = parseElement(childElement, fileName);
                    builder.addChild(childObject);
                } else if (propertyTags.contains(childTag)) {
                    if (childTag.equals("transform")) {
                        parseTransform(childElement, builder, fileName);
                    } else {
                        String name = extractAttribute(childElement, "name", fileName);
                        String value = extractAttribute(childElement, "value", fileName);
                        if (childTag.equals("string")) {
                            builder.setProperty(name, value);
                        } else if (childTag.equals("integer")) {
                            builder.setProperty(name, new Integer(value));
                        } else if (childTag.equals("float")) {
                            builder.setProperty(name, new Double(value));
                        } else if (childTag.equals("boolean")) {
                            builder.setProperty(name, new Boolean(value));
                        } else if (childTag.equals("point")) {
                            builder.setProperty(name, new Point3d(toVector3d(value)));
                        } else if (childTag.equals("value")) {
                            builder.setProperty(name, toVector3d(value));
                        } else if (childTag.equals("color")) {
                            builder.setProperty(name, new Color3d(toVector3d(value)));
                        } else {
                            throw new RuntimeException("Error while parsing " + fileName
                                    + ": Property tag '" + childTag +  "' is not implemented by the parser.");
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    private static void parseTransform(Element element, WakameObject.Builder builder, String fileName) {
        String name = extractAttribute(element, "name", fileName);
        Matrix4d xform = new Matrix4d();
        xform.setIdentity();

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element childElement = (Element)child;
                String childTag = childElement.getTagName();
                Matrix4d M = new Matrix4d();
                M.setIdentity();
                if (childTag.equals("translate")) {
                    String value = extractAttribute(childElement, "value", fileName);
                    Vector3d t = toVector3d(value);
                    M.setTranslation(t);
                } else if (childTag.equals("rotate")) {
                    String axisString = extractAttribute(childElement, "axis", fileName);
                    String angleString = extractAttribute(childElement, "angle", fileName);
                    Vector3d axis = toVector3d(axisString);
                    Double angle = Double.valueOf(angleString) * Math.PI / 180;
                    AxisAngle4d axisAngle = new AxisAngle4d(axis, angle);
                    M.setRotation(axisAngle);
                } else if (childTag.equals("scale")) {
                    String value = extractAttribute(childElement, "value", fileName);
                    Vector3d scales = toVector3d(value);
                    M.m00 = Double.valueOf(scales.x);
                    M.m11 = Double.valueOf(scales.y);
                    M.m22 = Double.valueOf(scales.z);
                } else if (childTag.equals("matrix")) {
                    String value = extractAttribute(childElement, "value", fileName);
                    String[] comps = value.split("[\\s,]+");
                    double[] mm = new double[16];
                    for (int j = 0; j < 16; j++) {
                        mm[j] = Double.valueOf(comps[j]);
                    }
                    M = new Matrix4d(mm);
                } else if (childTag.equals("lookat")) {
                    Vector3d origin = toVector3d(extractAttribute(childElement, "origin", fileName));
                    Vector3d target = toVector3d(extractAttribute(childElement, "target", fileName));
                    Vector3d up = toVector3d(extractAttribute(childElement, "up", fileName));

                    Vector3d dir = new Vector3d();
                    dir.sub(target, origin);
                    dir.normalize();

                    up.normalize();
                    Vector3d left = new Vector3d();
                    left.cross(up, dir);
                    left.normalize();

                    Vector3d newUp = new Vector3d();
                    newUp.cross(dir, left);
                    newUp.normalize();

                    M.m00 = left.x;
                    M.m10 = left.y;
                    M.m20 = left.z;

                    M.m01 = newUp.x;
                    M.m11 = newUp.y;
                    M.m21 = newUp.z;

                    M.m02 = dir.x;
                    M.m12 = dir.y;
                    M.m22 = dir.z;

                    M.m03 = origin.x;
                    M.m13 = origin.y;
                    M.m23 = origin.z;
                } else {
                    throw new RuntimeException("Error wile parsing " + fileName
                            + ": Tag '" + childTag + "' is not a valid transformation tag.");
                }
                xform.mul(M, xform);
            }
        }

        builder.setProperty(name, new Transform(xform));
    }

    private static String extractAttribute(Element element, String attrib, String fileName) {
        String value = element.getAttribute(attrib);
        if (value.length() == 0) {
            throw new RuntimeException("Error wile parsing " + fileName + ": The value of attribute '" + attrib +
                    "' of an element with tag '" + element.getNodeName() + "' is empty.");
        } else {
            return value;
        }
    }

    private static Vector3d toVector3d(String value) {
        String[] comps = value.split("[\\s,]+");
        return new Vector3d(Double.valueOf(comps[0]),
                Double.valueOf(comps[1]),
                Double.valueOf(comps[2]));
    }
}
