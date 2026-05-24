from enums.DisasterType import DisasterType
from enums.SensorType import SensorType

DisasterSensorMapping = {
    DisasterType.FLOOD: [
        SensorType.FLOODGAUGE,
        SensorType.RAINGAUGE
    ],
    DisasterType.TYPHOON: [
        SensorType.RAINGAUGE,
        SensorType.BAROMETER,
        SensorType.ANEMOMETER
    ],
    DisasterType.LANDSLIDE: [
        SensorType.RAINGAUGE,
        SensorType.SOILMOISTURE,
        SensorType.VIBRATION,
        SensorType.TILT
    ],
}