FROM amazoncorretto:17.0.7-al2

# copy 当前jar路径 目标jar路径及其名称
COPY /target/app.jar /temp/random.jar

#EXPOSE 8080

# 与copy中的路径需对应
ENTRYPOINT ["java", "-jar", "/temp/random.jar"]