# readme

- 打包命令

mvn clean install package -DskipTests

- 镜像包制作

在项目根目录执行如下命令

```sh
docker build -f Dockerfile -t fraud-detection:latest .
```

- 运行

docker run -d -p 8080:8080 fraud-detection:latest

- 创建 namespace

````bash
kubectl create ns fraud-detection
```
- 部署

```bash
kubectl apply -f deployment.yaml
````

