
# manage-transit-movements-frontend

This service allows a user to view and manage their transit arrivals and departures.

Service manager port: 9485

### Main entry point

    http://localhost:9485/manage-transit-movements

### Testing

Run unit tests:

    sbt test


### Running manually or for journey tests

    sm --start CTC_TRADERS_ARRIVAL_ACCEPTANCE -r
    sm --stop MANAGE_TRANSIT_MOVEMENTS_FRONTEND
    sbt run


If you hit the main entry point before running the journey tests, it gets the compile out of the way and can help keep the first tests from failing.

### Testing new Phase 5 frontends and user journeys

This service uses switches defined in application.conf that toggle between Phase 4 and Phase 5 frontends/journeys.

```yaml
features {
  isPhase5Enabled = false
}
```

Setting the feature to `true` will ensure that links point to the P5 frontends.

The above are set to true in service-manager-config for profile `MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5`.


### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
