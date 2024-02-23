
# manage-transit-movements-frontend

This service allows a user to view and manage their transit arrivals and departures.

Service manager port: 9485

### Main entry point

    http://localhost:9485/manage-transit-movements

### Testing

Run unit tests:
<pre>sbt test</pre>  
Run integration tests:
<pre>sbt it/test</pre>
Run accessibility linter tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests

#### Phase 4

    sm2 --start CTC_TRADERS_ALL_ACCEPTANCE
    sm2 --stop MANAGE_TRANSIT_MOVEMENTS_FRONTEND
    sbt run

#### Phase 5

    sm2 --start CTC_TRADERS_P5_ACCEPTANCE
    sm2 --stop MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5
    sbt -Dfeatures.isPhase5Enabled run

If you hit the main entry point before running the journey tests, it gets the compile out of the way and can help keep the first tests from failing.

### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
