package seamap;

import java.awt.geom.Point2D;

import seamap.SeaMap.*;

public interface MapContext {
	Point2D getPoint(Snode coord);
	double mile(Feature feature);
}
