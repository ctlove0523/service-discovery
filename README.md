## 服务实例互相发现

在基于微服务的架构中，一个服务通常会部署多个实例，有些业务场景需要实例之间互相感知以进行某些业务通信。比如，每个服务实例都缓存了数据库中的数据以提供访问效率，当其中一个实例修改了数据库数据时，需要同步通知其他实例以更新本地缓存，这种业务场景我们又叫做缓存同步。实现实例之间的通知有多种方式，比如通过第三方服务或者通过消息中间件，但是这也增加了对其他服务的依赖，如果仅仅为了只是实现通知就引入一个第三方服务，不管是从成本还是架构来讲，都显得有一些重。本项目旨在封装服务实例之间的互相发现机制，并提供多种实现方式，具体实现有些依赖DNS、有些依赖PaaS平台的能力还有些依赖第三方服务，可按照自己的实际情况选择实现方案。

### 1、基于K8s的Headless Services 实例发现

#### 1.1 Headless Services介绍

在k8s中，有时候我们可能并不需要一个只有一个Service IP和负载均衡功能的Service。在这种情况下我们可以通过设置cluster IP 的值为`None` 来创建一个headless service。headless service 可以和其他服务发现机制配合使用而不受限与k8s的实现。对于headless服务，系统不会分配cluster IP，kube-proxy不会处理这些服务，平台也不会提供负载均衡和代理功能。如果headless service配置了selector，DNS会自动配置（这也是使用headless服务进行实例发现的关键）。

**定义selector的headless service** 

如果headless service定义了selector，endpoint controller会创建`Endpoints` 记录并修改DNS以直接返回service后面Pod的地址。

**未定义selector的headless service**

如果headless service没有定义selector，endpoint controller不会创建`Endpoints` 。但是DNS系统会进行查找和配置：

* 对于`ExternalName` 类型的服务，通过CNAME记录进行查找。
* 对于其他类型的服务，查找与service name相同的任何`Endpoints` 记录。

#### 1.2 定义headless service

* 编写service定义文件

  ~~~yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: spring-cloud-gateway-headless
    labels:
      app: gateway
  spec:
    ports:
      - name: gateway-headless-service
        port: 8080
        targetPort: 5225
    # This must set be None    
    clusterIP: None
    selector:
      trust-service: gateway
  ~~~

* 使用`kubectl`创建service

  ~~~shell
  kubectl create -f spring-cloud-gateway-headless.yaml
  ~~~

### 2、基于k8s Service和API的实例发现

基于K8s的API进行服务实例发现就极其的灵活了，可以说几乎没有任何额外的依赖。这里仅介绍基于service的实现方案，这里service可以是普通的service不要求一定是headles类型的service。

#### 2.1 实例发现流程

* 通过API获取指定service name的service 信息，并从service信息中拿到selector。
* 通过selector信息查找对应的pod，并获取pod的地址。

2.2 调用API准备

* 创建账号

  ~~~
  kubectl create serviceaccount chentong
  ~~~

* 账号绑定较色

  ~~~
  kubectl create clusterrolebinding chentong-rolebinding --clusterrole=cluster-admin --serviceaccount=default:chentong
  ~~~

* 获取密码:

  ~~~
  kubectl get serviceaccounts chentong -o yaml
  ~~~

  **输出:**

  ~~~
  apiVersion: v1
  kind: ServiceAccount
  metadata:
    ...
  secrets:
  - name: chentong-token-v8lrh
  ~~~

  直接获取密码命令：

  ~~~
  kubectl get secret chentong-token-v8lrh -o yaml
  ~~~

  输出:

  ~~~
  apiVersion: v1
  data:
    ca.crt: {ca}
    namespace: ZGVmYXVsdA==
    token: {token}
  kind: Secret
  metadata:
    annotations:
      kubernetes.io/service-account.name: chentong
      kubernetes.io/service-account.uid: e88a5db9-1780-11eb-8bc8-fa163edda8e8
    creationTimestamp: 2020-10-26T11:46:38Z
    name: chentong-token-v8lrh
    namespace: default
    resourceVersion: "66194811"
    selfLink: /api/v1/namespaces/default/secrets/chentong-token-v8lrh
    uid: e88c199d-1780-11eb-b83d-fa163e30f7ea
  type: kubernetes.io/service-account-token
  ~~~

* 获取解码后的密码

  ~~~
  kubectl get secret chentong-token-v8lrh -n default -o jsonpath={".data.token"} | base64 -d
  ~~~

  

