package org.argeo.app.geo;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Area;
import javax.xml.transform.TransformerException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.UserLayer;
import org.geotools.styling.css.CssParser;
import org.geotools.styling.css.CssTranslator;
import org.geotools.styling.css.Stylesheet;
import org.geotools.xml.styling.SLDTransformer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.GraphicalSymbol;

import si.uom.SI;
import tech.units.indriya.quantity.Quantities;

/** Utilities around geographical format, mostly wrapping GeoTools patterns. */
public class GeoUtils {

	/** In square meters. */
	public static Quantity<Area> calcArea(SimpleFeature feature) {
		try {
			Polygon p = (Polygon) feature.getDefaultGeometry();
			Point centroid = p.getCentroid();
			String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
			CoordinateReferenceSystem auto = CRS.decode(code);

			MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);

			Polygon projed = (Polygon) JTS.transform(p, transform);
			return Quantities.getQuantity(projed.getArea(), SI.SQUARE_METRE);
		} catch (MismatchedDimensionException | FactoryException | TransformException e) {
			throw new IllegalStateException("Cannot claculate area of feature");
		}
	}

	public static void exportToSvg(SimpleFeatureCollection features, Writer out, int width, int height) {
		try {
			double minY = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			List<String> shapes = new ArrayList<>();
			for (SimpleFeatureIterator it = features.features(); it.hasNext();) {
				SimpleFeature feature = it.next();
				StringBuffer sb = new StringBuffer();
				sb.append("<polyline style=\"stroke-width:1;stroke:#000000;fill:none;\" points=\"");

				Polygon p = (Polygon) feature.getDefaultGeometry();
				Point centroid = p.getCentroid();
				String code = "AUTO:42001," + centroid.getX() + "," + centroid.getY();
				CoordinateReferenceSystem auto = CRS.decode(code);

				MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);

				Polygon projed = (Polygon) JTS.transform(p, transform);

				for (Coordinate coord : projed.getCoordinates()) {
					double x = coord.x;
					if (x < minX)
						minX = x;
					if (x > maxX)
						maxX = x;
					double y = -coord.y;
					if (y < minY)
						minY = y;
					if (y > maxY)
						maxY = y;
					sb.append(x + "," + y + " ");
				}
				sb.append("\">");
				sb.append("</polyline>\n");
				shapes.add(sb.toString());

			}
			double viewportHeight = maxY - minY;
			double viewportWidth = maxX - minX;
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\"\n");
			out.write(" width=\"" + width + "\"\n");
			out.write(" height=\"" + height + "\"\n");
			out.write(" viewBox=\"" + minX + " " + minY + " " + viewportWidth + " " + viewportHeight + "\"\n");
			out.write(" preserveAspectRatio=\"xMidYMid meet\"\n");
			out.write(">\n");
			for (String shape : shapes) {
				out.write(shape);
				out.write("\n");
			}
			out.write("</svg>");
		} catch (IOException | FactoryException | MismatchedDimensionException | TransformException e) {
			throw new RuntimeException("Cannot export to SVG", e);
		}
	}

	/** Write a list of simple features to a shapefile. */
	public static void saveFeaturesAsShapefile(SimpleFeatureType featureType, List<SimpleFeature> features,
			Path shpFile) {
		try {
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

			Map<String, Serializable> params = new HashMap<>();
			params.put("url", shpFile.toUri().toURL());

			params.put("create spatial index", Boolean.TRUE);

			ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
			newDataStore.createSchema(featureType);

			String typeName = newDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);

				try (Transaction transaction = new DefaultTransaction("create")) {
					try {
						featureStore.setTransaction(transaction);
						featureStore.addFeatures(collection);
						transaction.commit();
					} catch (Exception problem) {
						transaction.rollback();
						throw new RuntimeException("Cannot write shapefile " + shpFile, problem);
					}
				}
			} else {
				throw new IllegalArgumentException(typeName + " does not support read/write access");
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot write shapefile " + shpFile, e);
		}
	}

	public static org.opengis.style.Style createStyleFromCss(String css) {
		Stylesheet ss = CssParser.parse(css);
		CssTranslator translator = new CssTranslator();
		org.opengis.style.Style style = translator.translate(ss);

//		try {
//			SLDTransformer styleTransform = new SLDTransformer();
//			String xml = styleTransform.transform(style);
//			System.out.println(xml);
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return style;
	}

	public static String createSldFromCss(String name, String title, String css) {

		StyleFactory sf = CommonFactoryFinder.getStyleFactory();

		StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
		sld.setName(name);
		sld.setTitle(title);

		UserLayer layer = sf.createUserLayer();
		layer.setName("default");

		org.opengis.style.Style style = createStyleFromCss(css);
		layer.userStyles().add((Style) style);

		sld.layers().add(layer);
		try {
			SLDTransformer styleTransform = new SLDTransformer();
			String xml = styleTransform.transform(sld);
//			System.out.println(xml);
			return xml;
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void main(String... args) {
		String css = """
				* {
				   mark: symbol(circle);
				   mark-size: 6px;
				 }

				 :mark {
				   fill: red;
				 }

								""";
		createSldFromCss("test", "Test", css);
	}

	public static String createTestSLD() {

		StyleFactory sf = CommonFactoryFinder.getStyleFactory();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
		sld.setName("sld");
		sld.setTitle("Example");
		sld.setAbstract("Example Style Layer Descriptor");

		UserLayer layer = sf.createUserLayer();
		layer.setName("layer");

		//
		// define constraint limited what features the sld applies to
		FeatureTypeConstraint constraint = sf.createFeatureTypeConstraint("Feature", Filter.INCLUDE);

		layer.layerFeatureConstraints().add(constraint);

		//
		// create a "user defined" style
		Style style = sf.createStyle();
		style.setName("style");
		style.getDescription().setTitle("User Style");
		style.getDescription().setAbstract("Definition of Style");

		//
		// define feature type styles used to actually define how features are rendered
		FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();

		// RULE 1
		// first rule to draw cities
		Rule rule1 = sf.createRule();
		rule1.setName("rule1");
		rule1.getDescription().setTitle("City");
		rule1.getDescription().setAbstract("Rule for drawing cities");
//		rule1.setFilter(ff.less(ff.property("POPULATION"), ff.literal(50000)));

		//
		// create the graphical mark used to represent a city
		Stroke stroke = sf.stroke(ff.literal("#000000"), null, null, null, null, null, null);
		Fill fill = sf.fill(null, ff.literal(java.awt.Color.BLUE), ff.literal(1.0));

//        // OnLineResource implemented by gt-metadata - so no factory!
//        OnLineResourceImpl svg = new OnLineResourceImpl(new URI("file:city.svg"));
//        svg.freeze(); // freeze to prevent modification at runtime
//
//        OnLineResourceImpl png = new OnLineResourceImpl(new URI("file:city.png"));
//        png.freeze(); // freeze to prevent modification at runtime

		//
		// List of symbols is considered in order with the rendering engine choosing
		// the first one it can handle. Allowing for svg, png, mark order
		List<GraphicalSymbol> symbols = new ArrayList<>();
//        symbols.add(sf.externalGraphic(svg, "svg", null)); // svg preferred
//        symbols.add(sf.externalGraphic(png, "png", null)); // png preferred
		symbols.add(sf.mark(ff.literal("circle"), fill, stroke)); // simple circle backup plan

		Expression opacity = null; // use default
		Expression size = ff.literal(10);
		Expression rotation = null; // use default
		AnchorPoint anchor = null; // use default
		Displacement displacement = null; // use default

		// define a point symbolizer of a small circle
		Graphic city = sf.graphic(symbols, opacity, size, rotation, anchor, displacement);
		PointSymbolizer pointSymbolizer = sf.pointSymbolizer("point", ff.property("the_geom"), null, null, city);

		rule1.symbolizers().add(pointSymbolizer);

		featureTypeStyle.rules().add(rule1);

		//
		// RULE 2 Default

//		List<GraphicalSymbol> dotSymbols = new ArrayList<>();
//		dotSymbols.add(sf.mark(ff.literal("circle"), null, null));
//		Graphic dotGraphic = sf.graphic(dotSymbols, null, ff.literal(3), null, null, null);
//		PointSymbolizer dotSymbolizer = sf.pointSymbolizer("dot", ff.property("the_geom"), null, null, dotGraphic);
//		List<org.opengis.style.Symbolizer> symbolizers = new ArrayList<>();
//		symbolizers.add(dotSymbolizer);
//		Filter other = null; // null will mark this rule as "other" accepting all remaining features
//		Rule rule2 = sf.rule("default", null, null, Double.MIN_VALUE, Double.MAX_VALUE, symbolizers, other);
//		featureTypeStyle.rules().add(rule2);

		style.featureTypeStyles().add(featureTypeStyle);

		layer.userStyles().add(style);

		sld.layers().add(layer);

		try {
			SLDTransformer styleTransform = new SLDTransformer();
			String xml = styleTransform.transform(sld);
			System.out.println(xml);
			return xml;
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}

	}

	/** Singleton. */
	private GeoUtils() {
	}
}
