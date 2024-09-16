
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

* `config.TransitionModule` will ensure that any requests to the `transit-movements-trader-manage-documents` service will have a 2.0 APIVersion header.
* Conversely, `config.PostTransitionModule` will ensure those requests have a 2.1 APIVersion header.

The above features are covered by two service-manager-config definitions:

1. `MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5`
   * `play.additional.module=config.PostTransitionModule`
2. `MANAGE_TRANSIT_MOVEMENTS_FRONTEND_P5_TRANSITION`
   * `play.additional.module=config.TransitionModule`


### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
