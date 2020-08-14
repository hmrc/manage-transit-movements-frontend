#!/bin/bash

echo ""
echo "Applying migration ViewDepartures"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /viewDepartures                       controllers.ViewDeparturesController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "viewDepartures.title = viewDepartures" >> ../conf/messages.en
echo "viewDepartures.heading = viewDepartures" >> ../conf/messages.en

echo "Migration ViewDepartures completed"
