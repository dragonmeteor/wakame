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

import wakame.bsdf.Dielectric;
import wakame.bsdf.Diffuse;
import wakame.bsdf.Microfacet;
import wakame.bsdf.Mirror;
import wakame.camera.PerspectiveCamera;
import wakame.emitter.AreaEmitter;
import wakame.emitter.DistantDisk;
import wakame.emitter.EnvironmentMap;
import wakame.emitter.PointLight;
import wakame.integrator.*;
import wakame.media.Heterogeneous;
import wakame.media.Homogeneous;
import wakame.mesh.WavefrontOBJ;
import wakame.phase.HenyeyGreenstein;
import wakame.phase.Isotropic;
import wakame.rfilter.BoxFilter;
import wakame.rfilter.GaussianFilter;
import wakame.rfilter.MitchellNetravaliFilter;
import wakame.rfilter.TentFilter;
import wakame.sampler.Independent;
import wakame.test.Chi2Test;
import wakame.test.StudentsTTest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class of all objects belonging to the Wakame ray tracer.
 * A WakameObject usually represents a part of a scene description.
 *
 * Any subclass of WakameObject MUST have a zero-argument constructor
 * and this constructor should be made private (i.e., the user must not
 * be able to directly construct an instance).  In order to enable
 * object construction, each subclass must create an inner static class called
 * Builder, which must subclasses WakameObject.Builder and implements
 * its createInstance method by returning a newly created and blank
 * instance of the subclass.
 */
public abstract class WakameObject {
    /**
     * Add a child object to the current instance.
     *
     * This method is to be used by the builder and it not meant to be used directly by the user.
     *
     * The default implementation does not support children and
     * simply throws an exception.
     *
     * @param child
     */
    protected void addChild(WakameObject child) {
        throw new RuntimeException("Adding child is not supported");
    }

    /**
     * Set the parent object.
     *
     * This method is to be used by the builder and it not meant to be used directly by the user.
     *
     * Subclasses may choose to override this method to be notified when they are
     * added to a parent object.  The default implementation does nothing.
     *
     * @param parent
     */
    protected void setParent(WakameObject parent) {
        // NO-OP
    }

    /**
     * Perform some action associated with the object to initialize it for further use.
     *
     * This method is to be used by the builder and it not meant to be used directly by the user.
     *
     * Called by the builder once it has constructed an instance, set all properties, and added all of its
     * children using addChild.
     */
    protected abstract void activate();

    /**
     * Set the properties of the instance.
     *
     * This method is to be used by the builder and it not meant to be used directly by the user.
     *
     * Called by the builder once it has construct a blank instance with the zero-argument
     * constructor, but before adding the children.
     *
     * @param properties the properties as a map from property name to
     */
    protected abstract void setProperties(HashMap<String, Object> properties);

    /**
     * Construct an instance of builder for the class of the given name.
     *
     * The name must be registered through the registerBuilder method.
     *
     * @param name the name of the class
     * @return a blank instance of the class.
     */
    public static WakameObject.Builder getBuilder(String name) {
        if (!hasBuilder(name)) {
            throw new RuntimeException("Builder of the class name '" + name + "' has not been registered");
        } else {
            try {
                Class klass = builders.get(name);
                return (WakameObject.Builder)klass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Register a builder of Java class with the given name so that the XML parser can get an instance
     * of the builder class when it sees the name.
     * @param name
     * @param klass
     */
    public static void registerBuilder(String name, Class klass) {
        builders.put(name, klass);
    }

    /**
     * Checks whether a builder with the given name has registered a builder.
     * @param name the name of the builder
     * @return whether a builder with the given name has been registered.
     */
    public static boolean hasBuilder(String name) {
        return builders.containsKey(name);
    }

    /**
     * The map from registered names to corresponding Builder class.
     */
    private final static HashMap<String, Class> builders = new HashMap<String, Class>();

    static {
        WakameObject.registerBuilder("scene", Scene.Builder.class);

        // Samplers
        WakameObject.registerBuilder("independent", Independent.Builder.class);

        // Reconstruction filters
        WakameObject.registerBuilder("gaussian", GaussianFilter.Builder.class);
        WakameObject.registerBuilder("mitchell", MitchellNetravaliFilter.Builder.class);
        WakameObject.registerBuilder("tent", TentFilter.Builder.class);
        WakameObject.registerBuilder("box", BoxFilter.Builder.class);

        // Camera
        WakameObject.registerBuilder("perspective", PerspectiveCamera.Builder.class);

        // Mesh
        WakameObject.registerBuilder("obj", WavefrontOBJ.Builder.class);

        // BSDF
        WakameObject.registerBuilder("diffuse", Diffuse.Builder.class);
        WakameObject.registerBuilder("microfacet", Microfacet.Builder.class);
        WakameObject.registerBuilder("mirror", Mirror.Builder.class);

        // Phase functions
        WakameObject.registerBuilder("isotropic", Isotropic.Builder.class);

        // Medium
        WakameObject.registerBuilder("homogeneous", Homogeneous.Builder.class);
        WakameObject.registerBuilder("heterogeneous", Heterogeneous.Builder.class);

        // Test
        WakameObject.registerBuilder("ttest", StudentsTTest.Builder.class);
        WakameObject.registerBuilder("chi2test", Chi2Test.Builder.class);
    }

    /**
     * The prototype builder class for Wakame object.
     */
    public static abstract class Builder {
        /**
         * Properties of the Wakame object to construct.
         */
        protected HashMap<String, Object> properties = new HashMap<String, Object>();
        /**
         * Children of the Wakame object to construct.
         */
        protected ArrayList<WakameObject> children = new ArrayList<WakameObject>();

        /**
         * Create a new instance of a Wakame object.
         * This method is used by the build() method to create a new instance internally
         * and is to be overridden by subclasses.
         *
         * @return a new, blank instance of the Wakame object
         */
        protected abstract WakameObject createInstance();

        /**
         * Add a child to the Wakame object to be constructed.
         * @param child the child
         * @return this instance to enable method chaining
         */
        public WakameObject.Builder addChild(WakameObject child) {
            children.add(child);
            return this;
        }

        /**
         * Set a property of the Wakame object to be constructed
         * @param name
         * @param value
         * @return this instance to enable method chaining
         */
        public WakameObject.Builder setProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        /**
         * Actually creating the Wakame object.
         * @return a new, activated Wakame object with children added
         */
        public WakameObject build() {
            WakameObject instance = createInstance();
            instance.setProperties(properties);
            for (WakameObject child : children) {
                instance.addChild(child);
                child.setParent(instance);
            }
            instance.activate();
            return instance;
        }
    }
}
