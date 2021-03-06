package gaia.cu9.ari.gaiaorbit.render.system;

import com.bitfire.postprocessing.PostProcessor;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.postprocessing.effects.Fuzzy;

public class PixelFuzzyRenderSystem extends PixelPostProcessRenderSystem {

    public PixelFuzzyRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
    }

    protected PostProcessor getPostProcessor(int w, int h) {
        Integer key = getKey(w, h);
        if (!ppmap.containsKey(key)) {
            PostProcessor pp = new PostProcessor(w, h, true, true, true);

            // Fuzzy
            Fuzzy fuzzy = new Fuzzy(w, h, 3f);
            pp.addEffect(fuzzy);

            // Enable post processor
            pp.setEnabled(true);

            ppmap.put(key, pp);
        }
        return ppmap.get(key);
    }

}
