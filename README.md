
# manage-transit-movements-frontend

This service allows a user to view and manage their transit arrivals and departures.

Service manager port: 9485

### Main entry point

    http://localhost:9485/manage-transit-movements

### Testing

Run unit tests:
<pre>sbt test</pre>
Run accessibility linter tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests

<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
</pre>

### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
