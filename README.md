### Simple microservice using Akka HTTP and Slick

Simple service that transfers money between two accounts:

 * just one endpoint (`/transfer`)
 * JSON request and response
 * H2 as an in-memory database backend (prepopulated with example data at startup, can be changed to other)

To run type:

`sbt run`


Example use:

```
curl -X POST -H 'Content-Type: application/json' http://localhost:8080/transfer -d '{"from": 1, "to": 2, "amount": 10}'
```

Service responds with status code 200 (Ok) for correctly completed request and with code 400 (BadRequest) in case request cannot be fulfilled, which is:

 * incorrect source or destination user id
 * trying to transfer negative amount
 * trying to transfer more than available on source account

Database backend can easily be changed to any other supported by Slick, just provide other implementation of `DBComponent` trait (the reason this trait exists is that you'll probably want to use different database for testing and for production).
