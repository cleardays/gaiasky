package gaia.cu9.ari.gaiaorbit.desktop.render;

import com.badlogic.gdx.Gdx;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

public class DesktopPostProcessorDummy implements IPostProcessor, IObserver {

    PostProcessBean ppb;

    public DesktopPostProcessorDummy() {
        ppb = new PostProcessBean();
    }

    private int getWidth(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getWidth();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_WIDTH;
        case frame:
            return GlobalConf.frame.RENDER_WIDTH;
        }
        return 0;
    }

    private int getHeight(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getHeight();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_HEIGHT;
        case frame:
            return GlobalConf.frame.RENDER_HEIGHT;
        }
        return 0;
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
        return ppb;
    }

    @Override
    public void resize(final int width, final int height) {

    }

    @Override
    public void dispose() {
    }

    @Override
    public void notify(Events event, final Object... data) {
    }

    @Override
    public boolean isLightScatterEnabled() {
        return false;
    }

}
