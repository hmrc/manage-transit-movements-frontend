#!/bin/bash

echo ""
echo "Applying migration NoReleaseForTransit"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /noReleaseForTransit                       controllers.NoReleaseForTransitController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "noReleaseForTransit.title = noReleaseForTransit" >> ../conf/messages.en
echo "noReleaseForTransit.heading = noReleaseForTransit" >> ../conf/messages.en

echo "Migration NoReleaseForTransit completed"
