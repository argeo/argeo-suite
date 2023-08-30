export default class MapPart {

	setZoom(zoom) {
		throw new Error("Abstract method");
	}

	setCenter(lng, lat) {
		throw new Error("Abstract method");
	}
}
