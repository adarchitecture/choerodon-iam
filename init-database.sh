#!/usr/bin/env bash
#!/usr/bin/env bash
MAVEN_LOCAL_REPO=$(cd / && mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)
TOOL_GROUP_ID=io.choerodon
TOOL_ARTIFACT_ID=choerodon-tool-liquibase
TOOL_VERSION=0.17.1.RELEASE
TOOL_JAR_PATH=D:/choerodon/Back/core/choerodon-starters/choerodon-tool-liquibase/target/choerodon-tool-liquibase.jar
mvn org.apache.maven.plugins:maven-dependency-plugin:get \
 -Dartifact=${TOOL_GROUP_ID}:${TOOL_ARTIFACT_ID}:${TOOL_VERSION} \
 -Dtransitive=false

java -Dspring.datasource.url="jdbc:mysql://localhost:3306/?serverTimezone=CTT&useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true" \
 -Dspring.datasource.dynamic.datasource.gen.url="jdbc:mysql://localhost:3306/?serverTimezone=CTT&useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true" \
 -Dspring.datasource.dynamic.datasource.gen.username=root \
 -Dspring.datasource.dynamic.primary=master \
 -Dspring.datasource.dynamic.strict=false \
 -Dspring.datasource.dynamic.datasource.master.url="jdbc:mysql://localhost:3306/?serverTimezone=CTT&useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true" \
 -Dspring.datasource.dynamic.datasource.master.username=root \
 -Dspring.datasource.dynamic.datasource.master.password=root \
 -Dspring.datasource.dynamic.datasource.master.driver-class-name=com.mysql.jdbc.Driver \
 -Dspring.datasource.username=root \
 -Dspring.datasource.dynamic.datasource.gen.password=root \
 -Dspring.datasource.password=root \
 -Dspring.datasource.dynamic.datasource.gen.driver-class-name=com.mysql.jdbc.Driver \
 -Dspring.datasource.driver-class-name=com.mysql.jdbc.Driver \
 -Ddata.init=true \
 -Ddata.version=1 \
 -Dserver.port=1011 \
 -Dinstaller.dataDir=D:/choerodon/Back/choerodon-platform/src/main/resources/script/db/init-data \
 -Dinstaller.groovyDir=D:/choerodon/Back/choerodon-platform/src/main/resources/script/db/groovy \
 -Dinstaller.fixDir=true \
 -Dlogging.level.root=info \
 -Dinstaller.jarPath=target/app.jar \
 -jar ${TOOL_JAR_PATH}
