#!/bin/bash

echo ""
echo "Applying migration ControlDecision"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /controlDecision                       controllers.ControlDecisionController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "controlDecision.title = controlDecision" >> ../conf/messages.en
echo "controlDecision.heading = controlDecision" >> ../conf/messages.en

echo "Migration ControlDecision completed"
