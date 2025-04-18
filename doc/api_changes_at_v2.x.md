# API Changes at v2.x
This document outlines the key differences in APIs between Android Wrapper v2.x(v2-main branch) and the Android Wrapper v1.x(main branch).

## 1. Added New interfaces for Android Wrapper v2.x

### Class Name: AlignFilter
- void setMatchTargetResolution(boolean state)
### Class Name: Config
- void enableStream(StreamType streamType)
- void enableStream(SensorType sensorType)
- void enableVideoStream(SensorType sensorType, int width, int height, int fps, Format format)
- void enableAccelStream()
- void enableGyroStream()
- void disableStream(SensorType sensorType)
- void disableAllStream()
- void setFrameAggregateOutputMode(FrameAggregateOutputMode mode)
### Class Name: Device
- boolean isExtensionInfoExist(String infoKey)
- void updateOptionalDepthPresets(String[] filePathList, int pathCount, UpgradeCallback callback)
- String getExtensionInfo(String infoKey)
- void setHdrConfig(HdrConfig config)
- HdrConfig getHdrConfig()
- void enableHeartbeat(boolean enable)
### Class Name: Filter
- String getName()
- String getConfigSchema()
- List getConfigSchemaList()
- void setConfigValue(String configName, double value)
- double getConfigValue(String configName)
### Class Name: OBContext
- void setUvcBackendType(UvcBackendType type)
### Class Name: PointCloudFilter
- void setCoordinateSystem(CoordinateSystemType type)
### Class Name: PointFrame
- float getCoordinateValueScale()
### Class Name: Sensor
- List createRecommendedFilters()
### Class Name: StreamProfileList
- AccelStreamProfile getAccelStreamProfile(AccelFullScaleRange fullScaleRange, IMUSampleRate sampleRate)
- GyroStreamProfile getGyroStreamProfile(GyroFullScaleRange fullScaleRange, IMUSampleRate sampleRate)
### Class Name: VideoFrame
- PixelType getPixelType()

## 2. Added New Class for Android Wrapper v2.x
- FilterFactory
- FrameFactory
- SensorList
- StreamProfileFactory
- TypeHelper
- CameraDistortionModel
- CoordinateSystemType
- DataTranState
- DepthWorkModeTag
- DispOffsetConfig
- FileTranState
- FilterConfigSchemaItem
- FilterConfigValueType
- FrameAggregateOutputMode
- Float3D
- PixelType
- UvcBackendType
- UpgradeState

## 3. Modified Interfaces for Android Wrapper v2.x

|  Class Name  |  Android Wrapper v1.x  |  Android Wrapper v2.x  |
| --- | --- | --- |
|  PointCloudFilter  |  void setPositionDataScale(float scale)  |  void setCoordinateDataScaled(float factor)  |
|  StreamProfileList  |  int getStreamProfileCount()  |  int getCount()  |
|  Frame  |  FrameType getStreamType()  |  FrameType getType()  |
|  Frame|  long getFrameIndex()  |  long getIndex()  |
| Frame |  long getMetaValue(FrameMetadataType frameMetadataType)  |  long getMetadataValue(FrameMetadataType frameMetadataType)  |
|  AccelStreamProfile  |  AccelFullScaleRange getAccelFullScaleRange()  |  AccelFullScaleRange getFullScaleRange()  |
| AccelStreamProfile |  SampleRate getGyroSampleRate()  |  IMUSampleRate getSampleRate()  |
|  GyroStreamProfile  |  GyroFullScaleRange getGyroFullScaleRange()  |  GyroFullScaleRange getFullScaleRange()  |
| GyroStreamProfile |  SampleRate getGyroSampleRate()  |  IMUSampleRate getSampleRate()  |
|  HoleFillingFilter  |  void setMode(HoleFillingMode mode)  |  void setFilterMode(HoleFillingMode mode)  |
|HoleFillingFilter  |  HoleFillingMode getMode()  |  HoleFillingMode getFilterMode()  |
 

### 4. Rename Class Names for Android Wrapper v2.x

|  Android Wrapper v1.x  |  Android Wrapper v2.x  |
| --- | --- |
|  HdrMergeFilter  |  HdrMerge  |
|  CoordinateUtil  |  CoordinateTransformHelper  |
|  FormatConvertType  |  ConvertFormat  |
|  TimestampResetConfig  |  DeviceTimestampResetConfig  |
|  D2CTransform  |  Extrinsic  |
|  EdgeNoiseRemovalParams  |  EdgeNoiseRemovalFilterParams  |
|  SyncMode  |  MultiDeviceSyncMode  |
|  OBNetworkConfig  |  NetIpConfig  |
|  NoiseRemovalParams  |  NoiseRemovalFilterParams  |
|  OBRect  |  Rect  |
|  SpatialAdvancedParams  |  SpatialAdvancedFilterParams  |

### 5. Removed Class or interfaces for Android Wrapper v2.x

|  Class Name  |  Function  |
| --- | --- |
|  Config  |  void setD2CTargetResolution(int width, int height)  |
|  OBContext  |  void setLoggerToFile(LogSeverity severity, String directory, long maxFileSize, long maxFileNum)  |
|  OBContext|  void loadLicense(String licenseFilePath, String key)  |
| OBContext |  boolean isNetDeviceEnumerationEnable()  |
|  Pipeline  |  OBRect getD2CValidArea(int distance)  |
| Pipeline |  OBRect getD2CRangeValidArea(int minimumDistance, int maximumDistance)  |
| Pipeline |  void startRecord(String filePath)  |
|Pipeline  |  void stopRecord()  |
| Pipeline |  Playback getPlayback()  |
| Pipeline |  Pipeline(String playbackFile)  |
|  Playback  |  Remove this Class  |
|  Recorder  |  Remove this Class  |
|  CompressionFilter  |  Remove this Class  |
|  DeCompressionFilter  |  Remove this Class  |

### 6. Deprecated interfaces for Android Wrapper v2.x

|  Class Name  |  Function  |
| --- | --- |
|  PointCloudFilter  |  void setD2CAlignStatus(boolean isAlign)  |
|  Pipeline  |  CameraParam getCameraParam()  |
|  Pipeline|  CameraParam getCameraParamWithProfile(int colorWidth, int colorHeight, int depthWidth, int depthHeight)  |
| Pipeline |  CalibrationParam getCalibrationParam(Config config)  |
