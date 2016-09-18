package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.glutils.Mesh30;
import gaia.cu9.ari.gaiaorbit.util.glutils.ShaderProgram30;

public interface IQuadRenderable extends IRenderable {

    /**
     * Renders the renderable as a quad using the star shader.
     * 
     * @param shader
     * @param alpha
     * @param camera
     */
    public void render(ShaderProgram30 shader, float alpha, boolean colorTransit, Mesh30 mesh, ICamera camera);
}
