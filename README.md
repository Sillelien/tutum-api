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
    <groupId>sillelien</groupId>
    <artifactId>tutum-api</artifactId>
    <version>0.21</version>
</dependency>
```        

[![Circle CI](https://circleci.com/gh/Sillelien/tutum-api/tree/master.svg?style=svg)](https://circleci.com/gh/Sillelien/tutum-api/tree/master)

[![Release](https://img.shields.io/github/release/sillelien/tutum-api.svg?label=maven)](https://jitpack.io/#sillelien/tutum-api)
