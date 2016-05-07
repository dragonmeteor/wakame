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

package wakame.mesh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wakame.WakameObject;
import wakame.struct.Transform;
import wakame.util.PropertiesUtil;
import yondoko.util.FileResolver;
import yondoko.util.IOUtil;

import javax_.vecmath.Point3d;
import javax_.vecmath.Point3i;
import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Loader for Wavefront OBJ triangle meshes
 */
public class WavefrontOBJ extends Mesh {
    /**
     * The unresolved file name of the mesh file.
     */
    private String fileName;
    /**
     * Resolved file name of the mesh file.
     */
    private String resolvedFileName;
    /**
     * The logger
     */
    private static Logger logger = LoggerFactory.getLogger(WavefrontOBJ.class);

    private WavefrontOBJ() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        fileName = PropertiesUtil.getString(properties, "filename");
        resolvedFileName = FileResolver.resolve(fileName);
        Transform trafo = PropertiesUtil.getTransform(properties, "toWorld", new Transform());

        logger.info("Loading a mesh from \"" + resolvedFileName + "\" ...");

        String content = IOUtil.readTextFile(resolvedFileName);
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
        BufferedReader fin = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<Point3d> P = new ArrayList<>();
        ArrayList<Vector2d> T = new ArrayList<>();
        ArrayList<Vector3d> N = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<OBJVertex> vertices = new ArrayList<>();
        HashMap<OBJVertex, Integer> vertexMap = new HashMap<>();

        bbox.reset();

        long start = System.currentTimeMillis();

        try {
            String line = fin.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() == 0) {
                    line = fin.readLine();
                    continue;
                }
                if (line.charAt(0) == '#') {
                    line = fin.readLine();
                    continue;
                }

                int spacePos = line.indexOf(' ');
                if (spacePos == -1) {
                    line = fin.readLine();
                    continue;
                }
                String prefix = line.substring(0, spacePos);
                String theRest = line.substring(spacePos+1, line.length()).trim();
                if (prefix.equals("v")) {
                    String[] comps = theRest.split("\\s+");
                    Point3d p = new Point3d(
                            Double.valueOf(comps[0]),
                            Double.valueOf(comps[1]),
                            Double.valueOf(comps[2]));
                    trafo.m.transform(p);
                    bbox.expandBy(p);
                    P.add(p);
                } else if (prefix.equals("vt")) {
                    String[] comps = theRest.split("\\s+");
                    Vector2d t = new Vector2d(
                            Double.valueOf(comps[0]),
                            Double.valueOf(comps[1]));
                    T.add(t);
                } else if (prefix.equals("vn")) {
                    String[] comps = theRest.split("\\s+");
                    Vector3d n = new Vector3d(
                            Double.valueOf(comps[0]),
                            Double.valueOf(comps[1]),
                            Double.valueOf(comps[2]));
                    trafo.mit.transform(n);
                    N.add(n);
                } else if (prefix.equals("f")) {
                    String[] comps = theRest.split("\\s+");
                    OBJVertex[] verts = new OBJVertex[comps.length];
                    for (int i = 0; i < verts.length; i++) {
                        verts[i] = new OBJVertex(comps[i]);
                    }

                    OBJVertex[] triVerts = new OBJVertex[3];
                    for (int i = 0; i < verts.length - 2; i++) {
                        triVerts[0] = verts[0];
                        triVerts[1] = verts[i+1];
                        triVerts[2] = verts[i+2];

                        for (int j = 0; j < 3; j++) {
                            if (!vertexMap.containsKey(triVerts[j])) {
                                indices.add(vertices.size());
                                vertexMap.put(triVerts[j], vertices.size());
                                vertices.add(triVerts[j]);
                            } else {
                                indices.add(vertexMap.get(triVerts[j]));
                            }
                        }
                    }
                }
                line = fin.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < indices.size() / 3; i++) {
            triangles.add(new Point3i(
                    indices.get(3 * i + 0),
                    indices.get(3*i+1),
                    indices.get(3*i+2)));
        }

        for (int i = 0; i < vertices.size(); i++) {
            Point3d p = P.get(vertices.get(i).p-1);
            positions.add(new Point3d(p));
        }

        if (N.size() > 0) {
            for (int i = 0; i < vertices.size(); i++) {
                Vector3d n = N.get(vertices.get(i).n-1);
                normals.add(new Vector3d(n));
            }
        }

        if (T.size() > 0) {
            for (int i = 0; i < vertices.size(); i++) {
                Vector2d t = T.get(vertices.get(i).uv-1);
                texCoords.add(new Vector2d(t));
            }
        }

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        logger.info(String.format("Done loading mesh with %d vertices and %d triangles. "
                        + "Took %d min(s) %d second(s) and %d ms.",
                positions.size(), triangles.size(),
                elapsed / (60*1000), (elapsed / 1000) % 60, elapsed % 1000));
    }

    @Override
    protected void activate() {
        super.activate();
    }

    private static class OBJVertex {
        public int p = -1;
        public int n = -1;
        public int uv = -1;

        public OBJVertex() {
            // NO-OP
        }

        public OBJVertex(String s) {
            String comps[] = s.split("/");

            if (comps.length < 1 || comps.length > 3) {
                throw new RuntimeException("Invalid vertex data: " + s);
            }

            p = Integer.valueOf(comps[0]);

            if (comps.length >= 2 && comps[1].length() > 0) {
                uv = Integer.valueOf(comps[1]);
            }

            if (comps.length >= 3 && comps[2].length() > 0) {
                n = Integer.valueOf(comps[2]);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof OBJVertex) {
                OBJVertex other = (OBJVertex)o;
                return other.p == p && other.n == n && other.uv == uv;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = Integer.hashCode(p);
            hash = hash * 37 + Integer.hashCode(uv);
            hash = hash * 37 + Integer.hashCode(n);
            return hash;
        }
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new WavefrontOBJ();
        }
    }

    public String getUnresolvedFileName() {
        return fileName;
    }

    public String toString() {
        return "WavefrontObj[\n" +
               "  filename = " + resolvedFileName + "\n" +
               "  vertices = " + positions.size() + "\n" +
               "  triangles = " + triangles.size() + "\n" +
               "]";
    }
}
