
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

To toggle between the Phase 5 transition and post-transition modes we have defined two separate profiles:

#### Transition
<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE_TRANSITION
</pre>

#### Final
<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
</pre>

We have dedicated modules for handling the phase 5 transition and phase 5 final phases. This is configured through the `play.additional.module` key in `application.conf`.

* `config.TransitionModule` will ensure that any requests to the `transit-movements-trader-manage-documents` service will have a 'transition' (`application/vnd.hmrc.transition+pdf`) Accept header.
* Conversely, `config.PostTransitionModule` will ensure those requests have a 'final' (`application/vnd.hmrc.final+pdf`) Accept header.

This service uses switches defined in `application.conf` that toggle between Phase 4 and Phase 5 frontends/journeys.

Phase 4 and Phase 5 features can co-exist by setting both feature flags to `true`. This will allow users to access any in-flight P4 movements, with the links for new movements pointing to the P5 frontends.

```yaml
microservice.services.features {
  isPhase4Enabled = true
  isPhase5Enabled = true
}
```

The above features are covered by several service-manager-config definitions:

1. `MANAGE_TRANSIT_MOVEMENTS_FRONTEND`
   * `microservice.services.features.isPhase4Enabled = true`
   * `microservice.services.features.isPhase5Enabled = false`
2. `MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5`
   * `microservice.services.features.isPhase4Enabled = false`
   * `microservice.services.features.isPhase5Enabled = true`
   * `play.additional.module=config.PostTransitionModule`
3. `MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5_TRANSITION`
   * `microservice.services.features.isPhase4Enabled = false`
   * `microservice.services.features.isPhase5Enabled = true`
   * `play.additional.module=config.TransitionModule`


### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
