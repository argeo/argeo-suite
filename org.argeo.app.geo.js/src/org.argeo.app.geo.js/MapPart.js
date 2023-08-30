/** API to be used by Java.
 *  @module MapPart
 */

/** Abstract base class for displaying a map. */
export default class MapPart {

	/** Zoom the map to the given value. */
	setZoom(zoom) {
		throw new Error("Abstract method");
	}

	/** Set the center of the map to the given coordinates. */
	setCenter(lng, lat) {
		throw new Error("Abstract method");
	}
}
