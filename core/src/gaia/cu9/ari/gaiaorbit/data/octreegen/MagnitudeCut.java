package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.List;

public class MagnitudeCut implements IAggregationAlgorithm<Star> {

    private static final float START_MAG = 7;
    long starId;

    public MagnitudeCut() {
	starId = System.currentTimeMillis();
    }

    @Override
    public boolean sample(List<Star> inputStars, OctreeNode<Star> octant, int maxObjs) {
	float limitMag = START_MAG + octant.depth * 2;
	List<Star> candidates = new ArrayList<Star>(10000);
	for (Star s : inputStars) {
	    if (s.appmag < limitMag) {
		candidates.add(s);
	    }
	}
	boolean leaf = candidates.size() == inputStars.size();

	for (Star s : candidates) {
	    if (leaf) {
		octant.add(s);
		s.page = octant;
		s.pageId = octant.pageId;
	    } else {
		// New virtual star
		Star virtual = getVirtualCopy(s);
		virtual.type = 92;
		virtual.nparticles = inputStars.size() / candidates.size();

		// Add virtual to octant
		octant.add(virtual);
		virtual.page = octant;
		virtual.pageId = octant.pageId;
	    }
	}
	return leaf;
    }

    private Star getVirtualCopy(Star s) {
	Star copy = new Star();
	copy.name = s.name;
	copy.absmag = s.absmag;
	copy.appmag = s.appmag;
	copy.cc = s.cc;
	copy.colorbv = s.colorbv;
	copy.ct = s.ct;
	copy.pos = new Vector3d(s.pos);
	copy.id = starId++;
	return copy;
    }
}