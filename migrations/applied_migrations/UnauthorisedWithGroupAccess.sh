#!/bin/bash

echo ""
echo "Applying migration UnauthorisedWithGroupAccess"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /unauthorisedWithGroupAccess                       controllers.UnauthorisedWithGroupAccessController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unauthorisedWithGroupAccess.title = unauthorisedWithGroupAccess" >> ../conf/messages.en
echo "unauthorisedWithGroupAccess.heading = unauthorisedWithGroupAccess" >> ../conf/messages.en

echo "Migration UnauthorisedWithGroupAccess completed"
