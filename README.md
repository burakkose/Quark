# Quark

Quark is a streaming first API Gateway that handles requests and manipulates them to redirect to internal services.

## Table of contents
- [What is an API Gateway?](#what-is-an-api-gateway)
- [Why does Quark exist?](#why-does-quark-exist)
- [What is not Quark?](#what-is-not-quark)
- [Quick start](#quick-start)
    - [Installation](#installation)
- [Working with Quark](#working-with-quark)
    - [Operations](#operations)
    - [Service](#service)
    - [Gateway](#gateway)
    - [Starting Http Server](#starting-http-server)
- [Authors](#authors)
- [Contributing](#contributing)
- [License](#license)

## What is an API Gateway?
An API Gateway is used as a single entry point for numerous internal servers that you do not want to expose publicly. 
![Gateway](https://drive.google.com/uc?id=0B2TF9bqC4oflUTNhajhwWVhZMDg)

## Why does Quark exist?
Quark is here to provide API Gateway pattern easily in a functional streaming way. 

## What is not Quark?
 Quark is not an API management solutions. API management solutions are a complex solution that provides design, management, ...
  
## Quick Start

### Installation

**There is no release yet, It is still under development.**

## Working with Quark

Quark encourages you to write streaming application via Akka Stream in a functional approach. The main idea of Quark is to handle request from outside world. Quark provides a pipeline which is basically a function that we can represent it by `HttpRequest -> HttpResponse`. 

![Details](https://drive.google.com/uc?id=0B2TF9bqC4oflMnh1U1FGNzdPMkk)

From a global vision, Quark is an edge server that acts as a front door for all requests from clients to internal servers for controlling and protecting private networks.

![Proxy](https://drive.google.com/uc?id=0B2TF9bqC4oflbmhJX1FYTnhqNE0)

### Operations
In Quark, the pipeline manages all request from outside world by three operations.  

#### Incoming Operation
![Request](https://drive.google.com/uc?id=0B2TF9bqC4oflVTFvRFMyTV9JOEk)

Incoming action is a simple function that takes HttpRequest and return new HttpRequest. In functional defination, it is `HttpRequest -> Either[String, HttpRequest]`

If you want to reject request, you can use **Left("cause")** or finishing transforming, **Right(httpReqeust)**. Here is a simple incoming action for logging
```
incoming{ req =>
    println("There is a new request")
    Right(req)
}
```
or rejection:
```
incoming{ req =>
    println("I don't like this reqeust")
    Left("Just reject it")
}
```
#### Endpoint Operation

![Endpoint](https://drive.google.com/uc?id=0B2TF9bqC4oflNTh6amdOUE9WN1U)

Endpoint action is a place that the gateway calls a remote server. It is basically `HttpRequest -> Either[String, HttpResponse]`

#### Outgoing Operation

![Outgoing](https://drive.google.com/uc?id=0B2TF9bqC4oflcHlIbkRVR0I1dFk)

Outgoing action is for transforming **HttpResponse** to **HttpResponse**. It is `HttpResponse -> Either[String, HttpResponse]`

### Service
To create a service defination with actions:
```
service("service-id"){
    incoming(...) ~ endpoint(...) ~ outgoing(...)
}
```

**Service ID** is necessary. Quark tries to match incoming request by service id. Quark has a service resolver for translating the request into a target URL. For instance:

`/user-service/users/1 -> service-id: user-service, remote path: /users/1`

**!!! Not implemented yet.**

Here is an example to define multiple services with concatenation.

```
service("user-service"){
    incoming(...)
} ~ service("payment-service"){
        outgoing(...) ~ endpoint(...)
}
```

### Gateway

Gateway is the structure that wraps services. It is used to define application behavior. 

```
val gate = gateway{
    service(...){
        ...
    } ~ service(...){
        ...
    } ~ service(...){
        ...
    }
}
```

### Starting Http Server
As we mentioned before, Quark is developed top of Akka. For all HTTP components, it uses Akka Http. 

```
val app: GatewayApp = GatewayApp(g)
app.start("localhost", 8080)
```
This will start a http server at **localhost:8080**

### Simple Example

```
here is an example
```

## Authors
Burak Kose <burakks41@gmail.com>

## Contributing
Contributions are very welcome!

If you see an issue that you would like to fix or improve, please submitting a pull request to help out. The repository is also open for new ideas and code reviewing!
  
## License
Quark is Open Source and available under the Apache 2 License.
