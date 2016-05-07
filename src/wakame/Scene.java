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

import wakame.accel.Bvh;
import wakame.camera.Camera;
import wakame.emitter.Emitter;
import wakame.integrator.Integrator;
import wakame.media.Medium;
import wakame.mesh.Mesh;
import wakame.sampler.Independent;
import wakame.sampler.Sampler;
import wakame.struct.Aabb3d;
import wakame.struct.Intersection;
import wakame.struct.Ray;
import wakame.util.DiscretePdf;
import yondoko.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scene extends WakameObject {
    private ArrayList<Mesh> meshes = new ArrayList<Mesh>();
    private Integrator integrator = null;
    private Sampler sampler = null;
    private Camera camera = null;
    private Bvh bvh = null;
    private Medium medium = null;

    public Integrator getIntegrator() {
        return integrator;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public Camera getCamera() {
        return camera;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public Bvh getBvh() {
        return bvh;
    }

    /**
     * Get the bounding box of all the meshes in the scene.
     *
     * @param bbox the receiver of the bounding box value
     */
    public void getBoundingBox(Aabb3d bbox) {
        bvh.getBoundingBox(bbox);
    }

    private Scene() {
        // NO-OP
    }

    @Override
    protected void activate() {
        bvh = new Bvh();
        for (Mesh mesh : meshes) {
            bvh.addMesh(mesh);
        }
        bvh.build();

        if (integrator == null) {
            throw new RuntimeException("No integrator was specified!");
        }
        if (camera == null) {
            throw new RuntimeException("No camera was specified!");
        }
        if (sampler == null) {
            /* Create a default (independent) sampler */
            sampler = (Sampler)new Independent.Builder().build();
        }
    }

    @Override
    public void addChild(WakameObject obj) {
        if (obj instanceof Mesh) {
            Mesh mesh = (Mesh) obj;
            meshes.add(mesh);
            if (mesh.getEmitter() != null) {
                // TODO: Write code to deal with mesh emitter.
            }
        } else if (obj instanceof Emitter) {
            // TODO: Write code to deal with emitter.
        } else if (obj instanceof Sampler) {
            if (sampler != null) {
                throw new RuntimeException("Scene.addChild(): There can be only one sampler per scene!");
            }
            this.sampler = (Sampler) obj;
        } else if (obj instanceof Camera) {
            if (camera != null) {
                throw new RuntimeException("Scene.addChild(): There can be only one camera per scene!");
            }
            this.camera = (Camera) obj;
        } else if (obj instanceof Integrator) {
            if (integrator != null) {
                throw new RuntimeException("Scene.addChild(): There can be only one integrator per scene!");
            }
            this.integrator = (Integrator) obj;
        } else if (obj instanceof Medium) {
            if (medium != null) {
                throw new RuntimeException("Scene.addChild(): There can be only one medium per scene!");
            }
            this.medium = (Medium)obj;
        } else {
            throw new RuntimeException("Scene.addChild(): Adding child whose class is " + obj.getClass().getName()
                    + " is not supported");
        }
    }

    /**
     * A scene does not have any properties on its own, so this method does nothing.
     *
     * @param properties the properties as a map from property name to values
     */
    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // NO-OP
    }

    /**
     * Intersect a ray against all triangles stored in the scene
     * and return detailed intersection information.
     *
     * @param ray the ray
     * @param its the receiver of the intersection information
     * @return whether the ray intersects anything in the scene
     */
    public boolean rayIntersect(Ray ray, Intersection its) {
        return bvh.rayIntersect(ray, its, false);
    }

    /**
     * Intersect a ray against all triangles stored in the scene
     * and  only determine whether or not there is an intersection.
     * <p>
     * This method much faster than the other ray tracing function,
     * but the performance comes at the cost of not providing any
     * additional information about the detected intersection
     * <p>
     * (not even its position).
     *
     * @param ray the ray
     * @return whether the ray intersect anything in the scene
     */
    public boolean rayIntersect(Ray ray) {
        Intersection intersection = new Intersection();
        return bvh.rayIntersect(ray, intersection, true);
    }

    public String toString() {
        String output = String.format(
                "Scene[\n" +
                "  integrator = %s,\n" +
                "  sampler = %s\n" +
                "  camera = %s,\n" +
                "  meshes = {\n",
                StringUtil.indent(StringUtil.safeToString(integrator.toString())),
                StringUtil.indent(StringUtil.safeToString(sampler.toString())),
                StringUtil.indent(StringUtil.safeToString(camera.toString())));
        for (int i = 0; i < meshes.size(); i++) {
            output += "    " + StringUtil.indent(meshes.get(i).toString(), 2) + "\n";
        }
        output += "  }\n";
        output += "]";
        return output;
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Scene();
        }
    }

    /**
     * Get the medium.
     * @return the medium of this scene
     */
    public Medium getMedium() {
        return medium;
    }
}
