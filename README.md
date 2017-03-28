# protobuf-demo
Protocol buffers demo, and benchmark against SOAP.

This simple app contains a Service component that basically just responds to requests, and two interfaces that expose it:

1. A web service
2. An asynchronous protobuf server

There are clients for both interfaces, and they are used in a simple benchmark that sends 50,000 requests:

1. The web service client uses a thread pool to send the requests.
   The thread pool has 1 thread per CPU.
2. The protobuf client uses a single connection to send all requests

I ran this in an 8-core machine and the results are:

* Protobuf client completes in about one second
* Web Service client with 8-thread pool completes in about 9 seconds

The protobuf server uses thread-per-connection scheme. Not optimal, I know, but simple enough for the purpose of the demo.
