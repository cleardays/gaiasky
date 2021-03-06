package gaia.cu9.ari.gaiaorbit.desktop.gui.swing;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import net.miginfocom.swing.MigLayout;

public class SimpleGui {
    LwjglCanvas canvas;
    JFrame frame;

    public SimpleGui(JFrame fr, LwjglCanvas cv) {
        this.frame = fr;
        this.canvas = cv;
    }

    public void initialize(ISceneGraph sg) {
        this.frame.setLayout(new MigLayout("fill"));
        this.frame.add(this.canvas.getCanvas(), "growx,growy");
        this.frame.setMinimumSize(new Dimension(450, 300));
    }
}
