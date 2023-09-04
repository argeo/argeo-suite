/** API to be used by Java.
 *  @module MapPart
 */

/** Abstract base class for displaying a map. */
export default class MapPart {

	/** The name of the map, will also be the name of the variable */
	#mapName;

	constructor(mapName) {
		this.#mapName = mapName;
		this.createMapDiv(this.#mapName);
	}

	//
	// ABSTRACT METHODS
	//

	/** Zoom the map to the given value. */
	setZoom(zoom) {
		throw new Error("Abstract method");
	}

	/** Set the center of the map to the given coordinates. */
	setCenter(lng, lat) {
		throw new Error("Abstract method");
	}

	/** Add a single point. */
	addPoint(lng, lat, style) {
		throw new Error("Abstract method");
	}

	addUrlLayer(url, format) {
		throw new Error("Abstract method");
	}

	//
	// EXTENSIONS
	//
	loadMapModule(url) {
		var script = document.createElement("script");
		script.src = url;
		document.head.appendChild(script);
		//		import(url)
		//			.then(module => { })
		//			.catch((error) => 'An error occurred while loading the component');
	}

	//
	// ACCESSORS
	//
	getMapName() {
		return this.#mapName;
	}

	//
	// HTML
	//
	createMapDiv(id) {
		var mapDiv = document.createElement('div');
		mapDiv.id = id;
		mapDiv.className = this.getMapDivCssClass();
		mapDiv.style.cssText = 'width: 100%; height: 100vh;';
		document.body.appendChild(mapDiv);
	}

	getMapDivCssClass() {
		throw new Error("Abstract method");
	}
}
