## Maven

Add the following repository to your `pom.xml`

```xml
<!-- Bundabrg's Repo -->
<repository>
    <id>bundabrg-repo</id>
    <url>https://repo.worldguard.com.au/repository/maven-public</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>   
```

Add the following dependency to your `pom.xml`
```xml
<dependency>
    <groupId>au.com.grieve</groupId>
    <artifactId>PortalNetwork</artifactId>
    <version>1.2-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## New Portal Type

When creating a new Portal Type you will want to override either [portals.BasePortal](https://github.com/bundabrg/PortalNetwork/blob/master/src/main/java/au/com/grieve/portalnetwork/portals/BasePortal.java)
or one of the existing portal types.

The main methods you are interested in are:

* activate - Called when a portal activates.
* deactivate - Called when a portal deactivate
* getPortalIterator - Returns an iterator over the blocks that make up the body of a portal
* getPortalBaseIterator - Returns an iterator over the blocks that make up the base of a portal
* getPortalFrameIterator - Returns an iterator over the frame of a portal

## Registering Portal Type

Use [PortalManager#registerPortalClass](https://github.com/bundabrg/PortalNetwork/blob/master/src/main/java/au/com/grieve/portalnetwork/PortalManager.java)
to register the new portal type.

For example if you have a new PortalType class called "DiamondPortal" that creates a diamond shaped portal you would
do the following:

```java
PortalNetwork.getInstance().getPortalManager()
    .registerPortalClass("diamond", DiamondPortal.class);
```

Then use the following command to get the portalblock ingame:
```
/pn give -type diamond
```
