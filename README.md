Downsample by 50% and display resulting image:

```bin/scaling-sandbox /path/to/test/file```

Note that .fake files must be specified according to https://github.com/scifio/scifio/blob/37d1717f4aaa5b3c92f508bce69105efdc9e16d0/src/main/java/io/scif/io/location/TestImgLocation.java, e.g.:

```
test&pixelType=uint8&axes=X,Y&lengths=100000,100000.fake
```

Turning off the ```TestImgFormat``` does not bypass SCIFIO's fake file handling due to https://github.com/scifio/scifio/blob/37d1717f4aaa5b3c92f508bce69105efdc9e16d0/src/main/java/io/scif/io/location/TestImgLocationResolver.java
