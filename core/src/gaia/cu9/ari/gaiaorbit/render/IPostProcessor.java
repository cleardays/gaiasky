package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Antialiasing;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.postprocessing.effects.Fisheye;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.LightScattering;
import com.bitfire.postprocessing.effects.MotionBlur;

public interface IPostProcessor extends Disposable {
    public class PostProcessBean {
        public PostProcessor pp;
        public Bloom bloom;
        public Antialiasing antialiasing;
        public LensFlare2 lens;
        public Curvature curvature;
        public Fisheye fisheye;
        public LightScattering lscatter;
        public MotionBlur motionblur;

        public boolean capture() {
            if (pp != null)
                return pp.capture();
            else
                return false;
        }

        public boolean captureNoClear() {
            if (pp != null)
                return pp.captureNoClear();
            else
                return false;
        }

        public void render() {
            if (pp != null)
                pp.render();
        }

        public FrameBuffer captureEnd() {
            if (pp != null)
                return pp.captureEnd();
            else
                return null;
        }

        public void render(FrameBuffer dest) {
            if (pp != null)
                pp.render(dest);
        }

        public void dispose() {
            if (pp != null)
                pp.dispose();
        }

    }

    public enum RenderType {
        screen(0), screenshot(1), frame(2);

        public int index;

        private RenderType(int index) {
            this.index = index;
        }

    }

    public PostProcessBean getPostProcessBean(RenderType type);

    public void resize(int width, int height);

    public boolean isLightScatterEnabled();
}
