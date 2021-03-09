#!/bin/bash

echo ""
echo "Applying migration UnauthorisedWithoutGroupAccess"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /unauthorisedWithoutGroupAccess                       controllers.UnauthorisedWithoutGroupAccessController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unauthorisedWithoutGroupAccess.title = unauthorisedWithoutGroupAccess" >> ../conf/messages.en
echo "unauthorisedWithoutGroupAccess.heading = unauthorisedWithoutGroupAccess" >> ../conf/messages.en

echo "Migration UnauthorisedWithoutGroupAccess completed"
