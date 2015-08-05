Add the repository:

```
    <repositories>
        <repository>
            <id>s3-releases</id>
            <url>http://sillelien-maven-repo.s3-website-eu-west-1.amazonaws.com/release</url>
        </repository>
    </repositories>
```

And the dependency:

```
<dependency>
    <groupId>com.sillelien</groupId>
    <artifactId>tutum-api</artifactId>
    <version>${RELEASE}</version>
</dependency>
```        

Please refer to the [JavaDocs](http://sillelien.github.io/tutum-api/apidocs/index.html) for documentation at present.


## Badges
Build Status: [![Circle CI](https://circleci.com/gh/sillelien/tutum-api.svg?style=svg)](https://circleci.com/gh/sillelien/tutum-api)

Waffle Stories: [![Stories in Ready](https://badge.waffle.io/sillelien/tutum-api.png?label=ready&title=Ready)](https://waffle.io/sillelien/tutum-api)

Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/55c0d20865376200200027e5/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55c0d20865376200200027e5)

