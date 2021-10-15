
# manage-transit-movements-frontend

This service allows a user to create a transit movement departure.

Service manager port: 9485

### Main entry point

    http://localhost:9485/manage-transit-movements

### Testing

Run unit tests:
<pre>sbt test</pre>


### Running manually or for journey tests

    sm --start CTC_TRADERS_ARRIVAL_ACCEPTANCE -r
    sm --stop MANAGE_TRANSIT_MOVEMENTS_FRONTEND
    sbt run

If you hit the main entry point before running the journey tests, it gets the compile out of the way and can help keep the first tests from failing.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
