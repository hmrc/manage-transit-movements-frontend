
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

### Sortable table
The sortable table on the `/draft-declarations` page is based on https://design-patterns.service.justice.gov.uk/components/sortable-table/ from the MOJ design system. Related CSS and JS is pulled in through the `ministryofjustice__frontend` dependency.

Some of the underlying CSS looks in `/node-modules` for `govuk-frontend` imports, but this is located elsewhere in our project when pulled in through `play-frontend-hmrc`.

To get round this, a symlink has been created in `build.sbt` that links off to `"target" / "web" / "web-modules" / "main" / "webjars" / "lib" / "govuk-frontend"` from `"target" / "web" / "web-modules" / "main" / "webjars" / "node_modules"`.


### License 

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
