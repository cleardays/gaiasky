package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Loc extends AbstractPositionEntity implements I3DTextRenderable {
    private static final float LOWER_LIMIT = 3e-4f;
    private static final float UPPER_LIMIT = 3e-3f;

    /** Longitude and latitude **/
    Vector2 location;
    Vector3 location3d;
    /** This controls the distance from the center in case of non-spherical objects **/
    float distFactor = 1f;
    float threshold;

    public Loc() {
        cc = new float[] { 1f, 1f, 1f, 1f };
        localTransform = new Matrix4();
        location3d = new Vector3();
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {

        if (((ModelBody) parent).viewAngle > ((ModelBody) parent).THRESHOLD_QUAD() * 30f) {
            updateLocalValues(time, camera);

            this.transform.translate(pos);

            Vector3d aux = v3dpool.obtain();
            this.distToCamera = (float) transform.getTranslation(aux).len();
            v3dpool.free(aux);
            this.viewAngle = (float) Math.atan(size / distToCamera) / camera.getFovFactor();
            this.viewAngleApparent = this.viewAngle;
            if (!copy) {
                addToRenderLists(camera);
            }
        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {

        ModelBody papa = (ModelBody) parent;
        papa.setToLocalTransform(distFactor, localTransform, false);

        location3d.set(0, 0, -.5f);
        // Latitude [-90..90]
        location3d.rotate(location.y, 1, 0, 0);
        // Longitude [0..360]
        location3d.rotate(location.x + 90, 0, 1, 0);

        location3d.mul(localTransform);

    }

    public void setLocation(double[] pos) {
        this.location = new Vector2((float) pos[0], (float) pos[1]);
    }

    @Override
    public void render(Object... params) {
        render((SpriteBatch) params[0], (ShaderProgram) params[1], (BitmapFont) params[2], (BitmapFont) params[3], (ICamera) params[4]);
    }

    @Override
    public boolean renderText() {
        if (viewAngle < LOWER_LIMIT || viewAngle > UPPER_LIMIT || !GaiaSky.instance.isOn(ComponentType.Labels)) {
            return false;
        }
        Vector3d aux = v3dpool.obtain();
        transform.getTranslation(aux).scl(-1);

        double cosalpha = aux.add(location3d.x, location3d.y, location3d.z).nor().dot(GaiaSky.instance.cam.getDirection().nor());
        v3dpool.free(aux);
        return cosalpha < -0.2f;
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        Vector3d pos = v3dpool.obtain();
        textPosition(camera, pos);
        shader.setUniformf("a_viewAngle", viewAngle * (float) Constants.U_TO_KM);
        shader.setUniformf("a_thOverFactor", 1f);
        render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour());
        v3dpool.free(pos);
    }

    @Override
    public float[] textColour() {
        return cc;
    }

    @Override
    public float textSize() {
        return size / 2f;
    }

    @Override
    public float textScale() {
        return .5f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        out.set(location3d);
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public boolean isLabel() {
        return false;
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Double size) {
        this.size = (float) (size * Constants.KM_TO_U);
    }

    public void setSize(Long size) {
        this.size = (float) (size * Constants.KM_TO_U);
    }

    public void setDistFactor(Double distFactor) {
        this.distFactor = distFactor.floatValue();
    }

    @Override
    public void setName(String name) {
        this.name = '\u02D9' + " " + name;
    }

}
